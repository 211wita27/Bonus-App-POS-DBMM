package at.htlle.dto;

import at.htlle.entity.LoyaltyAccount;
import java.time.Instant;
import java.util.List;

/**
 * Response payload for a loyalty account, optionally including ledger entries.
 */
public record AccountResponse(
        Long id,
        String accountNumber,
        Long customerId,
        String firstName,
        String lastName,
        Long restaurantId,
        String restaurantName,
        LoyaltyAccount.Status status,
        LoyaltyAccount.Tier tier,
        Long currentPoints,
        Instant createdAt,
        Instant updatedAt,
        List<LedgerEntryResponse> ledgerEntries) {
}
