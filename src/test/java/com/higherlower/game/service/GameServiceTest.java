package com.higherlower.game.service;

import com.higherlower.game.dto.request.GuessRequest;
import com.higherlower.game.dto.request.StartGameRequest;
import com.higherlower.game.dto.response.GameRoundDto;
import com.higherlower.game.dto.response.GuessResultDto;
import com.higherlower.game.entity.GameSession;
import com.higherlower.game.entity.Item;
import com.higherlower.game.entity.enums.GameStatus;
import com.higherlower.game.entity.enums.GuessType;
import com.higherlower.game.exception.GameSessionNotFoundException;
import com.higherlower.game.exception.InsufficientItemsException;
import com.higherlower.game.exception.InvalidGuessException;
import com.higherlower.game.repository.GameSessionRepository;
import com.higherlower.game.repository.ItemRepository;
import com.higherlower.game.service.impl.GameServiceImpl;
import com.higherlower.game.util.ItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for {@link GameServiceImpl}.
 *
 * <p>All dependencies are mocked — no Spring context or DB required.
 * Tests verify game-loop logic in isolation.</p>
 */
@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock ItemRepository       itemRepository;
    @Mock GameSessionRepository sessionRepository;
    @Mock LeaderboardService   leaderboardService;

    @InjectMocks GameServiceImpl gameService;

    // Use the real mapper — it has no dependencies
    ItemMapper itemMapper = new ItemMapper();

    // Test fixtures
    Item knownItem;
    Item challengeItem;
    GameSession activeSession;

    @BeforeEach
    void setUp() {
        // Inject real mapper via field injection workaround
        org.springframework.test.util.ReflectionTestUtils
            .setField(gameService, "itemMapper", itemMapper);

        knownItem = Item.builder()
            .id("item-1")
            .title("Budweiser")
            .imageUrl("http://img/bud.jpg")
            .searchVolume(301_000L)
            .category("Food & Drink")
            .active(true)
            .build();

        challengeItem = Item.builder()
            .id("item-2")
            .title("Corn Flakes")
            .imageUrl("http://img/corn.jpg")
            .searchVolume(450_000L)
            .category("Food & Drink")
            .active(true)
            .build();

        activeSession = GameSession.builder()
            .id("session-1")
            .playerName("Alice")
            .currentItemId("item-1")
            .score(0)
            .status(GameStatus.ACTIVE)
            .build();

        // Pre-mark both items as seen
        activeSession.addSeenItemId("item-1");
        activeSession.addSeenItemId("item-2");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // startGame()
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("startGame()")
    class StartGame {

        @Test
        @DisplayName("should create session and return first round when items are available")
        void shouldStartGame_whenItemsAvailable() {
            given(itemRepository.countByActiveTrue()).willReturn(5L);
            given(itemRepository.findRandomActiveItem()).willReturn(Optional.of(knownItem));
            given(itemRepository.findRandomActiveItemExcluding(anyList()))
                .willReturn(Optional.of(challengeItem));
            given(sessionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            StartGameRequest request = new StartGameRequest();
            request.setPlayerName("Alice");

            GameRoundDto round = gameService.startGame(request);

            assertThat(round).isNotNull();
            assertThat(round.getKnownItem().getTitle()).isEqualTo("Budweiser");
            assertThat(round.getKnownItem().getSearchVolume()).isEqualTo(301_000L);
            assertThat(round.getChallengeItem().getTitle()).isEqualTo("Corn Flakes");
            // Volume MUST be hidden for challenge item
            assertThat(round.getChallengeItem().getSearchVolume()).isNull();
            assertThat(round.getCurrentScore()).isZero();
        }

        @Test
        @DisplayName("should use 'Anonymous' when no player name provided")
        void shouldUseAnonymous_whenNoPlayerName() {
            given(itemRepository.countByActiveTrue()).willReturn(5L);
            given(itemRepository.findRandomActiveItem()).willReturn(Optional.of(knownItem));
            given(itemRepository.findRandomActiveItemExcluding(anyList()))
                .willReturn(Optional.of(challengeItem));
            given(sessionRepository.save(any())).willAnswer(inv -> {
                GameSession s = inv.getArgument(0);
                assertThat(s.getPlayerName()).isEqualTo("Anonymous");
                return s;
            });

            gameService.startGame(null);  // null request = anonymous
        }

        @Test
        @DisplayName("should throw InsufficientItemsException when pool has fewer than 2 items")
        void shouldThrow_whenInsufficientItems() {
            given(itemRepository.countByActiveTrue()).willReturn(1L);

            assertThatThrownBy(() -> gameService.startGame(null))
                .isInstanceOf(InsufficientItemsException.class)
                .hasMessageContaining("Not enough active items");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // processGuess()
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("processGuess()")
    class ProcessGuess {

        private GuessRequest guessRequest(GuessType type) {
            GuessRequest req = new GuessRequest();
            req.setSessionId("session-1");
            req.setChallengeItemId("item-2");
            req.setGuess(type);
            return req;
        }

        @Test
        @DisplayName("HIGHER guess is correct when challenge volume > known volume")
        void shouldCorrect_whenHigherGuessIsRight() {
            // challengeItem (450k) > knownItem (301k) → HIGHER is correct
            given(sessionRepository.findByIdAndStatus("session-1", GameStatus.ACTIVE))
                .willReturn(Optional.of(activeSession));
            given(itemRepository.findById("item-1")).willReturn(Optional.of(knownItem));
            given(itemRepository.findById("item-2")).willReturn(Optional.of(challengeItem));
            // Next challenge item
            Item nextItem = Item.builder().id("item-3").title("Nike").searchVolume(5_000_000L).active(true).build();
            given(itemRepository.findRandomActiveItemExcluding(anyList())).willReturn(Optional.of(nextItem));
            given(sessionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            GuessResultDto result = gameService.processGuess(guessRequest(GuessType.HIGHER));

            assertThat(result.isCorrect()).isTrue();
            assertThat(result.isGameOver()).isFalse();
            assertThat(result.getScore()).isEqualTo(1);
            assertThat(result.getRevealedChallengeItem().getSearchVolume()).isEqualTo(450_000L);
            assertThat(result.getNextRound()).isNotNull();
        }

        @Test
        @DisplayName("LOWER guess is wrong when challenge volume > known volume")
        void shouldWrong_whenLowerGuessOnHigherItem() {
            // challengeItem (450k) > knownItem (301k) → LOWER is WRONG
            given(sessionRepository.findByIdAndStatus("session-1", GameStatus.ACTIVE))
                .willReturn(Optional.of(activeSession));
            given(itemRepository.findById("item-1")).willReturn(Optional.of(knownItem));
            given(itemRepository.findById("item-2")).willReturn(Optional.of(challengeItem));
            given(sessionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            willDoNothing().given(leaderboardService).recordScore(any(), any(), anyInt());

            GuessResultDto result = gameService.processGuess(guessRequest(GuessType.LOWER));

            assertThat(result.isCorrect()).isFalse();
            assertThat(result.isGameOver()).isTrue();
            assertThat(result.getScore()).isZero();
            assertThat(result.getNextRound()).isNull();
            then(leaderboardService).should().recordScore("session-1", "Alice", 0);
        }

        @Test
        @DisplayName("Tie is treated as HIGHER correct (player-friendly)")
        void shouldCorrect_onTieWithHigherGuess() {
            Item tieItem = Item.builder()
                .id("item-2").title("Tied Item").searchVolume(301_000L).active(true).build();
            activeSession.addSeenItemId("item-2");

            given(sessionRepository.findByIdAndStatus("session-1", GameStatus.ACTIVE))
                .willReturn(Optional.of(activeSession));
            given(itemRepository.findById("item-1")).willReturn(Optional.of(knownItem));
            given(itemRepository.findById("item-2")).willReturn(Optional.of(tieItem));
            given(itemRepository.findRandomActiveItemExcluding(anyList())).willReturn(Optional.empty());
            given(sessionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            GuessResultDto result = gameService.processGuess(guessRequest(GuessType.HIGHER));

            // Equal volumes + HIGHER guess = correct
            assertThat(result.isCorrect()).isTrue();
        }

        @Test
        @DisplayName("should throw GameSessionNotFoundException for unknown session ID")
        void shouldThrow_whenSessionNotFound() {
            given(sessionRepository.findByIdAndStatus("session-1", GameStatus.ACTIVE))
                .willReturn(Optional.empty());

            assertThatThrownBy(() -> gameService.processGuess(guessRequest(GuessType.HIGHER)))
                .isInstanceOf(GameSessionNotFoundException.class);
        }

        @Test
        @DisplayName("should throw InvalidGuessException when challenge item not in seen list")
        void shouldThrow_whenChallengeItemNotInSeenList() {
            // Only item-1 is in seenItemIds — item-2 was never shown
            GameSession sessionWithoutChallenge = GameSession.builder()
                .id("session-1").playerName("Alice")
                .currentItemId("item-1").score(0).status(GameStatus.ACTIVE).build();
            sessionWithoutChallenge.addSeenItemId("item-1"); // item-2 NOT added

            given(sessionRepository.findByIdAndStatus("session-1", GameStatus.ACTIVE))
                .willReturn(Optional.of(sessionWithoutChallenge));
            given(itemRepository.findById("item-1")).willReturn(Optional.of(knownItem));
            given(itemRepository.findById("item-2")).willReturn(Optional.of(challengeItem));

            assertThatThrownBy(() -> gameService.processGuess(guessRequest(GuessType.HIGHER)))
                .isInstanceOf(InvalidGuessException.class)
                .hasMessageContaining("not part of the current session");
        }
    }
}
