package com.higherlower.game.service.impl;

import com.higherlower.game.dto.request.GuessRequest;
import com.higherlower.game.dto.request.StartGameRequest;
import com.higherlower.game.dto.response.GameRoundDto;
import com.higherlower.game.dto.response.GuessResultDto;
import com.higherlower.game.dto.response.ItemDto;
import com.higherlower.game.entity.GameSession;
import com.higherlower.game.entity.Item;
import com.higherlower.game.entity.enums.GameStatus;
import com.higherlower.game.entity.enums.GuessType;
import com.higherlower.game.exception.GameSessionNotFoundException;
import com.higherlower.game.exception.InsufficientItemsException;
import com.higherlower.game.exception.InvalidGuessException;
import com.higherlower.game.repository.GameSessionRepository;
import com.higherlower.game.repository.ItemRepository;
import com.higherlower.game.service.GameService;
import com.higherlower.game.service.LeaderboardService;
import com.higherlower.game.util.ItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Core game-loop implementation.
 *
 * <p>Threading note: each HTTP request gets its own transaction so
 * concurrent sessions are fully isolated at the DB level.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceImpl implements GameService {

    /** Minimum active items required to start a game. */
    private static final int MIN_ITEMS_REQUIRED = 2;

    /** Fallback display name for sessions without a player name. */
    private static final String ANONYMOUS = "Anonymous";

    private final ItemRepository       itemRepository;
    private final GameSessionRepository sessionRepository;
    private final LeaderboardService   leaderboardService;
    private final ItemMapper           itemMapper;

    // ── Start Game ────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Guard: at least {@value MIN_ITEMS_REQUIRED} active items must exist.</li>
     *   <li>Pick two distinct random items from the active pool.</li>
     *   <li>Create and persist a new {@link GameSession}.</li>
     *   <li>Return the first {@link GameRoundDto} with the known item's
     *       volume revealed and the challenge item's volume hidden.</li>
     * </ol></p>
     */
    @Override
    @Transactional
    public GameRoundDto startGame(StartGameRequest request) {
        log.info("Starting new game session for player: {}",
            request != null ? request.getPlayerName() : ANONYMOUS);

        // ── 1. Guard: ensure enough items exist in the pool ──────────────────
        long activeCount = itemRepository.countByActiveTrue();
        if (activeCount < MIN_ITEMS_REQUIRED) {
            throw new InsufficientItemsException(
                "Not enough active items to start a game. Found: " + activeCount
                    + ", required: " + MIN_ITEMS_REQUIRED);
        }

        // ── 2. Select the first (known) item at random ───────────────────────
        Item knownItem = itemRepository.findRandomActiveItem()
            .orElseThrow(() -> new InsufficientItemsException("Failed to load starting item"));

        // ── 3. Select the challenge item, excluding the known item ────────────
        Item challengeItem = itemRepository
            .findRandomActiveItemExcluding(List.of(knownItem.getId()))
            .orElseThrow(() -> new InsufficientItemsException(
                "Failed to load a distinct challenge item"));

        // ── 4. Resolve player name ────────────────────────────────────────────
        String playerName = resolvePlayerName(request);

        // ── 5. Persist the session ────────────────────────────────────────────
        GameSession session = GameSession.builder()
            .playerName(playerName)
            .currentItemId(knownItem.getId())
            .score(0)
            .status(GameStatus.ACTIVE)
            .build();

        // Mark both items as seen to prevent them from appearing again together
        session.addSeenItemId(knownItem.getId());
        session.addSeenItemId(challengeItem.getId());

        session = sessionRepository.save(session);
        log.debug("Created session [{}] for player '{}'. Known: '{}', Challenge: '{}'",
            session.getId(), playerName, knownItem.getTitle(), challengeItem.getTitle());

        // ── 6. Build and return the first round ──────────────────────────────
        return buildRound(session, knownItem, challengeItem);
    }

    // ── Process Guess ─────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Load and validate the active session.</li>
     *   <li>Load the known and challenge items.</li>
     *   <li>Validate the challenge item ID matches session state.</li>
     *   <li>Evaluate correctness: HIGHER if challenge >= known, LOWER otherwise.
     *       Ties are treated as HIGHER (player-friendly).</li>
     *   <li>Correct → increment score, select next challenge item, advance session.</li>
     *   <li>Wrong  → mark GAME_OVER, persist leaderboard entry.</li>
     * </ol></p>
     */
    @Override
    @Transactional
    public GuessResultDto processGuess(GuessRequest request) {
        log.info("Processing guess for session [{}]: {} on item [{}]",
            request.getSessionId(), request.getGuess(), request.getChallengeItemId());

        // ── 1. Load and validate the session ─────────────────────────────────
        GameSession session = sessionRepository
            .findByIdAndStatus(request.getSessionId(), GameStatus.ACTIVE)
            .orElseThrow(() -> new GameSessionNotFoundException(
                "No active session found with ID: " + request.getSessionId()));

        // ── 2. Load the known item (left card) ────────────────────────────────
        Item knownItem = itemRepository.findById(session.getCurrentItemId())
            .orElseThrow(() -> new GameSessionNotFoundException(
                "Known item not found for session: " + session.getId()));

        // ── 3. Load the challenge item (right card) ───────────────────────────
        Item challengeItem = itemRepository.findById(request.getChallengeItemId())
            .orElseThrow(() -> new InvalidGuessException(
                "Challenge item not found: " + request.getChallengeItemId()));

        // ── 4. Cross-check: challenge ID must be in the session's seen list ───
        // The seen list is the source of truth for what the client was shown.
        // This prevents replay attacks from a stale browser tab.
        if (!session.getSeenItemIdList().contains(challengeItem.getId())) {
            throw new InvalidGuessException(
                "Challenge item [" + challengeItem.getId()
                    + "] was not part of the current session round.");
        }

        // ── 5. Evaluate the guess ─────────────────────────────────────────────
        // Ties (equal volume) count as HIGHER — better UX than losing on a tie.
        boolean isCorrect = evaluateGuess(request.getGuess(), knownItem, challengeItem);

        log.debug("Guess evaluation — known: {} ({}), challenge: {} ({}), guess: {}, correct: {}",
            knownItem.getTitle(), knownItem.getSearchVolume(),
            challengeItem.getTitle(), challengeItem.getSearchVolume(),
            request.getGuess(), isCorrect);

        if (isCorrect) {
            return handleCorrectGuess(session, knownItem, challengeItem);
        } else {
            return handleWrongGuess(session, knownItem, challengeItem);
        }
    }

    // ── Private: Correct Guess Flow ───────────────────────────────────────────

    /**
     * Increments the score, selects the next challenge item (preventing
     * duplicates), persists the updated session, and builds the response.
     */
    private GuessResultDto handleCorrectGuess(GameSession session,
                                               Item knownItem,
                                               Item challengeItem) {
        // Increment score
        session.setScore(session.getScore() + 1);

        // The challenge item now becomes the new known item
        session.setCurrentItemId(challengeItem.getId());

        // Select the next challenge item, excluding everything seen so far
        List<String> seenIds = session.getSeenItemIdList();
        Item nextChallenge = itemRepository.findRandomActiveItemExcluding(seenIds)
            .orElse(null);

        GameRoundDto nextRound = null;
        if (nextChallenge != null) {
            // Add the new challenge to the seen list
            session.addSeenItemId(nextChallenge.getId());
            nextRound = buildRound(session, challengeItem, nextChallenge);
        }
        // If nextChallenge is null, the player has exhausted all items — rare but handled.

        sessionRepository.save(session);

        String message = String.format("Correct! %s has %,d monthly searches. Score: %d",
            challengeItem.getTitle(),
            challengeItem.getSearchVolume(),
            session.getScore());

        return GuessResultDto.builder()
            .correct(true)
            .score(session.getScore())
            .revealedChallengeItem(itemMapper.toDto(challengeItem))  // volume revealed
            .nextRound(nextRound)
            .message(message)
            .gameOver(nextRound == null)
            .build();
    }

    /**
     * Marks the session as GAME_OVER, persists the leaderboard entry,
     * and builds the failure response (no next round).
     */
    private GuessResultDto handleWrongGuess(GameSession session,
                                             Item knownItem,
                                             Item challengeItem) {
        session.markGameOver();
        sessionRepository.save(session);

        // Persist the score to the leaderboard (idempotent)
        leaderboardService.recordScore(
            session.getId(),
            session.getPlayerName(),
            session.getScore()
        );

        String message = String.format(
            "Wrong! %s has %,d monthly searches — %s %s than %s's %,d. Game over!",
            challengeItem.getTitle(),
            challengeItem.getSearchVolume(),
            challengeItem.getTitle(),
            challengeItem.getSearchVolume() >= knownItem.getSearchVolume() ? "higher" : "lower",
            knownItem.getTitle(),
            knownItem.getSearchVolume()
        );

        log.info("Game over for session [{}] — final score: {}", session.getId(), session.getScore());

        return GuessResultDto.builder()
            .correct(false)
            .score(session.getScore())
            .revealedChallengeItem(itemMapper.toDto(challengeItem))
            .nextRound(null)
            .message(message)
            .gameOver(true)
            .build();
    }

    // ── Private: Helpers ──────────────────────────────────────────────────────

    /**
     * Core guess evaluation logic.
     *
     * <p>Returns {@code true} when:
     * <ul>
     *   <li>{@code HIGHER} and challenge volume &ge; known volume (ties favour player)</li>
     *   <li>{@code LOWER}  and challenge volume &lt; known volume</li>
     * </ul></p>
     */
    private boolean evaluateGuess(GuessType guess, Item knownItem, Item challengeItem) {
        long knownVol     = knownItem.getSearchVolume();
        long challengeVol = challengeItem.getSearchVolume();

        return switch (guess) {
            case HIGHER -> challengeVol >= knownVol;   // ties: player-friendly
            case LOWER  -> challengeVol < knownVol;
        };
    }

    /**
     * Assembles a {@link GameRoundDto} with the challenge item's volume hidden.
     *
     * <p>The challenge item DTO deliberately has {@code searchVolume = null}
     * so that even if a client inspects the raw API response, the volume
     * is not leaked before the guess is submitted.</p>
     */
    private GameRoundDto buildRound(GameSession session,
                                    Item knownItem,
                                    Item challengeItem) {
        // Known item — full DTO including search volume
        ItemDto knownDto = itemMapper.toDto(knownItem);

        // Challenge item — volume HIDDEN (set to null)
        ItemDto challengeDto = ItemDto.builder()
            .id(challengeItem.getId())
            .title(challengeItem.getTitle())
            .imageUrl(challengeItem.getImageUrl())
            .category(challengeItem.getCategory())
            .searchVolume(null)           // deliberately omitted
            .build();

        return GameRoundDto.builder()
            .sessionId(session.getId())
            .knownItem(knownDto)
            .challengeItem(challengeDto)
            .currentScore(session.getScore())
            .build();
    }

    /**
     * Resolves the player name from the request, normalising blanks
     * and nulls to the "Anonymous" fallback.
     */
    private String resolvePlayerName(StartGameRequest request) {
        if (request == null || request.getPlayerName() == null
                || request.getPlayerName().isBlank()) {
            return ANONYMOUS;
        }
        return request.getPlayerName().trim();
    }
}
