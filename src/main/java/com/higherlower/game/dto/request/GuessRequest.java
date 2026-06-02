package com.higherlower.game.dto.request;

import com.higherlower.game.entity.enums.GuessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /api/game/guess}.
 *
 * <p>The client submits:
 * <ol>
 *   <li>The session ID so the server can look up the current round state.</li>
 *   <li>The challenge item ID the player is guessing on (double-checks
 *       client/server are in sync — prevents stale-tab replay attacks).</li>
 *   <li>The guess direction: {@code HIGHER} or {@code LOWER}.</li>
 * </ol></p>
 */
@Data
@NoArgsConstructor
public class GuessRequest {

    /**
     * The active game session UUID — returned by {@code GET /api/game/start}.
     * Must be non-blank; the service will validate it is a live session.
     */
    @NotBlank(message = "Session ID is required")
    private String sessionId;

    /**
     * UUID of the challenge item the player is guessing about.
     * Cross-checked against the session's stored challenge item to
     * detect out-of-sync requests (e.g. double-tap on mobile).
     */
    @NotBlank(message = "Challenge item ID is required")
    private String challengeItemId;

    /**
     * The player's guess: {@code HIGHER} if they believe the challenge item
     * has more monthly searches than the known item, {@code LOWER} if fewer.
     */
    @NotNull(message = "Guess type is required (HIGHER or LOWER)")
    private GuessType guess;
}
