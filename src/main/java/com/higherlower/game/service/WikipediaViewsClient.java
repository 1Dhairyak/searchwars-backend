package com.higherlower.game.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.OptionalLong;

/**
 * Wikimedia Pageviews REST API: monthly human (non-bot) views of one English
 * Wikipedia article. Free, no key; Wikimedia asks for a descriptive User-Agent.
 */
@Component
public class WikipediaViewsClient {

    static final String BASE_URL =
        "https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia/all-access/user";
    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd
    private static final int MAX_ATTEMPTS = 3;

    private final RestClient http;
    Duration retryDelay = Duration.ofSeconds(1); // doubled per retry; tests set it to zero

    public WikipediaViewsClient(RestClient.Builder builder) {
        this.http = builder
            .baseUrl(BASE_URL)
            .defaultHeader(HttpHeaders.USER_AGENT, "SearchWars/1.0 (https://search-wars.vercel.app)")
            .build();
    }

    /**
     * Views of {@code article} during {@code month}; empty if Wikipedia has no
     * data for that article (404). Retries rate limits, 5xx and network errors.
     */
    public OptionalLong monthlyViews(String article, YearMonth month) {
        for (int attempt = 1; ; attempt++) {
            try {
                Response body = http.get()
                    .uri("/{article}/monthly/{start}/{end}",
                        article.replace(' ', '_'), DAY.format(month.atDay(1)), DAY.format(month.atEndOfMonth()))
                    .retrieve()
                    .body(Response.class);
                return body == null || body.items().isEmpty()
                    ? OptionalLong.empty()
                    : OptionalLong.of(body.items().get(0).views());
            } catch (HttpClientErrorException.NotFound e) {
                return OptionalLong.empty();
            } catch (HttpClientErrorException.TooManyRequests | HttpServerErrorException | ResourceAccessException e) {
                if (attempt == MAX_ATTEMPTS) throw e;
                sleep(retryDelay.multipliedBy(1L << (attempt - 1)));
            }
        }
    }

    private static void sleep(Duration d) {
        try {
            Thread.sleep(d);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying Wikipedia", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Response(List<Entry> items) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Entry(long views) {}
}
