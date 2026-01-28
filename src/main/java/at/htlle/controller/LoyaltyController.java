package at.htlle.controller;

import at.htlle.dto.AccountResponse;
import at.htlle.dto.PurchaseRequest;
import at.htlle.dto.PurchaseResponse;
import at.htlle.dto.RedemptionRequest;
import at.htlle.dto.RedemptionResponse;
import at.htlle.entity.PointLedger;
import at.htlle.entity.Purchase;
import at.htlle.entity.Redemption;
import at.htlle.service.AccountService;
import at.htlle.service.LoyaltyService;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;
    private final AccountService accountService;

    public LoyaltyController(
            LoyaltyService loyaltyService,
            AccountService accountService) {
        this.loyaltyService = loyaltyService;
        this.accountService = accountService;
    }

    @PostMapping("/purchases")
    public ResponseEntity<PurchaseResponse> recordPurchase(@Valid @RequestBody PurchaseRequest request) {
        PointLedger ledger = loyaltyService.recordPurchase(request);
        Purchase purchase = Objects.requireNonNull(ledger.getPurchase(), "purchase");

        PurchaseResponse response = new PurchaseResponse(
                purchase.getId(),
                purchase.getPurchaseNumber(),
                purchase.getTotalAmount(),
                purchase.getCurrency(),
                purchase.getPurchasedAt(),
                ledger.getLoyaltyAccount().getId(),
                purchase.getRestaurant().getId(),
                ledger.getId(),
                ledger.getPoints(),
                ledger.getBalanceAfter());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/redemptions")
    public ResponseEntity<RedemptionResponse> redeemReward(@Valid @RequestBody RedemptionRequest request) {
        Redemption redemption = loyaltyService.redeemReward(request);
        PointLedger ledger = redemption.getLedgerEntry();

        RedemptionResponse response = new RedemptionResponse(
                redemption.getId(),
                redemption.getLoyaltyAccount().getId(),
                redemption.getReward().getId(),
                redemption.getRestaurant().getId(),
                ledger.getId(),
                redemption.getPointsSpent(),
                redemption.getRedemptionCode(),
                ledger.getBalanceAfter(),
                redemption.getStatus(),
                redemption.getRedeemedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/accounts/{id}/sync")
    public AccountResponse synchronizeBalance(@PathVariable("id") Long accountId,
                                              @RequestParam(defaultValue = "false") boolean includeLedger) {
        return accountService.buildAccountResponse(loyaltyService.synchronizeBalance(accountId), includeLedger);
    }

    @GetMapping("/accounts/{id}")
    public AccountResponse getAccount(@PathVariable("id") Long accountId,
                                      @RequestParam(defaultValue = "false") boolean includeLedger) {
        return accountService.getAccountResponse(accountId, includeLedger);
    }
}
