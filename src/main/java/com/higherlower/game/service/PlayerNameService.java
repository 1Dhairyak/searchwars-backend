package com.higherlower.game.service;

import com.higherlower.game.entity.PlayerName;
import com.higherlower.game.exception.NameTakenException;
import com.higherlower.game.repository.GameSessionRepository;
import com.higherlower.game.repository.LeaderboardRepository;
import com.higherlower.game.repository.PlayerNameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * Globally unique player names, one per owner.
 *
 * <p>An owner is {@code u:<userId>} when the request carries a valid JWT,
 * otherwise {@code g:<sha256(guestToken)>}. The raw guest token is never stored.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerNameService {

    private final PlayerNameRepository  playerNameRepository;
    private final GameSessionRepository sessionRepository;
    private final LeaderboardRepository leaderboardRepository;

    // ── Owner keys ────────────────────────────────────────────────────────────

    public static String userOwner(Long userId) {
        return "u:" + userId;
    }

    /** {@code null} when the token is absent. */
    public static String guestOwner(String guestToken) {
        if (guestToken == null || guestToken.isBlank()) return null;
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(guestToken.getBytes(StandardCharsets.UTF_8));
            return "g:" + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e); // every JVM ships SHA-256
        }
    }

    /** JWT user if authenticated (see OptionalJwtFilter), else guest hash, else {@code null}. */
    public static String currentOwner(String guestToken) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userOwner(userId);
        }
        return guestOwner(guestToken);
    }

    // ── Queries / commands ────────────────────────────────────────────────────

    public Optional<String> nameOf(String ownerKey) {
        if (ownerKey == null) return Optional.empty();
        return playerNameRepository.findByOwnerKey(ownerKey).map(PlayerName::getDisplayName);
    }

    /**
     * Claims {@code rawName} for {@code ownerKey}, renaming the owner's existing
     * name if any. If {@code sessionId} belongs to this owner, that session and
     * its leaderboard entry are renamed too.
     *
     * @return the stored display name
     * @throws NameTakenException if another owner holds the name (any case)
     */
    @Transactional
    @CacheEvict(value = "leaderboard", allEntries = true)
    public String claim(String ownerKey, String rawName, String sessionId) {
        String displayName = rawName.trim();
        String nameKey = displayName.toLowerCase(Locale.ROOT);

        playerNameRepository.findByNameKey(nameKey)
            .filter(existing -> !existing.getOwnerKey().equals(ownerKey))
            .ifPresent(existing -> { throw new NameTakenException(); });

        PlayerName row = playerNameRepository.findByOwnerKey(ownerKey)
            .orElseGet(() -> PlayerName.builder().ownerKey(ownerKey).build());
        row.setNameKey(nameKey);
        row.setDisplayName(displayName);
        try {
            playerNameRepository.saveAndFlush(row);
        } catch (DataIntegrityViolationException e) {
            throw new NameTakenException(); // lost a race for the same name
        }

        if (sessionId != null) {
            sessionRepository.findById(sessionId)
                .filter(s -> ownerKey.equals(s.getOwnerKey()))
                .ifPresent(s -> {
                    s.setPlayerName(displayName);
                    leaderboardRepository.findBySessionId(sessionId)
                        .ifPresent(e -> e.setPlayerName(displayName));
                });
        }

        log.info("Owner [{}] claimed name '{}'", ownerKey, displayName);
        return displayName;
    }

    /** Moves a guest's name to a user who has none yet (called after login). */
    @Transactional
    public Optional<String> adoptGuestName(Long userId, String guestToken) {
        String userKey = userOwner(userId);
        Optional<PlayerName> own = playerNameRepository.findByOwnerKey(userKey);
        if (own.isPresent()) return own.map(PlayerName::getDisplayName);

        String guestKey = guestOwner(guestToken);
        if (guestKey == null) return Optional.empty();
        return playerNameRepository.findByOwnerKey(guestKey).map(p -> {
            p.setOwnerKey(userKey);
            return p.getDisplayName();
        });
    }
}
