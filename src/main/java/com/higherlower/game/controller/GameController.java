package com.higherlower.game.controller;

import com.higherlower.game.dto.request.GuessRequest;
import com.higherlower.game.dto.request.StartGameRequest;
import com.higherlower.game.dto.response.ApiResponse;
import com.higherlower.game.dto.response.GameRoundDto;
import com.higherlower.game.dto.response.GuessResultDto;
import com.higherlower.game.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the Higher-or-Lower game loop.
 *
 * <p>Exposes two endpoints:
 * <ul>
 *   <li>{@code GET  /api/game/start} – create a new session, get the first round</li>
 *   <li>{@code POST /api/game/guess} – submit a guess, get the result + next round</li>
 * </ul></p>
 *
 * <p>The controller is intentionally thin: it handles HTTP concerns only
 * (request parsing, status codes, response wrapping). All business logic
 * lives in {@link GameService}.</p>
 */
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // tighten to specific origins in production
public class GameController {

    private final GameService gameService;

    // ── GET /api/game/start ───────────────────────────────────────────────────

    /**
     * Starts a new game session.
     *
     * <p>Accepts an optional JSON body with a {@code playerName}.
     * The body is optional — a GET with no body is valid and results
     * in an anonymous session.</p>
     *
     * <p>Sample response:
     * <pre>{@code
     * {
     *   "success": true,
     *   "message": "Game started successfully",
     *   "data": {
     *     "sessionId": "550e8400-...",
     *     "knownItem":     { "id": "...", "title": "Budweiser", "searchVolume": 301000, ... },
     *     "challengeItem": { "id": "...", "title": "Corn Flakes", "searchVolume": null,  ... },
     *     "currentScore": 0
     *   }
     * }
     * }</pre></p>
     *
     * @param request optional player name payload (may be null / empty body)
     * @return 200 OK with {@link GameRoundDto} wrapped in {@link ApiResponse}
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<GameRoundDto>> startGame(
            @RequestBody(required = false) @Valid StartGameRequest request) {

        log.info("Received start-game request for player: {}",
            request != null ? request.getPlayerName() : "Anonymous");

        GameRoundDto round = gameService.startGame(request);

        return ResponseEntity.ok(
            ApiResponse.success("Game started successfully", round)
        );
    }

    // ── POST /api/game/guess ──────────────────────────────────────────────────

    /**
     * Submits a higher/lower guess for the current round.
     *
     * <p>Sample request body:
     * <pre>{@code
     * {
     *   "sessionId":       "550e8400-...",
     *   "challengeItemId": "abc123-...",
     *   "guess":           "HIGHER"
     * }
     * }</pre></p>
     *
     * <p>Sample response (correct guess):
     * <pre>{@code
     * {
     *   "success": true,
     *   "message": "Guess processed",
     *   "data": {
     *     "correct": true,
     *     "score": 1,
     *     "revealedChallengeItem": { "title": "Corn Flakes", "searchVolume": 450000, ... },
     *     "nextRound": { ... },
     *     "message": "Correct! Corn Flakes has 450,000 monthly searches. Score: 1",
     *     "gameOver": false
     *   }
     * }
     * }</pre></p>
     *
     * @param request validated guess payload
     * @return 200 OK with {@link GuessResultDto} wrapped in {@link ApiResponse}
     */
    @PostMapping("/guess")
    public ResponseEntity<ApiResponse<GuessResultDto>> submitGuess(
            @RequestBody @Valid GuessRequest request) {

        log.info("Received guess [{}] for session [{}] on item [{}]",
            request.getGuess(), request.getSessionId(), request.getChallengeItemId());

        GuessResultDto result = gameService.processGuess(request);

        return ResponseEntity.ok(
            ApiResponse.success("Guess processed", result)
        );
    }
}
