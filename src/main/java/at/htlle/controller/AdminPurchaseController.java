package at.htlle.controller;

import at.htlle.dto.PurchaseRequest;
import at.htlle.dto.PurchaseResponse;
import at.htlle.entity.PointLedger;
import at.htlle.service.LoyaltyService;
import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminPurchaseController {

    private final LoyaltyService loyaltyService;

    public AdminPurchaseController(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @GetMapping("/admin/purchases")
    public String purchaseForm(@RequestParam(name = "accountId", required = false) Long accountId,
                               @RequestParam(name = "branchId", required = false) Long branchId,
                               Model model) {
        model.addAttribute("accountId", accountId != null ? accountId : 1L);
        model.addAttribute("branchId", branchId != null ? branchId : 1L);
        model.addAttribute("currency", "EUR");
        return "purchase";
    }

    @PostMapping("/admin/purchases")
    public String createPurchase(@RequestParam("accountId") Long accountId,
                                 @RequestParam("branchId") Long branchId,
                                 @RequestParam("purchaseNumber") String purchaseNumber,
                                 @RequestParam("totalAmount") BigDecimal totalAmount,
                                 @RequestParam(name = "currency", defaultValue = "EUR") String currency,
                                 @RequestParam(name = "notes", required = false) String notes,
                                 @RequestParam(name = "description", required = false) String description,
                                 Model model) {
        String normalizedCurrency = currency.trim().toUpperCase(Locale.ROOT);
        PurchaseRequest payload = new PurchaseRequest(
                accountId,
                branchId,
                purchaseNumber,
                totalAmount,
                normalizedCurrency,
                null,
                notes,
                description,
                null);

        model.addAttribute("accountId", accountId);
        model.addAttribute("branchId", branchId);
        model.addAttribute("currency", normalizedCurrency);

        try {
            PointLedger ledger = loyaltyService.recordPurchase(payload);
            PurchaseResponse response = new PurchaseResponse(
                    ledger.getPurchase().getId(),
                    ledger.getPurchase().getPurchaseNumber(),
                    ledger.getPurchase().getTotalAmount(),
                    ledger.getPurchase().getCurrency(),
                    ledger.getPurchase().getPurchasedAt(),
                    ledger.getLoyaltyAccount().getId(),
                    ledger.getPurchase().getBranch().getId(),
                    ledger.getId(),
                    ledger.getPoints(),
                    ledger.getBalanceAfter());
            model.addAttribute("purchaseResponse", response);
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }
        return "purchase";
    }
}
