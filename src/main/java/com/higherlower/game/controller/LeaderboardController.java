package com.higherlower.game.controller;

import com.higherlower.game.dto.response.ApiResponse;
import com.higherlower.game.dto.response.LeaderboardDto;
import com.higherlower.game.service.LeaderboardService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the public leaderboard.
 *
 * <p>Exposes:
 * <ul>
 *   <li>{@code GET /api/leaderboard?limit=10} – top-N all-time scores</li>
 * </ul></p>
 */
@RestController
@RequestMapping({"/api/leaderboard", "/leaderboard"})
@RequiredArgsConstructor
@Validated  // enables constraint validation on @RequestParam
@Slf4j
@CrossOrigin(origins = "*") // tighten to specific origins in production
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    // ── GET /api/leaderboard ──────────────────────────────────────────────────

    /**
     * Returns the global top-N leaderboard.
     *
     * <p>Sample response:
     * <pre>{@code
     * {
     *   "success": true,
     *   "message": "Leaderboard retrieved successfully",
     *   "data": {
     *     "entries": [
     *       { "rank": 1, "playerName": "Alice", "score": 42, "achievedAt": "..." },
     *       { "rank": 2, "playerName": "Bob",   "score": 37, "achievedAt": "..." }
     *     ],
     *     "totalEntries": 2,
     *     "generatedAt": "2024-01-15T10:30:00"
     *   }
     * }
     * }</pre></p>
     *
     * @param limit number of entries to return (1–100, default 10)
     * @return 200 OK with {@link LeaderboardDto} wrapped in {@link ApiResponse}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<LeaderboardDto>> getLeaderboard(
            @RequestParam(defaultValue = "10")
            @Min(value = 1,   message = "Limit must be at least 1")
            @Max(value = 100, message = "Limit cannot exceed 100")
            int limit) {

        log.debug("Leaderboard request — top {} entries", limit);

        LeaderboardDto leaderboard = leaderboardService.getTopScores(limit);

        return ResponseEntity.ok(
            ApiResponse.success("Leaderboard retrieved successfully", leaderboard)
        );
    }
}
