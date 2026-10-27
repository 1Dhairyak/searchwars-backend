package com.higherlower.game.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a comparable item in the Higher-or-Lower game.
 *
 * <p>Each Item has {@code monthlyViews}: last month's English Wikipedia page
 * views for its article, refreshed by {@code WikipediaViewsJob}. Players try to
 * rank items against each other by it. Items are grouped by
 * {@code category} so the game engine can optionally restrict comparisons
 * to a single category for thematic rounds.</p>
 *
 * <p>Design notes:
 * <ul>
 *   <li>UUID primary key avoids sequential-ID enumeration attacks.</li>
 *   <li>{@code monthlyViews} is stored as a plain long — no floating point
 *       rounding issues when comparing.</li>
 *   <li>{@code active} flag lets admins soft-delete items without breaking
 *       historical leaderboard references.</li>
 * </ul></p>
 */
@Entity
@Table(
    name = "items",
    indexes = {
        @Index(name = "idx_item_category", columnList = "category"),
        @Index(name = "idx_item_active",   columnList = "active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "imageUrl") // imageUrl can be very long
public class Item {

    /** Surrogate primary key — UUID avoids sequential enumeration. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    /**
     * Display name shown to the player (e.g. "Budweiser", "Corn Flakes").
     * Must be unique so duplicate seed data is caught at the DB level.
     */
    @NotBlank(message = "Item title must not be blank")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    @Column(name = "title", nullable = false, unique = true, length = 200)
    private String title;

    /**
     * URL of the background/preview image displayed during gameplay.
     * Stored as TEXT to accommodate long CDN URLs.
     */
    @NotBlank(message = "Image URL must not be blank")
    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    /**
     * Last full month's English Wikipedia page views (see WikipediaViewsJob).
     * This is the value players are comparing — kept as a raw long
     * to avoid any floating-point ambiguity during comparison logic.
     */
    @NotNull(message = "Monthly views are required")
    @Min(value = 0, message = "Monthly views cannot be negative")
    @Column(name = "monthly_views", nullable = false)
    private Long monthlyViews;

    /** When {@code monthlyViews} was last fetched from Wikipedia; null = never (seed value). */
    @Column(name = "views_refreshed_at")
    private LocalDateTime viewsRefreshedAt;

    /**
     * Optional grouping category (e.g. "Food & Drink", "Sports", "Tech").
     * Stored as a free-text string for flexibility; can be normalised to
     * an enum/lookup table in a future iteration.
     */
    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Column(name = "category", length = 100)
    private String category;

    /**
     * Soft-delete flag — inactive items are excluded from new game rounds
     * but remain in the DB to preserve leaderboard history integrity.
     */
    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /** Audit: when this row was first inserted. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Audit: last modification timestamp. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
