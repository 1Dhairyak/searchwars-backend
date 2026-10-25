package com.higherlower.game.util;

import com.higherlower.game.dto.response.ItemDto;
import com.higherlower.game.entity.Item;
import org.springframework.stereotype.Component;

/**
 * Manual mapper for {@link Item} entity ↔ {@link ItemDto}.
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
}
