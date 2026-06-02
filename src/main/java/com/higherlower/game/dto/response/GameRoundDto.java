package com.higherlower.game.dto.response;

import lombok.Builder;
import lombok.Value;

/**
 * Represents a single game round delivered to the player.
 *
 * <p>The "known" item is the left-hand card whose search volume IS revealed.
 * The "challenge" item is the right-hand card whose volume is hidden until
 * the player guesses.</p>
 *
 * <p>After a correct guess the previous challenge item becomes the new
 * known item, so the chain keeps growing naturally.</p>
 */
@Value
@Builder
public class GameRoundDto {

    /**
     * Session token the client must include in every subsequent
     * {@code POST /api/game/guess} request body.
     */
    String sessionId;

    /**
     * The left-hand item — volume is revealed.
     * {@link ItemDto#getSearchVolume()} will be non-null here.
     */
    ItemDto knownItem;

    /**
     * The right-hand item — volume is HIDDEN (null in the DTO).
     * The client shows only the title, image, and category.
     */
    ItemDto challengeItem;

    /** Score going into this round (before the guess). */
    Integer currentScore;
}
