package com.higherlower.game.config;

import com.higherlower.game.entity.LeaderboardEntry;
import com.higherlower.game.repository.LeaderboardRepository;
import com.higherlower.game.util.IndianNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Replaces legacy "Anonymous" leaderboard names with random Indian names.
 * Idempotent: once no "Anonymous" rows remain it does nothing.
 */
@Component
@Profile({"dev","default","prod"})
@RequiredArgsConstructor
@Slf4j
public class AnonymousNameBackfill implements CommandLineRunner {

    private final LeaderboardRepository leaderboardRepository;

    @Override
    @Transactional
    @CacheEvict(value = "leaderboard", allEntries = true)
    public void run(String... args) {
        List<LeaderboardEntry> rows = leaderboardRepository.findByPlayerName("Anonymous");
        rows.forEach(e -> e.setPlayerName(IndianNames.random()));
        if (!rows.isEmpty()) log.info("Renamed {} 'Anonymous' leaderboard entries", rows.size());
    }
}
