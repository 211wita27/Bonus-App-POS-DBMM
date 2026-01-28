package at.htlle.service;

import static org.assertj.core.api.Assertions.assertThat;

import at.htlle.entity.PointRule;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class PointCalculatorTest {

    private final PointCalculator pointCalculator = new PointCalculator();

    @Test
    void calculatePointsMultiplierRoundsDown() {
        PointRule rule = baseRule(PointRule.RuleType.MULTIPLIER, new BigDecimal("1.00"), new BigDecimal("1.00"), 0);
        long points = pointCalculator.calculatePoints(new BigDecimal("12.99"), rule);
        assertThat(points).isEqualTo(12);
    }

    @Test
    void calculatePointsMultiplierWithThreshold() {
        PointRule rule = baseRule(PointRule.RuleType.MULTIPLIER, new BigDecimal("2.00"), new BigDecimal("10.00"), 0);
        long points = pointCalculator.calculatePoints(new BigDecimal("99.00"), rule);
        assertThat(points).isEqualTo(19);
    }

    @Test
    void calculatePointsFixedUsesBasePoints() {
        PointRule rule = baseRule(PointRule.RuleType.FIXED, new BigDecimal("0.00"), new BigDecimal("0.00"), 150);
        long points = pointCalculator.calculatePoints(new BigDecimal("999.00"), rule);
        assertThat(points).isEqualTo(150);
    }

    @Test
    void isRuleActiveHonorsDateWindow() {
        PointRule rule = baseRule(PointRule.RuleType.MULTIPLIER, new BigDecimal("1.00"), new BigDecimal("1.00"), 0);
        rule.setValidFrom(LocalDate.of(2025, 1, 1));
        rule.setValidUntil(LocalDate.of(2025, 1, 31));

        Instant inside = LocalDate.of(2025, 1, 15).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant outside = LocalDate.of(2025, 2, 1).atStartOfDay().toInstant(ZoneOffset.UTC);

        assertThat(pointCalculator.isRuleActive(rule, inside)).isTrue();
        assertThat(pointCalculator.isRuleActive(rule, outside)).isFalse();
    }

    @Test
    void isRuleActiveRequiresActiveFlag() {
        PointRule rule = baseRule(PointRule.RuleType.MULTIPLIER, new BigDecimal("1.00"), new BigDecimal("1.00"), 0);
        rule.setActive(false);

        Instant reference = Instant.parse("2025-01-10T00:00:00Z");
        assertThat(pointCalculator.isRuleActive(rule, reference)).isFalse();
    }

    @Test
    void isRuleActiveUsesProvidedZone() {
        PointRule rule = baseRule(PointRule.RuleType.MULTIPLIER, new BigDecimal("1.00"), new BigDecimal("1.00"), 0);
        rule.setValidUntil(LocalDate.of(2025, 1, 1));

        Instant lateUtc = Instant.parse("2025-01-01T23:30:00Z");
        assertThat(pointCalculator.isRuleActive(rule, lateUtc, ZoneId.of("UTC"))).isTrue();
        assertThat(pointCalculator.isRuleActive(rule, lateUtc, ZoneId.of("Europe/Vienna"))).isFalse();
    }

    private PointRule baseRule(PointRule.RuleType ruleType, BigDecimal multiplier, BigDecimal threshold, int basePoints) {
        PointRule rule = new PointRule();
        rule.setRuleType(ruleType);
        rule.setMultiplier(multiplier);
        rule.setAmountThreshold(threshold);
        rule.setBasePoints(basePoints);
        rule.setActive(true);
        return rule;
    }
}
