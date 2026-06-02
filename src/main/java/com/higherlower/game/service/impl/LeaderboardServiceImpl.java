package com.higherlower.game.service.impl;

import com.higherlower.game.dto.response.LeaderboardDto;
import com.higherlower.game.entity.LeaderboardEntry;
import com.higherlower.game.repository.LeaderboardRepository;
import com.higherlower.game.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Leaderboard implementation with simple cache-aside strategy.
 *
 * <p>The leaderboard is cached to avoid hammering the DB on every
 * page load. The cache is evicted whenever a new score is recorded.
 * For very high-traffic scenarios, replace the simple cache with
 * Redis and use a TTL-based expiry instead.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardServiceImpl implements LeaderboardService {

    /** Cache name declared in application.properties / CacheConfig. */
    static final String LEADERBOARD_CACHE = "leaderboard";

    /** Hard cap: clients may not request more than this many entries. */
    private static final int MAX_LIMIT = 100;

    private final LeaderboardRepository leaderboardRepository;

    // ── Get Top Scores ────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Results are cached by {@code limit} key. A leaderboard request
     * for top-10 and top-50 are separate cache entries.</p>
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = LEADERBOARD_CACHE, key = "#limit")
    public LeaderboardDto getTopScores(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);

        log.debug("Fetching top {} leaderboard entries (cache miss)", safeLimit);

        // Sort is also declared in the JPQL but mirrored here for safety
        PageRequest pageRequest = PageRequest.of(
            0, safeLimit,
            Sort.by(Sort.Direction.DESC, "score")
                .and(Sort.by(Sort.Direction.ASC, "achievedAt"))
        );

        Page<LeaderboardEntry> page = leaderboardRepository.findTopScores(pageRequest);

        List<LeaderboardDto.LeaderboardEntryDto> entryDtos = buildRankedEntries(page.getContent());

        return LeaderboardDto.builder()
            .entries(entryDtos)
            .totalEntries(leaderboardRepository.count())
            .generatedAt(LocalDateTime.now())
            .build();
    }

    // ── Record Score ──────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Idempotent: if a leaderboard entry for this session already exists
     * (e.g. a retry from the client), the call is a no-op. This prevents
     * double-counting on network retries.</p>
     *
     * <p>Evicts the leaderboard cache on every successful write to keep
     * reads consistent.</p>
     */
    @Override
    @Transactional
    @CacheEvict(value = LEADERBOARD_CACHE, allEntries = true)
    public void recordScore(String sessionId, String playerName, int score) {
        // Idempotency guard: never create duplicate entries for the same session
        if (leaderboardRepository.existsBySessionId(sessionId)) {
            log.warn("Leaderboard entry for session [{}] already exists — skipping duplicate write",
                sessionId);
            return;
        }

        String displayName = (playerName == null || playerName.isBlank())
            ? "Anonymous"
            : playerName.trim();

        LeaderboardEntry entry = LeaderboardEntry.builder()
            .sessionId(sessionId)
            .playerName(displayName)
            .score(score)
            .build();

        leaderboardRepository.save(entry);

        log.info("Recorded leaderboard entry — session: [{}], player: '{}', score: {}",
            sessionId, displayName, score);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Maps a list of {@link LeaderboardEntry} entities to DTOs, assigning
     * 1-based rank numbers. Tied scores get distinct sequential ranks
     * (e.g. rank 3 and 4 rather than both rank 3) to keep the list clean.
     */
    private List<LeaderboardDto.LeaderboardEntryDto> buildRankedEntries(
            List<LeaderboardEntry> entries) {

        List<LeaderboardDto.LeaderboardEntryDto> dtos = new ArrayList<>(entries.size());

        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry entry = entries.get(i);
            dtos.add(LeaderboardDto.LeaderboardEntryDto.builder()
                .rank(i + 1)                         // 1-based rank
                .playerName(entry.getPlayerName())
                .score(entry.getScore())
                .achievedAt(entry.getAchievedAt())
                .sessionId(entry.getSessionId())
                .build());
        }

        return dtos;
    }
}
