package com.higherlower.game.config;

import com.higherlower.game.entity.Item;
import com.higherlower.game.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with sample game items on application startup.
 *
 * <p>Only active on non-production profiles ({@code !prod}) to prevent
 * accidental data insertion in live environments.
 * Run with {@code --spring.profiles.active=dev} or simply no profile
 * during development.</p>
 *
 * <p>Each item's {@code searchVolume} is an approximate average monthly
 * Google search volume (illustrative values — replace with real data).</p>
 */
@Component
@Profile({"dev", "default", "prod"})
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ItemRepository itemRepository;

    @Override
    public void run(String... args) {
        if (itemRepository.countByActiveTrue() > 0) {
            log.info("DataSeeder: items already present — skipping seed.");
            return;
        }

        log.info("DataSeeder: seeding sample items...");

        List<Item> items = List.of(

            // ── Food & Drink ──────────────────────────────────────────────────
            Item.builder()
                .title("Budweiser")
                .imageUrl("https://images.unsplash.com/photo-1608270586620-248524c67de9?w=800")
                .searchVolume(301_000L)
                .category("Food & Drink")
                .build(),
            Item.builder()
                .title("Corn Flakes")
                .imageUrl("https://images.unsplash.com/photo-1562155618-e1a8da39bf00?w=800")
                .searchVolume(450_000L)
                .category("Food & Drink")
                .build(),
            Item.builder()
                .title("Coca-Cola")
                .imageUrl("https://images.unsplash.com/photo-1554866585-cd94860890b7?w=800")
                .searchVolume(9_140_000L)
                .category("Food & Drink")
                .build(),
            Item.builder()
                .title("McDonald's")
                .imageUrl("https://images.unsplash.com/photo-1572802419224-296b0aeee0d9?w=800")
                .searchVolume(13_600_000L)
                .category("Food & Drink")
                .build(),

            // ── Technology ────────────────────────────────────────────────────
            Item.builder()
                .title("iPhone")
                .imageUrl("https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=800")
                .searchVolume(20_400_000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("PlayStation 5")
                .imageUrl("https://images.unsplash.com/photo-1606813907291-d86efa9b94db?w=800")
                .searchVolume(5_000_000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Netflix")
                .imageUrl("https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=800")
                .searchVolume(110_000_000L)
                .category("Technology")
                .build(),

            // ── Sports ────────────────────────────────────────────────────────
            Item.builder()
                .title("Cristiano Ronaldo")
                .imageUrl("https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800")
                .searchVolume(37_200_000L)
                .category("Sports")
                .build(),
            Item.builder()
                .title("NBA")
                .imageUrl("https://images.unsplash.com/photo-1546519638-68e109498ffc?w=800")
                .searchVolume(40_500_000L)
                .category("Sports")
                .build(),

            // ── Entertainment ─────────────────────────────────────────────────
            Item.builder()
                .title("Taylor Swift")
                .imageUrl("https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=800")
                .searchVolume(20_400_000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("The Beatles")
                .imageUrl("https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=800")
                .searchVolume(4_090_000L)
                .category("Entertainment")
                .build()
        );

        itemRepository.saveAll(items);
        log.info("DataSeeder: {} items seeded successfully.", items.size());
    }
}
