package com.higherlower.game.exception;

/**
 * Thrown when the active item pool does not contain enough items
 * to start or continue a game.
 *
 * <p>Maps to HTTP 503 Service Unavailable via {@link GlobalExceptionHandler}
 * because it is a configuration/data issue, not a client error.</p>
 */
public class InsufficientItemsException extends RuntimeException {
    public InsufficientItemsException(String message) {
        super(message);
    }
}
