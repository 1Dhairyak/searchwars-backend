package com.higherlower.game.config;

import com.higherlower.game.entity.LeaderboardEntry;
import com.higherlower.game.repository.LeaderboardRepository;
import com.higherlower.game.repository.PlayerNameRepository;
import com.higherlower.game.util.IndianNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gives a fresh random name to leaderboard rows named "Anonymous" (legacy) or
 * carrying a name from {@link IndianNames#RETIRED} — unless a real player has
 * claimed that name. Idempotent: once no such rows remain it does nothing.
 */
@Component
@Profile({"dev","default","prod"})
@RequiredArgsConstructor
@Slf4j
public class AnonymousNameBackfill implements CommandLineRunner {

    private final LeaderboardRepository leaderboardRepository;
    private final PlayerNameRepository  playerNameRepository;

    @Override
    @Transactional
    @CacheEvict(value = "leaderboard", allEntries = true)
    public void run(String... args) {
        List<LeaderboardEntry> rows = new ArrayList<>(leaderboardRepository.findByPlayerName("Anonymous"));
        leaderboardRepository.findByPlayerNameIn(IndianNames.RETIRED).stream()
            .filter(e -> playerNameRepository.findByNameKey(e.getPlayerName().toLowerCase(Locale.ROOT)).isEmpty())
            .forEach(rows::add);

        rows.forEach(e -> e.setPlayerName(IndianNames.random()));
        if (!rows.isEmpty()) log.info("Renamed {} anonymous/retired-name leaderboard entries", rows.size());
    }
}
