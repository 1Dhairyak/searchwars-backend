package com.higherlower.game.duel;

/** A duel action that isn't allowed right now; the message is shown to the player. */
public class DuelException extends RuntimeException {
    public DuelException(String message) {
        super(message);
    }
}
