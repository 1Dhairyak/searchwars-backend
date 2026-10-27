package com.higherlower.game.service;

import com.higherlower.game.entity.Item;
import com.higherlower.game.repository.ItemRepository;
import com.higherlower.game.util.WikiArticles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Keeps every active item's {@code monthlyViews} at last month's real
 * Wikipedia page views.
 *
 * <p>Runs nightly, and at startup when the data is stale: Render's free tier
 * sleeps when idle, so the nightly trigger alone would often be missed.
 * Each item is updated independently — one failing article never blocks the
 * rest, and a failed item keeps its previous value. Re-running is harmless.</p>
 */
@Component
@ConditionalOnProperty(name = "wiki.refresh.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class WikipediaViewsJob {

    static final Duration STALE_AFTER = Duration.ofHours(20);

    private final ItemRepository       itemRepository;
    private final WikipediaViewsClient wikipedia;
    Clock clock = Clock.systemUTC(); // tests pin the time

    private final AtomicBoolean running = new AtomicBoolean();

    @EventListener(ApplicationReadyEvent.class)
    public void refreshOnStartup() {
        // Off the main thread so the app starts serving immediately.
        Thread.ofVirtual().name("wiki-views-startup").start(this::refreshIfStale);
    }

    @Scheduled(cron = "${wiki.refresh.cron}", zone = "UTC")
    public void nightly() {
        // ~100 sequential HTTP calls: keep them off the shared scheduler thread (it also runs the duel ticks).
        Thread.ofVirtual().name("wiki-views-nightly").start(this::refresh);
    }

    void refreshIfStale() {
        LocalDateTime cutoff = LocalDateTime.now(clock).minus(STALE_AFTER);
        boolean stale = itemRepository.findByActiveTrue().stream()
            .anyMatch(i -> i.getViewsRefreshedAt() == null || i.getViewsRefreshedAt().isBefore(cutoff));
        if (stale) refresh();
        else log.info("Wikipedia views are fresh; skipping startup refresh");
    }

    /** @return number of items updated */
    int refresh() {
        if (!running.compareAndSet(false, true)) {
            log.info("Wikipedia views refresh already running; skipping");
            return 0;
        }
        try {
            YearMonth month = YearMonth.now(clock).minusMonths(1); // last complete month
            List<Item> items = itemRepository.findByActiveTrue();
            int updated = 0, missing = 0, failed = 0;

            for (Item item : items) {
                String article = WikiArticles.forTitle(item.getTitle());
                try {
                    OptionalLong views = wikipedia.monthlyViews(article, month);
                    if (views.isEmpty()) {
                        missing++;
                        log.warn("No Wikipedia views for '{}' (article '{}'); keeping {}",
                            item.getTitle(), article, item.getMonthlyViews());
                        continue;
                    }
                    item.setMonthlyViews(views.getAsLong());
                    item.setViewsRefreshedAt(LocalDateTime.now(clock));
                    itemRepository.save(item);
                    updated++;
                } catch (RuntimeException e) {
                    failed++;
                    log.warn("Wikipedia views refresh failed for '{}': {}", item.getTitle(), e.toString());
                }
            }

            log.info("Wikipedia views for {}: {} updated, {} missing, {} failed (of {})",
                month, updated, missing, failed, items.size());
            return updated;
        } finally {
            running.set(false);
        }
    }
}
