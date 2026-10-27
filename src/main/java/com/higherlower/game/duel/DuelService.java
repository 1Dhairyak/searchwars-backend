package com.higherlower.game.duel;

import com.higherlower.game.dto.response.ItemDto;
import com.higherlower.game.repository.ItemRepository;
import com.higherlower.game.util.ItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Live duels, held in memory.
 *
 * <p>ponytail: one JVM holds every duel — fine on a single Render instance. Running
 * several instances would need the duel state and socket fan-out in Redis (or sticky
 * routing by duel id).</p>
 */
@Service
@RequiredArgsConstructor
public class DuelService {

    static final Duration KEEP_WAITING  = Duration.ofMinutes(30);
    static final Duration KEEP_FINISHED = Duration.ofMinutes(10);
    private static final String CODE_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"; // no 0/O, 1/I/L
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final Map<String, Duel> duels = new ConcurrentHashMap<>();
    Clock clock = Clock.systemUTC(); // tests pin the time

    /** Opens a duel waiting for two players; returns its code. */
    public String create() {
        Duel duel = new Duel(newCode(), freshDeck(), clock.instant());
        duels.put(duel.id, duel);
        return duel.id;
    }

    Duel get(String id) {
        Duel duel = id == null ? null : duels.get(id.toUpperCase());
        if (duel == null) throw new DuelException("Duel not found — it may have expired");
        return duel;
    }

    /** Same two players, new deck, starting right away. Both players asking returns the same rematch. */
    String rematch(Duel old) {
        synchronized (old) {
            if (old.status != Duel.Status.FINISHED) throw new DuelException("The duel isn't over yet");
            if (old.rematchId == null) {
                Instant now = clock.instant();
                Duel next = new Duel(newCode(), freshDeck(), now);
                for (int slot = 0; slot < 2; slot++) {
                    next.join(old.players[slot].ownerKey, old.players[slot].name, now);
                }
                duels.put(next.id, next);
                old.rematchId = next.id;
            }
            return old.rematchId;
        }
    }

    /** Timeouts, forfeits and expiry. Returns the duels whose state changed. */
    List<Duel> tick() {
        Instant now = clock.instant();
        List<Duel> changed = new ArrayList<>();
        duels.values().removeIf(duel -> {
            synchronized (duel) {
                if (duel.tick(now)) changed.add(duel);
                return switch (duel.status) {
                    case WAITING  -> now.isAfter(duel.createdAt.plus(KEEP_WAITING));
                    case FINISHED -> now.isAfter(duel.finishedAt.plus(KEEP_FINISHED));
                    case PLAYING  -> false;
                };
            }
        });
        return changed;
    }

    Instant now() {
        return clock.instant();
    }

    private List<ItemDto> freshDeck() {
        List<ItemDto> deck = new ArrayList<>(itemRepository.findByActiveTrue().stream().map(itemMapper::toDto).toList());
        if (deck.size() < 2) throw new DuelException("Not enough items to play");
        Collections.shuffle(deck, RANDOM);
        return deck;
    }

    private String newCode() {
        while (true) {
            StringBuilder code = new StringBuilder(6);
            for (int i = 0; i < 6; i++) code.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            if (!duels.containsKey(code.toString())) return code.toString();
        }
    }
}
