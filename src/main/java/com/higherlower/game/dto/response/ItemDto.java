package com.higherlower.game.dto.response;

import lombok.Builder;
import lombok.Value;

/**
 * Read-only view of an {@link com.higherlower.game.entity.Item}
 * returned to the client.
 *
 * <p>Note: {@code searchVolume} is intentionally OMITTED when this
 * DTO represents the "right-hand" (challenge) item so the player
 * cannot inspect the response body to cheat. The game service sends
 * a separate {@link GameRoundDto} that hides the volume field
 * for the challenge side.</p>
 */
@Value
@Builder
public class ItemDto {

    /** UUID of the item. */
    String id;

    /** Human-readable label shown to the player. */
    String title;

    /** CDN URL for the background image. */
    String imageUrl;

    /**
     * Monthly search volume — only populated for the KNOWN (left) item.
     * Will be {@code null} for the challenge item until the guess is resolved.
     */
    Long searchVolume;

    /** Optional category label. */
    String category;
}
