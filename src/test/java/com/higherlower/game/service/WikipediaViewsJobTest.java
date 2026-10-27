package com.higherlower.game.service;

import com.higherlower.game.entity.Item;
import com.higherlower.game.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WikipediaViewsJobTest {

    final ItemRepository repo = mock(ItemRepository.class);
    final WikipediaViewsClient wikipedia = mock(WikipediaViewsClient.class);
    final WikipediaViewsJob job = new WikipediaViewsJob(repo, wikipedia);

    final LocalDateTime now = LocalDateTime.of(2026, 10, 27, 3, 0);
    final YearMonth lastMonth = YearMonth.of(2026, 9);

    Item kohli, pokemon, missing;

    @BeforeEach
    void setUp() {
        job.clock = Clock.fixed(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        kohli   = Item.builder().title("Virat Kohli").monthlyViews(1L).active(true).build();
        pokemon = Item.builder().title("Pokemon").monthlyViews(2L).active(true).build();
        missing = Item.builder().title("Gone Page").monthlyViews(3L).active(true).build();
        when(repo.findByActiveTrue()).thenReturn(List.of(kohli, pokemon, missing));
    }

    @Test
    @DisplayName("updates last month's views via the mapped article; missing or failing items keep their value")
    void refreshUpdatesEachItemIndependently() {
        when(wikipedia.monthlyViews("Virat Kohli", lastMonth)).thenReturn(OptionalLong.of(315_030));
        when(wikipedia.monthlyViews("Pokémon", lastMonth)).thenThrow(new IllegalStateException("boom"));
        when(wikipedia.monthlyViews("Gone Page", lastMonth)).thenReturn(OptionalLong.empty());

        assertThat(job.refresh()).isEqualTo(1);

        assertThat(kohli.getMonthlyViews()).isEqualTo(315_030L);
        assertThat(kohli.getViewsRefreshedAt()).isEqualTo(now);
        assertThat(pokemon.getMonthlyViews()).isEqualTo(2L);
        assertThat(missing.getMonthlyViews()).isEqualTo(3L);
        verify(repo).save(kohli);
        verify(repo, never()).save(pokemon);
        verify(repo, never()).save(missing);
    }

    @Test
    @DisplayName("startup refresh is skipped when every item was refreshed recently")
    void startupSkipsWhenFresh() {
        List.of(kohli, pokemon, missing).forEach(i -> i.setViewsRefreshedAt(now.minusHours(2)));
        job.refreshIfStale();
        verifyNoInteractions(wikipedia);

        missing.setViewsRefreshedAt(null); // one never refreshed → stale
        when(wikipedia.monthlyViews(anyString(), eq(lastMonth))).thenReturn(OptionalLong.of(7));
        job.refreshIfStale();
        verify(wikipedia, times(3)).monthlyViews(anyString(), eq(lastMonth));
    }
}
