package at.htlle.dto;

import java.time.Instant;

/**
 * Standard JSON error response payload.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path) {
}
