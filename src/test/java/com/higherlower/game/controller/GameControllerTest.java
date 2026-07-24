package com.higherlower.game.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.higherlower.game.config.SecurityConfig;
import com.higherlower.game.config.OptionalJwtFilter;
import com.higherlower.game.dto.request.GuessRequest;
import com.higherlower.game.dto.request.StartGameRequest;
import com.higherlower.game.dto.response.GameRoundDto;
import com.higherlower.game.dto.response.GuessResultDto;
import com.higherlower.game.dto.response.ItemDto;
import com.higherlower.game.entity.enums.GuessType;
import com.higherlower.game.exception.GameSessionNotFoundException;
import com.higherlower.game.exception.GlobalExceptionHandler;
import com.higherlower.game.service.GameService;
import com.higherlower.game.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests for {@link GameController}.
 * Only the web layer is loaded — service is mocked.
 */
@WebMvcTest(GameController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, OptionalJwtFilter.class})
@ActiveProfiles("test")
class GameControllerTest {

    @Autowired MockMvc     mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean GameService gameService;
    @MockBean JwtUtil jwtUtil;

    // ── /api/game/start ───────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/game/start returns 200 and GameRoundDto")
    void startGame_returns200_withRound() throws Exception {
        ItemDto known = ItemDto.builder()
            .id("item-1").title("Budweiser").searchVolume(301_000L).build();
        ItemDto challenge = ItemDto.builder()
            .id("item-2").title("Corn Flakes").searchVolume(null).build();

        GameRoundDto round = GameRoundDto.builder()
            .sessionId("sess-1")
            .knownItem(known)
            .challengeItem(challenge)
            .currentScore(0)
            .build();

        given(gameService.startGame(any())).willReturn(round);

        mockMvc.perform(post("/api/game/start")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.sessionId").value("sess-1"))
            .andExpect(jsonPath("$.data.knownItem.title").value("Budweiser"))
            .andExpect(jsonPath("$.data.knownItem.searchVolume").value(301000))
            .andExpect(jsonPath("$.data.challengeItem.searchVolume").doesNotExist());
    }

    // ── /api/game/guess ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/game/guess returns 200 with correct result")
    void submitGuess_correctGuess_returns200() throws Exception {
        GuessResultDto result = GuessResultDto.builder()
            .correct(true)
            .score(1)
            .gameOver(false)
            .message("Correct!")
            .build();

        given(gameService.processGuess(any())).willReturn(result);

        GuessRequest req = new GuessRequest();
        req.setSessionId("sess-1");
        req.setChallengeItemId("item-2");
        req.setGuess(GuessType.HIGHER);

        mockMvc.perform(post("/api/game/guess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.correct").value(true))
            .andExpect(jsonPath("$.data.score").value(1))
            .andExpect(jsonPath("$.data.gameOver").value(false));
    }

    @Test
    @DisplayName("POST /api/game/guess with missing sessionId returns 400")
    void submitGuess_missingSessionId_returns400() throws Exception {
        GuessRequest req = new GuessRequest();
        // sessionId intentionally omitted
        req.setChallengeItemId("item-2");
        req.setGuess(GuessType.HIGHER);

        mockMvc.perform(post("/api/game/guess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Session ID")));
    }

    @Test
    @DisplayName("POST /api/game/guess with invalid session returns 404")
    void submitGuess_unknownSession_returns404() throws Exception {
        given(gameService.processGuess(any()))
            .willThrow(new GameSessionNotFoundException("No active session found"));

        GuessRequest req = new GuessRequest();
        req.setSessionId("bad-id");
        req.setChallengeItemId("item-2");
        req.setGuess(GuessType.HIGHER);

        mockMvc.perform(post("/api/game/guess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/game/guess with invalid enum value returns 400")
    void submitGuess_badGuessEnum_returns400() throws Exception {
        String badBody = """
            {
              "sessionId": "sess-1",
              "challengeItemId": "item-2",
              "guess": "SIDEWAYS"
            }
            """;

        mockMvc.perform(post("/api/game/guess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badBody))
            .andExpect(status().isBadRequest());
    }
}
