package com.higherlower.game.service;

import com.higherlower.game.entity.GameSession;
import com.higherlower.game.exception.NameTakenException;
import com.higherlower.game.repository.GameSessionRepository;
import com.higherlower.game.repository.PlayerNameRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link PlayerNameService} against H2, so the unique constraints are real.
 */
@DataJpaTest
@Import(PlayerNameService.class)
@ActiveProfiles("test")
class PlayerNameServiceTest {

    @Autowired PlayerNameService    service;
    @Autowired PlayerNameRepository repo;
    @Autowired GameSessionRepository sessions;

    String alice = PlayerNameService.guestOwner("alice-device-token-0001");
    String bob   = PlayerNameService.guestOwner("bob-device-token-000002");

    @Test
    @DisplayName("names are unique per owner, case-insensitive; renaming reuses the row")
    void claimRules() {
        assertThat(service.claim(alice, "  Rocket ", null)).isEqualTo("Rocket");

        // another owner, different case → taken
        assertThatThrownBy(() -> service.claim(bob, "rOCKET", null))
            .isInstanceOf(NameTakenException.class)
            .hasMessage("Name already taken");

        // same owner can re-claim (and re-case) their own name
        assertThat(service.claim(alice, "ROCKET", null)).isEqualTo("ROCKET");

        // rename replaces the row instead of adding one, freeing the old name
        service.claim(alice, "Comet", null);
        assertThat(repo.count()).isEqualTo(1);
        assertThat(service.nameOf(alice)).contains("Comet");
        assertThat(service.claim(bob, "rocket", null)).isEqualTo("rocket");
    }

    @Test
    @DisplayName("login adopts the device's guest games, so they count toward the user's best")
    void adoptGuestMovesGamesIntoBestScore() {
        String user = PlayerNameService.userOwner(42L);
        sessions.save(GameSession.builder().ownerKey(alice).score(7).build());
        sessions.save(GameSession.builder().ownerKey(user).score(3).build());
        sessions.save(GameSession.builder().ownerKey(bob).score(99).build());
        assertThat(sessions.bestScore(user)).isEqualTo(3);

        service.claim(alice, "Rocket", null);
        assertThat(service.adoptGuest(42L, "alice-device-token-0001")).contains("Rocket");
        sessions.flush();

        assertThat(sessions.bestScore(user)).isEqualTo(7);   // alice's guest game moved over
        assertThat(sessions.bestScore(alice)).isZero();
        assertThat(sessions.bestScore("u:nobody")).isZero();
    }
}
