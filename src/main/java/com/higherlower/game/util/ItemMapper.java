package com.higherlower.game.util;

import com.higherlower.game.dto.response.ItemDto;
import com.higherlower.game.entity.Item;
import org.springframework.stereotype.Component;

/**
 * Manual mapper for {@link Item} entity ↔ {@link ItemDto}.
 *
 * <p>A hand-rolled mapper is used instead of MapStruct here because
 * the game service needs to selectively hide {@code searchVolume} for
 * the challenge item — a conditional that is cleaner in plain Java
 * than in a MapStruct annotation-driven mapping.</p>
 *
 * <p>The full-volume conversion ({@link #toDto}) is used for the KNOWN
 * (revealed) item. The game service builds the challenge-item DTO inline
 * with a null volume.</p>
 */
@Component
public class ItemMapper {

    /**
     * Maps an {@link Item} entity to an {@link ItemDto} with all fields
     * populated, including {@code searchVolume}.
     *
     * <p>Use this for the "known" (left-hand) item where the volume is
     * already visible to the player, and for the reveal after a guess.</p>
     *
     * @param item the entity to map — must not be null
     * @return fully populated DTO
     */
    public ItemDto toDto(Item item) {
        if (item == null) {
            return null;
        }
        return ItemDto.builder()
            .id(item.getId())
            .title(item.getTitle())
            .imageUrl(item.getImageUrl())
            .searchVolume(item.getSearchVolume())
            .category(item.getCategory())
            .build();
    }

    /**
     * Maps an {@link Item} to an {@link ItemDto} with {@code searchVolume}
     * deliberately omitted (null).
     *
     * <p>Use this when returning the "challenge" (right-hand) item before
     * the player's guess has been evaluated.</p>
     *
     * @param item the entity to map — must not be null
     * @return DTO with hidden search volume
     */
    public ItemDto toDtoHidden(Item item) {
        if (item == null) {
            return null;
        }
        return ItemDto.builder()
            .id(item.getId())
            .title(item.getTitle())
            .imageUrl(item.getImageUrl())
            .searchVolume(null)       // volume intentionally withheld
            .category(item.getCategory())
            .build();
    }
}
