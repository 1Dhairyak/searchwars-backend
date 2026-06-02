package com.higherlower.game.service;

import com.higherlower.game.dto.request.GuessRequest;
import com.higherlower.game.dto.request.StartGameRequest;
import com.higherlower.game.dto.response.GameRoundDto;
import com.higherlower.game.dto.response.GuessResultDto;

/**
 * Contract for all game-loop business logic.
 *
 * <p>Keeping the interface separate from the implementation lets us:
 * <ul>
 *   <li>Swap implementations (e.g. Redis-backed session store) without
 *       touching controllers or tests.</li>
 *   <li>Mock this interface cleanly in controller unit tests.</li>
 * </ul></p>
 */
public interface GameService {

    /**
     * Initialises a new game session and returns the first round.
     *
     * <p>Creates a {@link com.higherlower.game.entity.GameSession}, selects two
     * random items from the pool, marks the first as the "known" item (with
     * its volume revealed), and returns them in a {@link GameRoundDto}.</p>
     *
     * @param request optional player name
     * @return the first game round ready for the client to display
     * @throws com.higherlower.game.exception.InsufficientItemsException
     *         if the active item pool has fewer than 2 items
     */
    GameRoundDto startGame(StartGameRequest request);

    /**
     * Processes a player's higher/lower guess.
     *
     * <ol>
     *   <li>Validates the session is still active.</li>
     *   <li>Validates the challenge item ID matches the session state
     *       (prevents stale/replayed requests).</li>
     *   <li>Evaluates the guess against the actual search volumes.</li>
     *   <li>If correct: increments score, advances to the next round.</li>
     *   <li>If wrong: marks the session GAME_OVER, writes a leaderboard entry.</li>
     * </ol>
     *
     * @param request the player's guess payload
     * @return the result including the revealed volume and (if correct) next round
     * @throws com.higherlower.game.exception.GameSessionNotFoundException
     *         if the session ID does not exist or is no longer active
     * @throws com.higherlower.game.exception.InvalidGuessException
     *         if the challengeItemId does not match the session's current state
     */
    GuessResultDto processGuess(GuessRequest request);
}
