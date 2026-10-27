package com.higherlower.game.util;

import java.util.Map;

/**
 * Maps an item title to its English Wikipedia article. Most titles are the
 * article name already; these are the exceptions (disambiguation, redirects,
 * diacritics). The pageviews API counts the exact article only — it does not
 * follow redirects — so every value here must be the canonical title.
 */
public final class WikiArticles {

    private WikiArticles() {}

    static final Map<String, String> OVERRIDES = Map.ofEntries(
        Map.entry("Demon Slayer", "Demon Slayer: Kimetsu no Yaiba"),
        Map.entry("Apple", "Apple Inc."),
        Map.entry("Nike", "Nike, Inc."),
        Map.entry("Drake", "Drake (musician)"),
        Map.entry("Corn Flakes", "Corn flakes"),
        Map.entry("Dominos", "Domino's"),
        Map.entry("Lays", "Lay's"),
        Map.entry("McDonald's Big Mac", "Big Mac"),
        Map.entry("Candy Crush", "Candy Crush Saga"),
        Map.entry("FIFA", "FIFA (video game series)"),
        Map.entry("GTA 5", "Grand Theft Auto V"),
        Map.entry("Pokemon", "Pokémon"),
        Map.entry("PUBG", "PUBG: Battlegrounds"),
        Map.entry("Baby Yoda", "Grogu"),
        Map.entry("Distracted Boyfriend", "Distracted boyfriend"),
        Map.entry("Doge", "Doge (meme)"),
        Map.entry("Rickroll", "Rickrolling"),
        Map.entry("SpongeBob", "SpongeBob SquarePants"),
        Map.entry("Avengers", "Avengers (Marvel Cinematic Universe)"),
        Map.entry("Black Panther", "Black Panther (film)"),
        Map.entry("Fast and Furious", "Fast & Furious"),
        Map.entry("Titanic", "Titanic (1997 film)"),
        Map.entry("Kylian Mbappe", "Kylian Mbappé"),
        Map.entry("NBA", "National Basketball Association"),
        Map.entry("Amazon", "Amazon (company)"),
        Map.entry("iPhone", "IPhone"),
        Map.entry("Tesla", "Tesla, Inc."),
        Map.entry("Twitter", "X (social network)")
    );

    public static String forTitle(String title) {
        return OVERRIDES.getOrDefault(title, title);
    }
}
