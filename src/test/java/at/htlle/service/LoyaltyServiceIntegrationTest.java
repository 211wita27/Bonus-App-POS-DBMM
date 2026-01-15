package at.htlle.service;

import static org.assertj.core.api.Assertions.assertThat;

import at.htlle.dto.PurchaseRequest;
import at.htlle.dto.RedemptionRequest;
import at.htlle.entity.LoyaltyAccount;
import at.htlle.entity.PointLedger;
import at.htlle.entity.PointRule;
import at.htlle.entity.Redemption;
import at.htlle.entity.Reward;
import at.htlle.repository.BranchRepository;
import at.htlle.repository.LoyaltyAccountRepository;
import at.htlle.repository.PointRuleRepository;
import at.htlle.repository.RewardRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class LoyaltyServiceIntegrationTest {

    @Autowired
    private LoyaltyService loyaltyService;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Autowired
    private PointRuleRepository pointRuleRepository;

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Test
    void earnAndRedeemFlowShouldUpdateBalances() {
        LoyaltyAccount account = loyaltyAccountRepository.findByAccountNumber("ACCT-0001")
                .orElseThrow();
        PointRule rule = pointRuleRepository.findAll().stream().findFirst().orElseThrow();
        Reward reward = rewardRepository.findAll().stream().findFirst().orElseThrow();
        Long branchId = branchRepository.findAll().stream().findFirst().map(branch -> branch.getId()).orElseThrow();

        BigDecimal amount = BigDecimal.valueOf(123.45);
        PurchaseRequest purchaseRequest = new PurchaseRequest(
                account.getId(),
                branchId,
                "PUR-" + UUID.randomUUID(),
                amount,
                "EUR",
                Instant.parse("2025-01-01T10:15:30Z"),
                "Mittagessen",
                "Integrationstest Kauf",
                rule.getId());

        PointLedger ledger = loyaltyService.recordPurchase(purchaseRequest);
        LoyaltyAccount updatedAccount = loyaltyAccountRepository.findById(account.getId()).orElseThrow();

        assertThat(ledger.getEntryType()).isEqualTo(PointLedger.EntryType.EARN);
        assertThat(updatedAccount.getCurrentPoints()).isGreaterThanOrEqualTo(120);

        RedemptionRequest redemptionRequest = new RedemptionRequest(account.getId(), reward.getId(), branchId, "Welcome Drink");
        Redemption redemption = loyaltyService.redeemReward(redemptionRequest);
        LoyaltyAccount afterRedemption = loyaltyAccountRepository.findById(account.getId()).orElseThrow();

        assertThat(redemption.getPointsSpent()).isEqualTo(reward.getCostPoints().longValue());
        assertThat(afterRedemption.getCurrentPoints()).isEqualTo(ledger.getBalanceAfter() - reward.getCostPoints());

        LoyaltyAccount synced = loyaltyService.synchronizeBalance(account.getId());
        assertThat(synced.getCurrentPoints()).isEqualTo(afterRedemption.getCurrentPoints());
    }

    @Test
    void redeemShouldFailWhenInsufficientPoints() {
        LoyaltyAccount account = loyaltyAccountRepository.findByAccountNumber("ACCT-0001")
                .orElseThrow();
        Reward reward = rewardRepository.findAll().stream().findFirst().orElseThrow();
        Long branchId = branchRepository.findAll().stream().findFirst().map(branch -> branch.getId()).orElseThrow();

        RedemptionRequest redemptionRequest = new RedemptionRequest(account.getId(), reward.getId(), branchId, "Test");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                loyaltyService.redeemReward(redemptionRequest));
    }
}
