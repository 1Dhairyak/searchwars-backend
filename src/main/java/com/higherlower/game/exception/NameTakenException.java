package com.higherlower.game.exception;

/**
 * Thrown when a player tries to claim a name owned by someone else.
 *
 * <p>Maps to HTTP 409 Conflict via {@link GlobalExceptionHandler}.</p>
 */
public class NameTakenException extends RuntimeException {
    public NameTakenException() {
        super("Name already taken");
    }
}
