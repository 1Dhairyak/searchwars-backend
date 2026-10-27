package com.higherlower.game.controller;

import com.higherlower.game.dto.request.ClaimNameRequest;
import com.higherlower.game.dto.response.ApiResponse;
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
 * </ul>
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // tighten to specific origins in production
public class PlayerController {

    private final PlayerNameService playerNameService;

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
}
