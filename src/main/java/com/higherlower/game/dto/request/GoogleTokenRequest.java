package com.higherlower.game.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleTokenRequest(
    @NotBlank(message = "idToken is required") String idToken,
    @Size(min = 16, max = 128, message = "Guest token must be 16-128 characters") String guestToken
) {}
