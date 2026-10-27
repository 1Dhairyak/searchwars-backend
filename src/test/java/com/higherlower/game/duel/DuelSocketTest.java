package com.higherlower.game.duel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.higherlower.game.entity.Item;
import com.higherlower.game.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

/**
 * A whole duel over real WebSockets: create over REST, two clients join, the
 * server times the start, judges guesses and decides the winner.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DuelSocketTest {

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    @Autowired ItemRepository items;
    @Autowired ObjectMapper json;

    final Map<String, Long> views = new HashMap<>();

    @Test
    @DisplayName("two players duel: the one still right after the other is out wins")
    void fullDuel() throws Exception {
        for (int i = 1; i <= 6; i++) {
            Item saved = items.save(Item.builder().title("Item " + i).imageUrl("x").category("c").monthlyViews(i * 1000L).build());
            views.put(saved.getId(), saved.getMonthlyViews());
        }
        String duelId = rest.postForObject("/api/duels", null, JsonNode.class).path("data").path("duelId").asText();

        Client alice = connect(), bob = connect();
        alice.send(Map.of("type", "join", "duelId", duelId, "guestToken", "alice-token-0000000001"));
        alice.await(m -> "WAITING".equals(m.path("status").asText()));
        bob.send(Map.of("type", "join", "duelId", duelId.toLowerCase(), "guestToken", "bob-token-000000000002"));

        JsonNode bobRound = bob.await(m -> "PLAYING".equals(m.path("status").asText()) && m.has("challenge"));
        JsonNode aliceRound = alice.await(m -> "PLAYING".equals(m.path("status").asText()) && m.has("challenge"));
        assertThat(aliceRound.path("challenge").has("monthlyViews")).isFalse();
        assertThat(aliceRound.path("challenge").path("id")).isEqualTo(bobRound.path("challenge").path("id")); // same chain

        Thread.sleep(Duel.START_DELAY.toMillis() + 100);

        bob.send(Map.of("type", "guess", "guess", wrongGuess(bobRound)));
        assertThat(bob.await(m -> "result".equals(m.path("type").asText())).path("correct").asBoolean()).isFalse();

        alice.send(Map.of("type", "guess", "guess", rightGuess(aliceRound)));
        JsonNode end = alice.await(m -> "FINISHED".equals(m.path("status").asText()));
        assertThat(end.path("winner").asInt()).isZero();
        assertThat(end.path("players").get(0).path("score").asInt()).isEqualTo(1);
        assertThat(bob.await(m -> "FINISHED".equals(m.path("status").asText())).path("you").asInt()).isEqualTo(1);

        alice.send(Map.of("type", "guess", "guess", "HIGHER"));
        assertThat(alice.await(m -> "error".equals(m.path("type").asText())).path("message").asText()).isNotBlank();
    }

    private String rightGuess(JsonNode round) {
        long known = views.get(round.path("known").path("id").asText());
        long challenge = views.get(round.path("challenge").path("id").asText());
        return challenge >= known ? "HIGHER" : "LOWER";
    }

    private String wrongGuess(JsonNode round) {
        return "HIGHER".equals(rightGuess(round)) ? "LOWER" : "HIGHER";
    }

    private Client connect() throws Exception {
        Client client = new Client();
        client.session = new StandardWebSocketClient()
            .execute(client, new WebSocketHttpHeaders(), URI.create("ws://localhost:" + port + "/ws/duel"))
            .get(5, TimeUnit.SECONDS);
        return client;
    }

    class Client extends TextWebSocketHandler {
        final BlockingQueue<JsonNode> inbox = new LinkedBlockingQueue<>();
        WebSocketSession session;

        @Override
        protected void handleTextMessage(WebSocketSession s, TextMessage message) throws Exception {
            JsonNode msg = json.readTree(message.getPayload());
            if (msg.has("challenge")) assertThat(msg.path("challenge").has("monthlyViews")).isFalse(); // never leaked
            inbox.add(msg);
        }

        void send(Map<String, String> msg) throws Exception {
            session.sendMessage(new TextMessage(json.writeValueAsString(msg)));
        }

        JsonNode await(Predicate<JsonNode> match) throws InterruptedException {
            long until = System.currentTimeMillis() + 10_000;
            while (System.currentTimeMillis() < until) {
                JsonNode msg = inbox.poll(100, TimeUnit.MILLISECONDS);
                if (msg != null && match.test(msg)) return msg;
            }
            throw new AssertionError("Timed out waiting for a matching message");
        }
    }
}
