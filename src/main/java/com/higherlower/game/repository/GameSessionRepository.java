package com.higherlower.game.repository;

import com.higherlower.game.entity.GameSession;
import com.higherlower.game.entity.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Data access layer for {@link GameSession} entities.
 */
@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, String> {

    /**
     * Finds an active session by its ID.
     * The service layer calls this before processing any guess
     * to reject requests against already-ended sessions.
     *
     * @param id     session UUID
     * @param status must be {@link GameStatus#ACTIVE}
     */
    Optional<GameSession> findByIdAndStatus(String id, GameStatus status);

    /**
     * Bulk-marks stale active sessions as ABANDONED.
     *
     * <p>Intended to be scheduled (e.g. nightly) to clean up sessions
     * abandoned by players who navigated away. This keeps the
     * game_sessions table from growing unbounded.</p>
     *
     * @param cutoff   sessions last updated before this timestamp are abandoned
     * @param abandoned the target status string
     * @param active    only sessions currently ACTIVE are targeted
     */
    @Modifying
    @Query("""
        UPDATE GameSession gs
        SET gs.status = :abandoned
        WHERE gs.status = :active
          AND gs.updatedAt < :cutoff
        """)
    int abandonStaleSessionsBefore(
        @Param("cutoff")    LocalDateTime cutoff,
        @Param("abandoned") GameStatus    abandoned,
        @Param("active")    GameStatus    active
    );
}
