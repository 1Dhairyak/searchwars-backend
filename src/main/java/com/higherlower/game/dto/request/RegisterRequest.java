package com.higherlower.game.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Email is required") @Email(message = "Email is invalid") String email,
    @NotBlank(message = "Password is required") @Size(min = 6, max = 72, message = "Password must be 6-72 characters") String password,
    @Size(max = 20, message = "Display name must not exceed 20 characters") String displayName,
    @Size(min = 16, max = 128, message = "Guest token must be 16-128 characters") String guestToken
) {}
