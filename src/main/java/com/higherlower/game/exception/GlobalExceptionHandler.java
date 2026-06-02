package com.higherlower.game.exception;

import com.higherlower.game.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * Centralised exception handler for all REST controllers.
 *
 * <p>Translates domain and framework exceptions into consistent
 * {@link ApiResponse} error envelopes so the frontend always
 * receives the same JSON shape regardless of error type.</p>
 *
 * <p>HTTP status mapping:
 * <table border="1">
 *   <tr><th>Exception</th><th>HTTP Status</th></tr>
 *   <tr><td>{@link GameSessionNotFoundException}</td><td>404</td></tr>
 *   <tr><td>{@link InvalidGuessException}</td><td>409</td></tr>
 *   <tr><td>{@link InsufficientItemsException}</td><td>503</td></tr>
 *   <tr><td>Bean validation failures</td><td>400</td></tr>
 *   <tr><td>Unhandled exceptions</td><td>500</td></tr>
 * </table></p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── Domain Exceptions ─────────────────────────────────────────────────────

    @ExceptionHandler(GameSessionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSessionNotFound(
            GameSessionNotFoundException ex) {
        log.warn("Session not found: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidGuessException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidGuess(
            InvalidGuessException ex) {
        log.warn("Invalid guess: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InsufficientItemsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientItems(
            InsufficientItemsException ex) {
        log.error("Insufficient items in pool: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.error("Service temporarily unavailable: " + ex.getMessage()));
    }

    // ── Spring Validation Exceptions ──────────────────────────────────────────

    /**
     * Handles {@code @Valid} failures on {@code @RequestBody} DTOs.
     * Collects all field errors into a readable string.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));

        log.warn("Validation failure: {}", errors);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation failed: " + errors));
    }

    /**
     * Handles {@code @Validated} constraint violations on {@code @RequestParam}
     * and {@code @PathVariable} (e.g. {@code @Min}, {@code @Max}).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex) {

        String errors = ex.getConstraintViolations()
            .stream()
            .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
            .collect(Collectors.joining("; "));

        log.warn("Constraint violation: {}", errors);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Invalid parameter: " + errors));
    }

    /**
     * Handles malformed JSON bodies (e.g. invalid enum value for GuessType).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedJson(
            HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(
                "Malformed request body. Check that 'guess' is HIGHER or LOWER."));
    }

    /**
     * Handles type mismatches on request parameters (e.g. ?limit=abc).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String msg = String.format("Parameter '%s' must be of type %s",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("Type mismatch: {}", msg);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(msg));
    }

    // ── Catch-all ─────────────────────────────────────────────────────────────

    /**
     * Safety net for any unhandled exception.
     * Logs the full stack trace but returns a generic message to the client
     * to avoid leaking internal details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An unexpected error occurred. Please try again later."));
    }
}
