package com.higherlower.game.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.YearMonth;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class WikipediaViewsClientTest {

    MockRestServiceServer server;
    WikipediaViewsClient client;
    final YearMonth sept = YearMonth.of(2026, 9);

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new WikipediaViewsClient(builder);
        client.retryDelay = Duration.ZERO;
    }

    @Test
    @DisplayName("asks for the whole month of the article (spaces → underscores) and returns its views")
    void parsesMonthlyViews() {
        server.expect(requestTo(WikipediaViewsClient.BASE_URL + "/Virat_Kohli/monthly/20260901/20260930"))
            .andExpect(header("User-Agent", containsString("SearchWars")))
            .andRespond(withSuccess("""
                {"items":[{"project":"en.wikipedia","article":"Virat_Kohli","granularity":"monthly",
                           "timestamp":"2026090100","access":"all-access","agent":"user","views":315030}]}
                """, MediaType.APPLICATION_JSON));

        assertThat(client.monthlyViews("Virat Kohli", sept)).hasValue(315030);
        server.verify();
    }

    @Test
    @DisplayName("a missing article is empty, not an error")
    void notFoundIsEmpty() {
        server.expect(requestTo(containsString("/No_Such_Page/"))).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.monthlyViews("No Such Page", sept)).isEqualTo(OptionalLong.empty());
    }

    @Test
    @DisplayName("retries a temporary 503, then gives up after 3 attempts")
    void retriesServerErrors() {
        server.expect(requestTo(containsString("/Naruto/"))).andRespond(withServiceUnavailable());
        server.expect(requestTo(containsString("/Naruto/")))
            .andRespond(withSuccess("{\"items\":[{\"views\":42}]}", MediaType.APPLICATION_JSON));
        assertThat(client.monthlyViews("Naruto", sept)).hasValue(42);

        server.reset();
        server.expect(times(3), requestTo(containsString("/Naruto/"))).andRespond(withServiceUnavailable());
        assertThatThrownBy(() -> client.monthlyViews("Naruto", sept)).isNotNull();
        server.verify();
    }
}
