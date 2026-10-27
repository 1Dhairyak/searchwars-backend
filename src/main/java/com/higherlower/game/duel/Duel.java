package com.higherlower.game.duel;

import com.higherlower.game.dto.response.ItemDto;
import com.higherlower.game.entity.enums.GuessType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * One 1v1 match and all of its rules. Pure state + logic (no Spring, no I/O) so
 * every rule is unit-testable with a fixed clock; callers synchronize on the duel.
 *
 * <p>Both players walk the same chain of items at their own pace: round {@code i}
 * is {@code deck[i]} (views shown) vs {@code deck[i+1]} (views hidden). Views are
 * frozen in the deck when the duel is created, so both players face identical
 * questions even if the nightly Wikipedia refresh runs mid-match.</p>
 *
 * <p>A player is out after a wrong guess, a {@link #ROUND_TIME} timeout, being
 * disconnected for {@link #DISCONNECT_GRACE}, or finishing the deck. The duel ends
 * as soon as the result is decided: both out, or one out and the other ahead.
 * Higher score wins; a tie goes to whoever reached that score first.</p>
 */
public class Duel {

    public enum Status { WAITING, PLAYING, FINISHED }

    static final Duration START_DELAY      = Duration.ofSeconds(3);
    static final Duration ROUND_TIME       = Duration.ofSeconds(15);
    static final Duration REVEAL_TIME      = Duration.ofMillis(1200); // client shows the answer before the next round
    static final Duration DISCONNECT_GRACE = Duration.ofSeconds(15);

    public static final int DRAW = -1;

    final String id;
    final List<ItemDto> deck;
    final Instant createdAt;
    final Player[] players = new Player[2];

    Status status = Status.WAITING;
    Instant startsAt;
    Instant finishedAt;
    Integer winner;        // slot 0/1, DRAW, or null while undecided
    String rematchId;

    Duel(String id, List<ItemDto> deck, Instant now) {
        if (deck.size() < 2) throw new IllegalArgumentException("A duel needs at least 2 items");
        this.id = id;
        this.deck = List.copyOf(deck);
        this.createdAt = now;
    }

    static final class Player {
        final String ownerKey;
        final String name;
        int round;             // index into deck of the known item
        int score;
        boolean out;
        String outReason;      // wrong | timeout | left | finished
        Instant scoredAt;      // when the current score was reached (tie-break)
        Instant deadline;
        boolean connected = true;
        Instant disconnectedAt;

        Player(String ownerKey, String name) {
            this.ownerKey = ownerKey;
            this.name = name;
        }
    }

    /** Result of one guess, for the guesser's reveal animation. */
    record GuessResult(boolean correct, ItemDto revealed, int score) {}

    // ── Joining ──────────────────────────────────────────────────────────────

    /**
     * Seats the owner and returns their slot. Re-joining (reconnect, second tab)
     * returns the existing slot. The second distinct player starts the duel.
     *
     * @throws DuelException if the duel is full or over for a newcomer
     */
    int join(String ownerKey, String name, Instant now) {
        for (int slot = 0; slot < 2; slot++) {
            Player p = players[slot];
            if (p != null && p.ownerKey.equals(ownerKey)) {
                p.connected = true;
                p.disconnectedAt = null;
                return slot;
            }
        }
        if (status != Status.WAITING) throw new DuelException("This duel already has two players");

        int slot = players[0] == null ? 0 : 1;
        players[slot] = new Player(ownerKey, name);
        if (players[1] != null) start(now);
        return slot;
    }

    private void start(Instant now) {
        status = Status.PLAYING;
        startsAt = now.plus(START_DELAY);
        for (Player p : players) {
            p.scoredAt = startsAt;
            p.deadline = startsAt.plus(ROUND_TIME);
        }
    }

    void disconnected(int slot, Instant now) {
        Player p = players[slot];
        p.connected = false;
        p.disconnectedAt = now;
    }

    // ── Playing ──────────────────────────────────────────────────────────────

    GuessResult guess(int slot, GuessType guess, Instant now) {
        Player p = players[slot];
        if (status != Status.PLAYING || now.isBefore(startsAt)) throw new DuelException("The duel hasn't started");
        if (p.out) throw new DuelException("You're already out");

        ItemDto known = deck.get(p.round);
        ItemDto challenge = deck.get(p.round + 1);
        boolean correct = guess.isCorrect(known.getMonthlyViews(), challenge.getMonthlyViews());

        if (correct) {
            p.score++;
            p.round++;
            p.scoredAt = now;
            p.deadline = now.plus(REVEAL_TIME).plus(ROUND_TIME);
            if (p.round + 1 >= deck.size()) knockOut(p, "finished");
        } else {
            knockOut(p, "wrong");
        }
        decide(now);
        return new GuessResult(correct, challenge, p.score);
    }

    /**
     * Applies timeouts and disconnect forfeits.
     *
     * @return whether anything changed (so the caller re-broadcasts)
     */
    boolean tick(Instant now) {
        if (status != Status.PLAYING) return false;
        boolean changed = false;
        for (Player p : players) {
            if (p.out) continue;
            if (now.isAfter(p.deadline)) {
                knockOut(p, "timeout");
                changed = true;
            } else if (!p.connected && now.isAfter(p.disconnectedAt.plus(DISCONNECT_GRACE))) {
                knockOut(p, "left");
                changed = true;
            }
        }
        if (changed) decide(now);
        return changed;
    }

    private static void knockOut(Player p, String reason) {
        p.out = true;
        p.outReason = reason;
    }

    /** Ends the duel once no outcome can change any more. */
    private void decide(Instant now) {
        Player a = players[0], b = players[1];
        boolean decided = (a.out && b.out)
            || (a.out && b.score > a.score)
            || (b.out && a.score > b.score);
        if (!decided) return;

        status = Status.FINISHED;
        finishedAt = now;
        if (a.score != b.score) winner = a.score > b.score ? 0 : 1;
        else if (a.score == 0 || a.scoredAt.equals(b.scoredAt)) winner = DRAW;
        else winner = a.scoredAt.isBefore(b.scoredAt) ? 0 : 1;
    }

    // ── Views ────────────────────────────────────────────────────────────────

    /** Known item (views shown) and challenge item (views hidden) for {@code slot}; null when not playing. */
    ItemDto[] roundFor(int slot) {
        Player p = players[slot];
        if (status != Status.PLAYING || p.out) return null;
        ItemDto challenge = deck.get(p.round + 1);
        return new ItemDto[] { deck.get(p.round), hidden(challenge) };
    }

    private static ItemDto hidden(ItemDto item) {
        return ItemDto.builder()
            .id(item.getId()).title(item.getTitle()).imageUrl(item.getImageUrl()).category(item.getCategory())
            .build();
    }

    Player player(int slot) {
        return players[slot];
    }
}
