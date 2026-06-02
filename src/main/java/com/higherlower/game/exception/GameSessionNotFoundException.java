package com.higherlower.game.exception;

/**
 * Thrown when a client references a game session that does not
 * exist or is no longer in ACTIVE status.
 *
 * <p>Maps to HTTP 404 Not Found via {@link GlobalExceptionHandler}.</p>
 */
public class GameSessionNotFoundException extends RuntimeException {
    public GameSessionNotFoundException(String message) {
        super(message);
    }
}
