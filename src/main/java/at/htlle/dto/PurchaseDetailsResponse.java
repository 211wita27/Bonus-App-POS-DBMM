package at.htlle.dto;

import java.math.BigDecimal;

/**
 * Detailed purchase response payload for API consumers.
 */
public record PurchaseDetailsResponse(
        Long accountId,
        Long restaurantId,
        String purchaseNumber,
        BigDecimal totalAmount,
        String currency,
        String notes,
        String description) {
}
