package com.higherlower.game.repository;

import com.higherlower.game.entity.GameSession;
import com.higherlower.game.entity.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    /** Highest score across every session the owner played; 0 if none. */
    @Query("select coalesce(max(s.score), 0) from GameSession s where s.ownerKey = :ownerKey")
    int bestScore(@Param("ownerKey") String ownerKey);

    /** Moves sessions to a new owner (guest → user on login). */
    @Modifying
    @Query("update GameSession s set s.ownerKey = :to where s.ownerKey = :from")
    int reassignOwner(@Param("from") String from, @Param("to") String to);
}
