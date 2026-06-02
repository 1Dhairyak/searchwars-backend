package com.higherlower.game.repository;

import com.higherlower.game.entity.LeaderboardEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data access layer for {@link LeaderboardEntry} entities.
 *
 * <p>All "top scores" queries use pageable results so callers control
 * how many rows are returned without the repo knowing about UI concerns.</p>
 */
@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardEntry, String> {

    /**
     * Returns the global top-N leaderboard, ordered by score DESC,
     * then by earliest achievement date for tie-breaking.
     *
     * @param pageable use {@code PageRequest.of(0, limit, Sort.by("score").descending())}
     */
    @Query("""
        SELECT le FROM LeaderboardEntry le
        ORDER BY le.score DESC, le.achievedAt ASC
        """)
    Page<LeaderboardEntry> findTopScores(Pageable pageable);

    /**
     * Finds the personal best score for a given player name.
     * Case-insensitive match so "Alice" and "alice" are treated as the same.
     *
     * @param playerName the display name to look up
     */
    @Query("""
        SELECT le FROM LeaderboardEntry le
        WHERE LOWER(le.playerName) = LOWER(:playerName)
        ORDER BY le.score DESC
        LIMIT 1
        """)
    Optional<LeaderboardEntry> findPersonalBest(@Param("playerName") String playerName);

    /**
     * Checks whether a leaderboard entry already exists for a session.
     * The service calls this to guarantee idempotent game-over handling
     * (prevents a double-submit from creating duplicate entries).
     *
     * @param sessionId the game session UUID
     */
    boolean existsBySessionId(String sessionId);

    /**
     * Returns entries achieved within a date range — useful for weekly/monthly
     * leaderboard views. Both bounds are inclusive.
     *
     * @param from      start of the period (inclusive)
     * @param to        end of the period (inclusive)
     * @param pageable  paging/sorting spec
     */
    @Query("""
        SELECT le FROM LeaderboardEntry le
        WHERE le.achievedAt BETWEEN :from AND :to
        ORDER BY le.score DESC, le.achievedAt ASC
        """)
    Page<LeaderboardEntry> findTopScoresBetween(
        @Param("from") LocalDateTime from,
        @Param("to")   LocalDateTime to,
        Pageable pageable
    );
}
