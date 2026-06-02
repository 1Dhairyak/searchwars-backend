package com.higherlower.game.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Full leaderboard response containing a ranked list of top scores.
 */
@Value
@Builder
public class LeaderboardDto {

    /** Ordered list of top entries (rank 1 = index 0). */
    List<LeaderboardEntryDto> entries;

    /** Total number of entries ever recorded (for pagination UI). */
    long totalEntries;

    /** Timestamp when this leaderboard snapshot was generated. */
    LocalDateTime generatedAt;

    /**
     * Single leaderboard row.
     */
    @Value
    @Builder
    public static class LeaderboardEntryDto {

        /** 1-based rank position. */
        int rank;

        /** Player display name. */
        String playerName;

        /** Final score achieved. */
        int score;

        /** When the score was recorded. */
        LocalDateTime achievedAt;

        /** The session ID (useful for deep-link replays in future). */
        String sessionId;
    }
}
