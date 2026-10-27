package com.higherlower.game.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Optional request body for {@code POST /api/game/start}.
 *
 * <p>The player's name is not sent here: the server uses the name the
 * owner (JWT user, else guest token) has claimed, or a random one.</p>
 */
@Data
@NoArgsConstructor
public class StartGameRequest {

    /** Per-device secret from localStorage; identifies a guest owner. */
    @Size(min = 16, max = 128, message = "Guest token must be 16-128 characters")
    private String guestToken;
}
