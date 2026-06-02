package com.higherlower.game.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Optional request body for {@code GET /api/game/start}.
 *
 * <p>The player may supply their name before the game begins so it
 * can be shown on the leaderboard. Supplying no body (or an empty
 * name) results in the session being logged as "Anonymous".</p>
 */
@Data
@NoArgsConstructor
public class StartGameRequest {

    /**
     * Player display name — optional, max 100 chars.
     * Whitespace-only names are normalised to "Anonymous" in the service.
     */
    @Size(max = 100, message = "Player name must not exceed 100 characters")
    private String playerName;
}
