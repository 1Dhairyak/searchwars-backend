package com.higherlower.game.dto.response;

import lombok.Builder;
import lombok.Value;

/**
 * Returned by {@code POST /api/game/guess}.
 *
 * <p>Contains the outcome of the guess plus enough context for the
 * frontend to animate the reveal (show the hidden search volume)
 * before transitioning to the next round or the game-over screen.</p>
 */
@Value
@Builder
public class GuessResultDto {

    /** {@code true} if the player guessed correctly. */
    boolean correct;

    /** Updated score after this guess (unchanged if wrong). */
    Integer score;

    /**
     * The challenge item now with its {@code searchVolume} revealed.
     * The UI uses this to animate the number counting up/down.
     */
    ItemDto revealedChallengeItem;

    /**
     * The next round to display — only present when {@code correct = true}.
     * {@code null} when the game is over so the client knows to show the
     * game-over screen instead.
     */
    GameRoundDto nextRound;

    /**
     * Human-readable result message (e.g. "Correct! +1 point" or
     * "Wrong! Corn Flakes has 450,000 monthly searches.").
     * Keeps presentation logic server-side for easy A/B testing.
     */
    String message;

    /** {@code true} signals the client to show the game-over screen. */
    boolean gameOver;
}
