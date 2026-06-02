package com.higherlower.game.entity.enums;

/**
 * Represents the two possible guesses a player can make.
 *
 * <ul>
 *   <li>{@code HIGHER} – The player believes the right-hand item has a
 *       <em>greater</em> monthly search volume than the left-hand item.</li>
 *   <li>{@code LOWER}  – The player believes the right-hand item has a
 *       <em>lesser</em> monthly search volume than the left-hand item.</li>
 * </ul>
 *
 * <p>Ties (equal volumes) are treated as a {@code HIGHER} correct answer
 * to avoid a frustrating ambiguous-loss scenario.</p>
 */
public enum GuessType {
    HIGHER,
    LOWER
}
