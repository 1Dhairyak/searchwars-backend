package com.higherlower.game.entity;

import com.higherlower.game.entity.enums.GameStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the state of a single game session for one player.
 *
 * <p>A session is created when {@code GET /api/game/start} is called and
 * lives until the player makes a wrong guess (GAME_OVER) or abandons it.
 *
 * <p>Design decisions:
 * <ul>
 *   <li>{@code seenItemIds} persists as a comma-delimited TEXT column.  This
 *       avoids a full join table for what is conceptually a transient list,
 *       keeping the schema simple. For very large histories a proper
 *       {@code @ElementCollection} table would be preferable.</li>
 *   <li>{@code currentItemId} is the "left-hand" item whose search volume is
 *       revealed to the player; the right-hand item is fetched fresh on each
 *       request so its volume stays hidden until the guess is made.</li>
 * </ul></p>
 */
@Entity
@Table(
    name = "game_sessions",
    indexes = {
        @Index(name = "idx_session_status",    columnList = "status"),
        @Index(name = "idx_session_player",    columnList = "player_name"),
        @Index(name = "idx_session_created_at",columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    /**
     * Optional player display name, captured at game start.
     * Null-safe — anonymous sessions are valid.
     */
    @Column(name = "player_name", length = 100)
    private String playerName;

    /** The item whose search volume is currently revealed on the left. */
    @Column(name = "current_item_id")
    private String currentItemId;

    /**
     * Comma-separated list of Item IDs the player has already seen.
     * Used to prevent duplicate comparisons within a session.
     * Format: "id1,id2,id3,..."
     */
    @Builder.Default
    @Column(name = "seen_item_ids", columnDefinition = "TEXT")
    private String seenItemIds = "";

    /** Current score: incremented by 1 for each correct guess. */
    @Builder.Default
    @Column(name = "score", nullable = false)
    private Integer score = 0;

    /** Lifecycle state of the session. */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private GameStatus status = GameStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Helper methods ────────────────────────────────────────────────────────

    /**
     * Returns the seen IDs as a mutable list.
     * An empty {@code seenItemIds} string produces an empty list.
     */
    public List<String> getSeenItemIdList() {
        if (seenItemIds == null || seenItemIds.isBlank()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(List.of(seenItemIds.split(",")));
    }

    /**
     * Appends an item ID to the seen list and persists the updated
     * comma-delimited string back to the field.
     *
     * @param itemId the Item UUID to mark as seen
     */
    public void addSeenItemId(String itemId) {
        List<String> ids = getSeenItemIdList();
        if (!ids.contains(itemId)) {
            ids.add(itemId);
            this.seenItemIds = String.join(",", ids);
        }
    }

    /** Convenience: marks the session as finished (wrong guess). */
    public void markGameOver() {
        this.status = GameStatus.GAME_OVER;
    }

    /** Returns {@code true} if the session is still in progress. */
    public boolean isActive() {
        return GameStatus.ACTIVE.equals(this.status);
    }
}
