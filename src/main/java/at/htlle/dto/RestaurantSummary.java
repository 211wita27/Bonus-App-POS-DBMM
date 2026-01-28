package at.htlle.dto;

/**
 * Lightweight restaurant summary for API responses.
 */
public record RestaurantSummary(
        Long id,
        String name,
        String code,
        String defaultCurrency) {
}
