package com.higherlower.game.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body for {@code POST /api/players/name}.
 *
 * @param name       2–20 chars after trimming: letters, digits, space, {@code . _ -}
 * @param guestToken per-device secret; required unless a Bearer JWT is sent
 * @param sessionId  optional finished session whose leaderboard row should be renamed
 */
public record ClaimNameRequest(
    @NotNull(message = "Name is required")
    @Pattern(regexp = "\\s*[A-Za-z0-9._-][A-Za-z0-9._ -]{0,18}[A-Za-z0-9._-]\\s*",
             message = "Name must be 2-20 letters, digits, spaces, '.', '_' or '-'")
    String name,

    @Size(min = 16, max = 128, message = "Guest token must be 16-128 characters")
    String guestToken,

    String sessionId
) {}
