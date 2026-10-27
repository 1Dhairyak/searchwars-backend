package com.higherlower.game.duel;

import com.higherlower.game.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** {@code POST /api/duels} opens a duel and returns its code; playing happens on {@code /ws/duel}. */
@RestController
@RequestMapping("/api/duels")
@RequiredArgsConstructor
public class DuelController {

    private final DuelService duels;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> create() {
        return ResponseEntity.ok(ApiResponse.success("Duel created", Map.of("duelId", duels.create())));
    }
}
