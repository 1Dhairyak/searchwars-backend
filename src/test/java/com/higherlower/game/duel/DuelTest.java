package com.higherlower.game.duel;

import com.higherlower.game.dto.response.ItemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.higherlower.game.entity.enums.GuessType.HIGHER;
import static com.higherlower.game.entity.enums.GuessType.LOWER;
import static org.assertj.core.api.Assertions.*;

class DuelTest {

    // Chain: A(100) → B(200) HIGHER → C(50) LOWER → D(300) HIGHER
    static final List<ItemDto> DECK = List.of(item("A", 100), item("B", 200), item("C", 50), item("D", 300));

    final Instant t0 = Instant.parse("2026-10-27T12:00:00Z");
    final Instant play = t0.plus(Duel.START_DELAY);

    Duel started() {
        Duel duel = new Duel("ABC234", DECK, t0);
        assertThat(duel.join("g:alice", "Alice", t0)).isEqualTo(0);
        assertThat(duel.join("g:bob", "Bob", t0)).isEqualTo(1);
        return duel;
    }

    @Test
    @DisplayName("second player starts the duel after a countdown; rejoining keeps your seat; a third is refused")
    void joining() {
        Duel duel = new Duel("ABC234", DECK, t0);
        duel.join("g:alice", "Alice", t0);
        assertThat(duel.join("g:alice", "Alice", t0)).isZero();            // own link / second tab
        assertThat(duel.status).isEqualTo(Duel.Status.WAITING);

        duel.join("g:bob", "Bob", t0);
        assertThat(duel.status).isEqualTo(Duel.Status.PLAYING);
        assertThat(duel.startsAt).isEqualTo(play);
        assertThatThrownBy(() -> duel.guess(0, HIGHER, t0)).isInstanceOf(DuelException.class);
        assertThatThrownBy(() -> duel.join("g:eve", "Eve", t0)).hasMessageContaining("two players");
    }

    @Test
    @DisplayName("each player sees only their own round, with the challenge views hidden")
    void roundHidesChallenge() {
        Duel duel = started();
        duel.guess(0, HIGHER, play);                                         // alice moves ahead

        assertThat(duel.roundFor(0)).extracting(ItemDto::getTitle).containsExactly("B", "C");
        assertThat(duel.roundFor(0)[1].getMonthlyViews()).isNull();
        assertThat(duel.roundFor(1)).extracting(ItemDto::getTitle).containsExactly("A", "B");
    }

    @Test
    @DisplayName("ends as soon as it is decided: opponent out and you are ahead")
    void endsWhenDecided() {
        Duel duel = started();
        duel.guess(1, LOWER, play);                                          // bob wrong: out on 0
        assertThat(duel.status).isEqualTo(Duel.Status.PLAYING);              // alice is level, not ahead

        Duel.GuessResult r = duel.guess(0, HIGHER, play.plusSeconds(1));
        assertThat(r.correct()).isTrue();
        assertThat(r.revealed().getMonthlyViews()).isEqualTo(200);
        assertThat(duel.status).isEqualTo(Duel.Status.FINISHED);
        assertThat(duel.winner).isZero();
        assertThatThrownBy(() -> duel.guess(0, LOWER, play.plusSeconds(2))).isInstanceOf(DuelException.class);
    }

    @Test
    @DisplayName("equal scores: whoever reached the score first wins; 0–0 is a draw")
    void tieBreaks() {
        Duel duel = started();
        duel.guess(1, HIGHER, play.plusSeconds(2));                          // bob reaches 1 first
        duel.guess(0, HIGHER, play.plusSeconds(5));
        duel.guess(0, HIGHER, play.plusSeconds(6));                          // alice wrong on B→C
        duel.guess(1, HIGHER, play.plusSeconds(7));                          // bob wrong too
        assertThat(duel.winner).isEqualTo(1);

        Duel blank = started();
        blank.guess(0, LOWER, play);
        blank.guess(1, LOWER, play);
        assertThat(blank.winner).isEqualTo(Duel.DRAW);
    }

    @Test
    @DisplayName("a player is out after the round timer, or after the disconnect grace period")
    void timeoutsAndDisconnects() {
        Duel duel = started();
        duel.disconnected(1, play);
        assertThat(duel.tick(play.plus(Duration.ofSeconds(10)))).isFalse();  // still within grace
        duel.join("g:bob", "Bob", play.plusSeconds(10));                     // reconnects in time
        duel.disconnected(1, play.plusSeconds(11));

        assertThat(duel.tick(play.plus(Duel.ROUND_TIME).plusSeconds(1))).isTrue();
        assertThat(duel.player(0).outReason).isEqualTo("timeout");
        assertThat(duel.player(1).outReason).isEqualTo("timeout");           // timer beat the grace period
        assertThat(duel.status).isEqualTo(Duel.Status.FINISHED);
        assertThat(duel.winner).isEqualTo(Duel.DRAW);
    }

    @Test
    @DisplayName("finishing the whole deck ends your run with the maximum score")
    void deckExhausted() {
        Duel duel = started();
        duel.guess(0, HIGHER, play);
        duel.guess(0, LOWER, play.plusSeconds(1));
        duel.guess(0, HIGHER, play.plusSeconds(2));
        assertThat(duel.player(0).outReason).isEqualTo("finished");
        assertThat(duel.player(0).score).isEqualTo(3);
        assertThat(duel.status).isEqualTo(Duel.Status.PLAYING);              // bob plays on until he's out
    }

    static ItemDto item(String title, long views) {
        return ItemDto.builder().id(title).title(title).imageUrl("x").category("c").monthlyViews(views).build();
    }
}
