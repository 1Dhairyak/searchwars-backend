package com.higherlower.game.service;

import com.higherlower.game.exception.NameTakenException;
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
}
