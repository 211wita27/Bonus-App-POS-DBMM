package at.htlle.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * API response payload summarizing a recorded purchase.
 */
public record PurchaseResponse(
        Long purchaseId,
        String purchaseNumber,
        BigDecimal totalAmount,
        String currency,
        Instant purchasedAt,
        Long accountId,
        Long restaurantId,
        Long ledgerEntryId,
        Long points,
        Long balanceAfter) {
}
