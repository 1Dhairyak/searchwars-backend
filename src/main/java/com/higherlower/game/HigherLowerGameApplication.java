package com.higherlower.game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Entry point for the Higher Lower Game backend.
 *
 * <p>Bootstraps the Spring context, auto-configures JPA (PostgreSQL),
 * enables simple in-process caching for leaderboard reads, and
 * exposes REST endpoints under /api/**.</p>
 */
@SpringBootApplication
@EnableCaching
public class HigherLowerGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(HigherLowerGameApplication.class, args);
    }
}
