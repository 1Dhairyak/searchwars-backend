package com.higherlower.game.repository;

import com.higherlower.game.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for {@link Item} entities.
 *
 * <p>All queries automatically filter on {@code active = true} to respect
 * soft-deletes. Raw JPQL is used instead of native SQL to stay
 * database-agnostic (H2 for tests, PostgreSQL for production).</p>
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, String> {

    /**
     * Returns all active items, ordered by title for consistent pagination.
     */
    List<Item> findAllByActiveTrueOrderByTitleAsc();

    /**
     * Selects one random active item that is NOT in the given exclusion list.
     *
     * <p>Used by the game engine to pick the next "right-hand" item while
     * preventing the same item from appearing twice in a session.</p>
     *
     * <p>The {@code ORDER BY RAND()} / {@code random()} approach is acceptable
     * for moderate dataset sizes (&lt;100k items). For truly massive catalogues
     * a keyset-based random selection would be more efficient.</p>
     *
     * @param excludedIds list of Item IDs already seen in this session
     * @return one random active item not in the exclusion list
     */
    @Query("""
        SELECT i FROM Item i
        WHERE i.active = true
          AND i.id NOT IN :excludedIds
        ORDER BY FUNCTION('random', )
        LIMIT 1
        """)
    Optional<Item> findRandomActiveItemExcluding(@Param("excludedIds") List<String> excludedIds);

    /**
     * Variant for the very first turn when there are no excluded IDs yet.
     * Avoids the empty-IN-list edge case that some JDBC drivers reject.
     */
    @Query("""
        SELECT i FROM Item i
        WHERE i.active = true
        ORDER BY FUNCTION('random', )
        LIMIT 1
        """)
    Optional<Item> findRandomActiveItem();

    /**
     * Counts how many active items exist — used to validate that the item
     * pool is large enough to start a game.
     */
    long countByActiveTrue();

    /**
     * Finds an active item by exact title (case-insensitive).
     * Used during seed-data loading to prevent duplicate inserts.
     */
    @Query("SELECT i FROM Item i WHERE LOWER(i.title) = LOWER(:title) AND i.active = true")
    Optional<Item> findByTitleIgnoreCase(@Param("title") String title);

    /**
     * Returns all active items in a given category.
     * Allows future category-specific game rounds.
     */
    List<Item> findByCategoryAndActiveTrueOrderByTitleAsc(String category);
}
