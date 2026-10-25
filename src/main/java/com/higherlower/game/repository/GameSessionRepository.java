package com.higherlower.game.repository;

import com.higherlower.game.entity.GameSession;
import com.higherlower.game.entity.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
