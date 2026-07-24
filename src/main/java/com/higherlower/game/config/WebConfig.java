package com.higherlower.game.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Global CORS configuration for the Higher-or-Lower REST API.
 *
 * <p>During development {@code allowedOriginPatterns} is permissive ("*").
 * In production this should be locked down to the actual frontend domain(s):
 * <pre>
 *   config.setAllowedOriginPatterns(List.of("https://yourgame.com"));
 * </pre></p>
 *
 * <p>Note: {@code @CrossOrigin} annotations on individual controllers are
 * redundant when this filter is active, but kept as a fail-safe.</p>
 */
@Configuration
public class WebConfig {

    /**
     * Registers a global CORS filter applied before any Spring Security
     * filter (if security is added later).
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Origins — lock this down in production
        config.setAllowedOriginPatterns(List.of("https://search-wars.vercel.app", "https://search-wars-*.vercel.app", "http://localhost:5173"));

        // Standard REST methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers the browser is allowed to send
        config.setAllowedHeaders(List.of("*"));

        // Expose headers the browser can read (useful for auth tokens later)
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));

        // Allow cookies/credentials (needed if session auth is added later)
        config.setAllowCredentials(false);

        // Cache preflight for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}


