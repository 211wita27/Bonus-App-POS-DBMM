package at.htlle.dto;

/**
 * Lightweight reward summary for API responses.
 */
public record RewardSummary(
        Long id,
        String name,
        String description,
        Integer costPoints) {
}
