package at.htlle.service;

import at.htlle.entity.PointRule;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Calculates earned points based on point rules.
 */
@Component
public class PointCalculator {

    /**
     * Calculates points for a given purchase amount and rule.
     *
     * @param amount purchase amount
     * @param rule point rule or {@code null} for simple rounding
     * @return points earned
     */
    public long calculatePoints(BigDecimal amount, PointRule rule) {
        Objects.requireNonNull(amount, "amount");
        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.DOWN);
        if (rule == null) {
            return normalizedAmount.setScale(0, RoundingMode.DOWN).longValue();
        }

        BigDecimal pointsDecimal = switch (rule.getRuleType()) {
            case MULTIPLIER -> calculateMultiplierPoints(normalizedAmount, rule);
            case FIXED -> BigDecimal.valueOf(Objects.requireNonNullElse(rule.getBasePoints(), 0));
        };

        return pointsDecimal.setScale(0, RoundingMode.DOWN).longValue();
    }

    /**
     * Checks whether a rule is active at the given time using the system zone.
     *
     * @param rule point rule
     * @param purchasedAt purchase timestamp
     * @return {@code true} if active
     */
    public boolean isRuleActive(PointRule rule, Instant purchasedAt) {
        return isRuleActive(rule, purchasedAt, ZoneId.systemDefault());
    }

    /**
     * Checks whether a rule is active at the given time and zone.
     *
     * @param rule point rule
     * @param purchasedAt purchase timestamp
     * @param zoneId zone to use for date checks
     * @return {@code true} if active
     */
    public boolean isRuleActive(PointRule rule, Instant purchasedAt, ZoneId zoneId) {
        Objects.requireNonNull(rule, "rule");
        if (!rule.isActive()) {
            return false;
        }
        LocalDate referenceDate = toReferenceDate(purchasedAt, zoneId);
        if (rule.getValidFrom() != null && rule.getValidFrom().isAfter(referenceDate)) {
            return false;
        }
        if (rule.getValidUntil() != null && rule.getValidUntil().isBefore(referenceDate)) {
            return false;
        }
        return true;
    }

    private BigDecimal calculateMultiplierPoints(BigDecimal amount, PointRule rule) {
        BigDecimal threshold = Objects.requireNonNullElse(rule.getAmountThreshold(), BigDecimal.ZERO);
        BigDecimal multiplier = Objects.requireNonNullElse(rule.getMultiplier(), BigDecimal.ONE);
        BigDecimal units = threshold.compareTo(BigDecimal.ZERO) > 0
                ? amount.divide(threshold, 2, RoundingMode.DOWN)
                : amount;
        return units.multiply(multiplier);
    }

    private LocalDate toReferenceDate(Instant purchasedAt, ZoneId zoneId) {
        Instant reference = purchasedAt != null ? purchasedAt : Instant.now();
        ZoneId resolvedZone = zoneId != null ? zoneId : ZoneId.systemDefault();
        return reference.atZone(resolvedZone).toLocalDate();
    }
}
