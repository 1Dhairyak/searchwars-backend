package com.higherlower.game.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A globally unique display name claimed by one owner.
 *
 * <p>Owner is {@code u:<userId>} for logged-in users or
 * {@code g:<sha256 hex of guestToken>} for guests. Each owner holds at most
 * one name; claiming another renames the existing row.</p>
 */
@Entity
@Table(name = "player_names")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Trimmed, lower-cased name — the uniqueness key. */
    @Column(name = "name_key", nullable = false, unique = true, length = 20)
    private String nameKey;

    /** Name as the player typed it (trimmed). */
    @Column(name = "display_name", nullable = false, length = 20)
    private String displayName;

    @Column(name = "owner_key", nullable = false, unique = true, length = 80)
    private String ownerKey;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
