package com.higherlower.game.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Immutable leaderboard record written when a game session ends.
 *
 * <p>Intentionally kept separate from {@link GameSession} so that
 * session rows can be purged/archived without losing the public
 * leaderboard history. A session produces at most one leaderboard
 * entry (enforced by the unique constraint on {@code session_id}).</p>
 *
 * <p>The entity is append-only: once written it should never be
 * updated — hence all setters are package-private via Lombok's
 * {@code @Setter} on only what we need.</p>
 */
@Entity
@Table(
    name = "leaderboard",
    indexes = {
        @Index(name = "idx_leaderboard_score",      columnList = "score DESC"),
        @Index(name = "idx_leaderboard_player_name",columnList = "player_name"),
        @Index(name = "idx_leaderboard_achieved_at",columnList = "achieved_at DESC")
    }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    /**
     * Back-reference to the originating session.
     * Unique constraint guarantees one entry per session.
     */
    @Column(name = "session_id", updatable = false, unique = true)
    private String sessionId;

    /**
     * Player display name shown on the public leaderboard.
     * Falls back to "Anonymous" if the session had no name.
     */
    @NotBlank(message = "Player name must not be blank")
    @Size(max = 100)
    @Column(name = "player_name", nullable = false, length = 100)
    private String playerName;

    /**
     * Final score achieved in the session.
     * Stored redundantly here so leaderboard queries are self-contained
     * without joining back to game_sessions.
     */
    @NotNull
    @Min(0)
    @Column(name = "score", nullable = false)
    private Integer score;

    /**
     * Timestamp when the score was recorded (game-over moment).
     * Used for tie-breaking and date-range leaderboard filters.
     */
    @CreationTimestamp
    @Column(name = "achieved_at", updatable = false)
    private LocalDateTime achievedAt;
}
