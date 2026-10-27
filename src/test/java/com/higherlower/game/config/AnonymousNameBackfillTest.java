package com.higherlower.game.config;

import com.higherlower.game.entity.LeaderboardEntry;
import com.higherlower.game.entity.PlayerName;
import com.higherlower.game.repository.LeaderboardRepository;
import com.higherlower.game.repository.PlayerNameRepository;
import com.higherlower.game.util.IndianNames;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AnonymousNameBackfillTest {

    @Autowired LeaderboardRepository leaderboard;
    @Autowired PlayerNameRepository  names;

    @Test
    @DisplayName("retired random names are replaced, but a retired name a player claimed is kept")
    void renamesRetiredUnclaimedNames() {
        assertThat(IndianNames.NAMES).doesNotContainAnyElementsOf(IndianNames.RETIRED);

        LeaderboardEntry random  = leaderboard.save(entry("s1", "Thomas Chacko"));
        LeaderboardEntry claimed = leaderboard.save(entry("s2", "Farokh Contractor"));
        LeaderboardEntry anon    = leaderboard.save(entry("s3", "Anonymous"));
        LeaderboardEntry normal  = leaderboard.save(entry("s4", "Aarav Sharma"));
        names.save(PlayerName.builder().ownerKey("u:1").nameKey("farokh contractor").displayName("Farokh Contractor").build());

        new AnonymousNameBackfill(leaderboard, names).run();

        assertThat(IndianNames.NAMES).contains(random.getPlayerName(), anon.getPlayerName());
        assertThat(claimed.getPlayerName()).isEqualTo("Farokh Contractor");
        assertThat(normal.getPlayerName()).isEqualTo("Aarav Sharma");
    }

    private static LeaderboardEntry entry(String sessionId, String name) {
        return LeaderboardEntry.builder().sessionId(sessionId).playerName(name).score(1).build();
    }
}
