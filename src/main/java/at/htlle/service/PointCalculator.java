package at.htlle.service;

import at.htlle.entity.PointRule;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class PointCalculator {

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

    public boolean isRuleActive(PointRule rule, Instant purchasedAt) {
        Objects.requireNonNull(rule, "rule");
        if (!rule.isActive()) {
            return false;
        }
        LocalDate referenceDate = toReferenceDate(purchasedAt);
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

    private LocalDate toReferenceDate(Instant purchasedAt) {
        Instant reference = purchasedAt != null ? purchasedAt : Instant.now();
        return reference.atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
