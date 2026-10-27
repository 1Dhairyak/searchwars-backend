package com.higherlower.game.controller;

import com.higherlower.game.dto.request.ClaimNameRequest;
import com.higherlower.game.dto.response.ApiResponse;
import com.higherlower.game.repository.GameSessionRepository;
import com.higherlower.game.service.PlayerNameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Player identity endpoints.
 *
 * <ul>
 *   <li>{@code POST /api/players/name} – claim (or rename to) a globally unique name</li>
 *   <li>{@code GET  /api/players/best} – signed-in user's best score</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // tighten to specific origins in production
public class PlayerController {

    private final PlayerNameService     playerNameService;
    private final GameSessionRepository sessionRepository;

    @PostMapping("/name")
    public ResponseEntity<ApiResponse<Map<String, String>>> claimName(
            @RequestBody @Valid ClaimNameRequest request) {

        String owner = PlayerNameService.currentOwner(request.guestToken());
        if (owner == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("guestToken or login required"));
        }

        String name = playerNameService.claim(owner, request.name(), request.sessionId());
        return ResponseEntity.ok(ApiResponse.success("Name saved", Map.of("name", name)));
    }

    /** The signed-in user's best score across all their games and devices. */
    @GetMapping("/best")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> bestScore() {
        String owner = PlayerNameService.currentOwner(null);
        if (owner == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("login required"));
        }
        return ResponseEntity.ok(ApiResponse.success("Best score",
            Map.of("bestScore", sessionRepository.bestScore(owner))));
    }
}
