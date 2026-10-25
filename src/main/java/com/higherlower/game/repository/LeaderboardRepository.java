package com.higherlower.game.repository;

import com.higherlower.game.entity.LeaderboardEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
     * Checks whether a leaderboard entry already exists for a session.
     * The service calls this to guarantee idempotent game-over handling
     * (prevents a double-submit from creating duplicate entries).
     *
     * @param sessionId the game session UUID
     */
    boolean existsBySessionId(String sessionId);
}
