package com.higherlower.game.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Email is required") @Email(message = "Email is invalid") String email,
    @NotBlank(message = "Password is required") @Size(max = 72, message = "Password must not exceed 72 characters") String password,
    @Size(min = 16, max = 128, message = "Guest token must be 16-128 characters") String guestToken
) {}
