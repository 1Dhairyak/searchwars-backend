package com.higherlower.game.entity.enums;

/**
 * Represents the two possible guesses a player can make.
 *
 * <ul>
 *   <li>{@code HIGHER} – The player believes the right-hand item has a
 *       <em>greater</em> monthly Wikipedia views than the left-hand item.</li>
 *   <li>{@code LOWER}  – The player believes the right-hand item has a
 *       <em>lesser</em> monthly Wikipedia views than the left-hand item.</li>
 * </ul>
 *
 * <p>Ties (equal volumes) are treated as a {@code HIGHER} correct answer
 * to avoid a frustrating ambiguous-loss scenario.</p>
 */
public enum GuessType {
    HIGHER,
    LOWER;

    /** Whether this guess is right about {@code challenge} vs {@code known}. Shared by solo and duel. */
    public boolean isCorrect(long known, long challenge) {
        return switch (this) {
            case HIGHER -> challenge >= known;   // ties: player-friendly
            case LOWER  -> challenge < known;
        };
    }
}
