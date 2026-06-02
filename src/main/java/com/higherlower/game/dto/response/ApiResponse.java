package com.higherlower.game.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Generic API response envelope used by every endpoint.
 *
 * <p>Wrapping all responses in a consistent structure means the
 * frontend can rely on a single schema regardless of which endpoint
 * it calls — success, validation error, or server fault.</p>
 *
 * <pre>
 * {
 *   "success": true,
 *   "message": "Game started successfully",
 *   "data": { ... },
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 * </pre>
 *
 * @param <T> the type of the {@code data} payload
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // omit null fields (e.g. data on errors)
public class ApiResponse<T> {

    /** {@code true} for 2xx responses; {@code false} for errors. */
    boolean success;

    /** Human-readable status message. */
    String message;

    /**
     * The actual payload — null on error responses.
     * {@code @JsonInclude(NON_NULL)} ensures the key is omitted entirely
     * rather than serialised as {@code "data": null}.
     */
    T data;

    /** Server-side timestamp for debugging and log correlation. */
    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();

    // ── Factory helpers ───────────────────────────────────────────────────────

    /** Creates a success response with a payload. */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .build();
    }

    /** Creates a success response with no payload (e.g. delete operations). */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .build();
    }

    /** Creates an error response (no data payload). */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .build();
    }
}
