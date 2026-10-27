package com.higherlower.game.duel;

import com.higherlower.game.config.WebConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class DuelWebSocketConfig implements WebSocketConfigurer {

    private final DuelSocketHandler handler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/duel")
            .setAllowedOriginPatterns(WebConfig.ALLOWED_ORIGINS.toArray(String[]::new));
    }
}
