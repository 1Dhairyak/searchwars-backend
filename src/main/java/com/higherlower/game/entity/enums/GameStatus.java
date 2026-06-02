package com.higherlower.game.entity.enums;

/**
 * Lifecycle states for a {@link com.higherlower.game.entity.GameSession}.
 *
 * <ul>
 *   <li>{@code ACTIVE}    – The session is in progress; guesses are accepted.</li>
 *   <li>{@code GAME_OVER} – The player made a wrong guess; session is frozen.</li>
 *   <li>{@code ABANDONED} – Reserved for future use (e.g. timeout/expiry cleanup).</li>
 * </ul>
 */
public enum GameStatus {
    ACTIVE,
    GAME_OVER,
    ABANDONED
}
