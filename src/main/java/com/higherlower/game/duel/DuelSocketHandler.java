package com.higherlower.game.duel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.higherlower.game.dto.response.ItemDto;
import com.higherlower.game.entity.enums.GuessType;
import com.higherlower.game.service.PlayerNameService;
import com.higherlower.game.util.IndianNames;
import com.higherlower.game.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code /ws/duel}: JSON messages over a plain WebSocket.
 *
 * <pre>
 * client → server   {"type":"join", "duelId", "guestToken", "authToken"?}
 *                   {"type":"guess", "guess":"HIGHER"|"LOWER"}
 *                   {"type":"rematch"}
 * server → client   {"type":"state", ...}   after every change: both scores live, plus only
 *                                           this player's own round (challenge views hidden)
 *                   {"type":"result", ...}  to the guesser: right/wrong + the revealed item
 *                   {"type":"error", "message"}
 * </pre>
 *
 * Identity travels in the first message, not the URL, so tokens never reach access logs.
 * Every state message is complete, so a reconnecting client just joins again.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DuelSocketHandler extends TextWebSocketHandler {

    private static final int SEND_TIMEOUT_MS = 5_000;
    private static final int SEND_BUFFER_BYTES = 64 * 1024;
    /** Close code telling a client another tab took its seat (so it must not auto-reconnect). */
    static final CloseStatus REPLACED = new CloseStatus(4000, "Opened in another tab");

    private final DuelService duels;
    private final JwtUtil jwtUtil;
    private final PlayerNameService playerNames;
    private final ObjectMapper json;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>(); // by session id
    private final Map<String, Seat> seats = new ConcurrentHashMap<>();                // by session id
    private final Map<String, WebSocketSession[]> sockets = new ConcurrentHashMap<>(); // by duel id, per slot

    private record Seat(String duelId, int slot) {}

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Ticks and the other player's actions send to this socket concurrently.
        sessions.put(session.getId(), new ConcurrentWebSocketSessionDecorator(session, SEND_TIMEOUT_MS, SEND_BUFFER_BYTES));
    }

    @Override
    protected void handleTextMessage(WebSocketSession raw, TextMessage message) throws IOException {
        WebSocketSession session = sessions.get(raw.getId());
        try {
            JsonNode msg = json.readTree(message.getPayload());
            switch (msg.path("type").asText()) {
                case "join"    -> join(session, msg);
                case "guess"   -> guess(session, msg);
                case "rematch" -> rematch(session);
                default        -> throw new DuelException("Unknown message type");
            }
        } catch (DuelException e) {
            send(session, Map.of("type", "error", "message", e.getMessage()));
        } catch (IOException | IllegalArgumentException e) {
            send(session, Map.of("type", "error", "message", "Bad message"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession raw, CloseStatus status) {
        sessions.remove(raw.getId());
        Seat seat = seats.remove(raw.getId());
        if (seat == null) return;
        try {
            Duel duel = duels.get(seat.duelId());
            WebSocketSession[] slots = sockets.get(duel.id);
            synchronized (duel) {
                // Only the slot's current socket counts; an older tab closing is irrelevant.
                if (slots == null || slots[seat.slot()] == null || !slots[seat.slot()].getId().equals(raw.getId())) return;
                slots[seat.slot()] = null;
                duel.disconnected(seat.slot(), duels.now());
            }
            broadcast(duel);
        } catch (DuelException expired) {
            // duel already gone
        }
    }

    // ── Messages ─────────────────────────────────────────────────────────────

    private void join(WebSocketSession session, JsonNode msg) {
        String owner = ownerOf(msg);
        Duel duel = duels.get(msg.path("duelId").asText(null));
        WebSocketSession replaced;
        synchronized (duel) {
            int slot = duel.join(owner, playerNames.nameOf(owner).orElseGet(IndianNames::random), duels.now());
            WebSocketSession[] slots = sockets.computeIfAbsent(duel.id, id -> new WebSocketSession[2]);
            replaced = slots[slot];
            slots[slot] = session;
            seats.put(session.getId(), new Seat(duel.id, slot));
        }
        if (replaced != null && !replaced.getId().equals(session.getId())) {
            seats.remove(replaced.getId());   // same player opened another tab: the newest one plays
            closeQuietly(replaced);
        }
        broadcast(duel);
    }

    private void guess(WebSocketSession session, JsonNode msg) {
        Seat seat = seatOf(session);
        Duel duel = duels.get(seat.duelId());
        GuessType guess = GuessType.valueOf(msg.path("guess").asText(""));
        Duel.GuessResult result;
        synchronized (duel) {
            result = duel.guess(seat.slot(), guess, duels.now());
        }
        send(session, Map.of("type", "result", "correct", result.correct(),
            "revealed", result.revealed(), "score", result.score()));
        broadcast(duel);
    }

    private void rematch(WebSocketSession session) {
        Duel duel = duels.get(seatOf(session).duelId());
        duels.rematch(duel);
        broadcast(duel); // carries rematchId; both clients then join it
    }

    @Scheduled(fixedRate = 1000)
    void tick() {
        duels.tick().forEach(this::broadcast);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String ownerOf(JsonNode msg) {
        String authToken = msg.path("authToken").asText(null);
        if (authToken != null) {
            var userId = jwtUtil.validate(authToken);
            if (userId.isPresent()) return PlayerNameService.userOwner(userId.get());
        }
        String owner = PlayerNameService.guestOwner(msg.path("guestToken").asText(null));
        if (owner == null) throw new DuelException("Missing player identity");
        return owner;
    }

    private Seat seatOf(WebSocketSession session) {
        Seat seat = seats.get(session.getId());
        if (seat == null) throw new DuelException("Join a duel first");
        return seat;
    }

    private void broadcast(Duel duel) {
        WebSocketSession[] slots = sockets.get(duel.id);
        if (slots == null) return;
        for (int slot = 0; slot < 2; slot++) {
            State state;
            WebSocketSession target;
            synchronized (duel) {
                target = slots[slot];
                if (target == null) continue;
                state = stateFor(duel, slot, duels.now());
            }
            send(target, state);
        }
    }

    /** One player as everyone sees them. */
    record PlayerView(String name, int score, boolean out, String outReason, boolean connected) {}

    /** Everything one player may see: both players publicly, but only their own round. */
    record State(String type, String duelId, Duel.Status status, int you, List<PlayerView> players,
                 Instant serverTime, Instant startsAt, Integer winner, String rematchId,
                 ItemDto known, ItemDto challenge, Instant deadline) {}

    static State stateFor(Duel duel, int you, Instant now) {
        List<PlayerView> players = new ArrayList<>();
        for (Duel.Player p : duel.players) {
            if (p != null) players.add(new PlayerView(p.name, p.score, p.out, p.outReason, p.connected));
        }
        ItemDto[] round = duel.roundFor(you);
        return new State("state", duel.id, duel.status, you, players, now, duel.startsAt, duel.winner, duel.rematchId,
            round == null ? null : round[0],
            round == null ? null : round[1],
            round == null ? null : duel.player(you).deadline);
    }

    private void send(WebSocketSession session, Object payload) {
        if (session == null || !session.isOpen()) return;
        try {
            session.sendMessage(new TextMessage(json.writeValueAsString(payload)));
        } catch (IOException | IllegalStateException e) {
            log.debug("Duel send failed: {}", e.toString());
        }
    }

    private static void closeQuietly(WebSocketSession session) {
        try {
            session.close(REPLACED);
        } catch (IOException ignored) {
            // already closed
        }
    }
}
