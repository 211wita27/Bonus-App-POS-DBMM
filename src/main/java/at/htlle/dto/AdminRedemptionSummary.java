package at.htlle.dto;

import at.htlle.entity.Redemption;
import java.time.Instant;

/**
 * Summary view of a redemption for admin dashboards.
 */
public record AdminRedemptionSummary(
        String redemptionCode,
        String customerLabel,
        String rewardName,
        String restaurantName,
        Long pointsSpent,
        Redemption.Status status,
        Instant redeemedAt) {
}
