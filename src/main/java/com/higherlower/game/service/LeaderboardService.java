package com.higherlower.game.service;

import com.higherlower.game.dto.response.LeaderboardDto;

/**
 * Contract for leaderboard read and write operations.
 */
public interface LeaderboardService {

    /**
     * Returns the global all-time top-N leaderboard.
     *
     * @param limit maximum number of entries to return (1–100)
     * @return ranked leaderboard snapshot
     */
    LeaderboardDto getTopScores(int limit);

    /**
     * Persists a leaderboard entry after a game ends.
     *
     * <p>This is called internally by {@link GameService} on game-over;
     * it is idempotent — a second call with the same {@code sessionId}
     * is a no-op.</p>
     *
     * @param sessionId  the finished session UUID
     * @param playerName display name (may be "Anonymous")
     * @param score      final score to record
     */
    void recordScore(String sessionId, String playerName, int score);
}
