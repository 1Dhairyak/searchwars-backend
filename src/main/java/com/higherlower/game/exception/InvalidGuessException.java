package com.higherlower.game.exception;

/**
 * Thrown when a guess request is structurally valid but logically
 * inconsistent with the current session state (e.g. the challengeItemId
 * in the request does not match what the server showed the client).
 *
 * <p>Maps to HTTP 409 Conflict via {@link GlobalExceptionHandler}.</p>
 */
public class InvalidGuessException extends RuntimeException {
    public InvalidGuessException(String message) {
        super(message);
    }
}
