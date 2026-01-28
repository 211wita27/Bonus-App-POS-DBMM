package at.htlle.controller;

import at.htlle.dto.AccountResponse;
import at.htlle.dto.RedemptionRequest;
import at.htlle.dto.RedemptionResponse;
import at.htlle.entity.AppUser;
import at.htlle.entity.LoyaltyAccount;
import at.htlle.entity.Reward;
import at.htlle.service.AccountService;
import at.htlle.service.CurrentUserService;
import at.htlle.service.LoyaltyService;
import at.htlle.service.QrCodeService;
import at.htlle.repository.RewardRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CustomerRewardController {

    private static final String SELECTED_ACCOUNT_SESSION_KEY = "selectedAccountId";

    private final AccountService accountService;
    private final CurrentUserService currentUserService;
    private final LoyaltyService loyaltyService;
    private final RewardRepository rewardRepository;
    private final QrCodeService qrCodeService;

    public CustomerRewardController(AccountService accountService,
                                    CurrentUserService currentUserService,
                                    LoyaltyService loyaltyService,
                                    RewardRepository rewardRepository,
                                    QrCodeService qrCodeService) {
        this.accountService = accountService;
        this.currentUserService = currentUserService;
        this.loyaltyService = loyaltyService;
        this.rewardRepository = rewardRepository;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/customer/rewards")
    public String rewards(@RequestParam(name = "accountId", required = false) Long accountId,
                          HttpSession session,
                          Model model) {
        AppUser user = currentUserService.getCurrentUser();
        if (user.getCustomer() == null) {
            model.addAttribute("errorMessage", "No customer profile linked to this account.");
            return "rewards";
        }
        List<LoyaltyAccount> accounts = accountService.getAccountsForCustomer(user.getCustomer().getId());
        if (accounts.isEmpty()) {
            model.addAttribute("errorMessage", "No loyalty accounts found.");
            return "rewards";
        }
        LoyaltyAccount selected = resolveSelectedAccount(accounts, accountId, session);
        AccountResponse accountResponse = accountService.buildAccountResponse(selected, false);
        List<RewardCard> rewardCards = buildRewardCards(selected);

        model.addAttribute("accounts", accounts);
        model.addAttribute("account", accountResponse);
        model.addAttribute("accountId", selected.getId());
        model.addAttribute("rewardCards", rewardCards);
        return "rewards";
    }

    @PostMapping("/customer/rewards/redeem")
    public String redeem(@RequestParam("accountId") Long accountId,
                         @RequestParam("rewardId") Long rewardId,
                         @RequestParam(name = "notes", required = false) String notes,
                         HttpSession session,
                         Model model) {
        AppUser user = currentUserService.getCurrentUser();
        if (user.getCustomer() == null) {
            model.addAttribute("errorMessage", "No customer profile linked to this account.");
            return "rewards";
        }
        LoyaltyAccount account = accountService.getAccountsForCustomer(user.getCustomer().getId())
                .stream()
                .filter(candidate -> candidate.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid account selection"));

        try {
            RedemptionRequest payload = new RedemptionRequest(account.getId(), rewardId, account.getRestaurant().getId(), notes);
            RedemptionResponse response = toResponse(loyaltyService.redeemReward(payload));
            String qrPayload = buildQrPayload(response);
            model.addAttribute("qrPayload", qrPayload);
            model.addAttribute("qrCodeDataUri", qrCodeService.generateDataUri(qrPayload, 260));
            model.addAttribute("redemption", response);
            model.addAttribute("accountId", account.getId());
            if (session != null) {
                session.setAttribute(SELECTED_ACCOUNT_SESSION_KEY, account.getId());
            }
            return "redemption-success";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return rewards(account.getId(), session, model);
        }
    }

    private List<RewardCard> buildRewardCards(LoyaltyAccount account) {
        LocalDate referenceDate = resolveDate(account);
        return rewardRepository.findActiveRewardsForDate(account.getRestaurant().getId(), referenceDate)
                .stream()
                .sorted(Comparator.comparing(Reward::getCostPoints))
                .map(reward -> new RewardCard(
                        reward.getId(),
                        reward.getName(),
                        reward.getDescription(),
                        reward.getCostPoints()))
                .toList();
    }

    private LocalDate resolveDate(LoyaltyAccount account) {
        String timezone = account.getRestaurant().getTimezone();
        try {
            return LocalDate.now(ZoneId.of(timezone));
        } catch (Exception ex) {
            return LocalDate.now();
        }
    }

    private LoyaltyAccount selectAccount(List<LoyaltyAccount> accounts, Long accountId) {
        if (accountId != null) {
            Optional<LoyaltyAccount> match = accounts.stream()
                    .filter(account -> account.getId().equals(accountId))
                    .findFirst();
            if (match.isPresent()) {
                return match.get();
            }
        }
        return accounts.stream()
                .min(Comparator.comparing(LoyaltyAccount::getId))
                .orElseThrow();
    }

    private LoyaltyAccount resolveSelectedAccount(List<LoyaltyAccount> accounts, Long accountId, HttpSession session) {
        // Persist selection across page switches via session.
        Long resolvedId = accountId;
        if (resolvedId == null && session != null) {
            Object stored = session.getAttribute(SELECTED_ACCOUNT_SESSION_KEY);
            if (stored instanceof Long) {
                resolvedId = (Long) stored;
            } else if (stored instanceof String storedValue) {
                try {
                    resolvedId = Long.valueOf(storedValue);
                } catch (NumberFormatException ignored) {
                    resolvedId = null;
                }
            }
        }
        LoyaltyAccount selected = selectAccount(accounts, resolvedId);
        if (session != null) {
            session.setAttribute(SELECTED_ACCOUNT_SESSION_KEY, selected.getId());
        }
        return selected;
    }

    private RedemptionResponse toResponse(at.htlle.entity.Redemption redemption) {
        return new RedemptionResponse(
                redemption.getId(),
                redemption.getLoyaltyAccount().getId(),
                redemption.getReward().getId(),
                redemption.getRestaurant().getId(),
                redemption.getLedgerEntry().getId(),
                redemption.getPointsSpent(),
                redemption.getRedemptionCode(),
                redemption.getLedgerEntry().getBalanceAfter(),
                redemption.getStatus(),
                redemption.getRedeemedAt());
    }

    private String buildQrPayload(RedemptionResponse redemption) {
        // Compact payload for mobile scanners.
        return "BONUSAPP|REDEEM|" + redemption.redemptionCode()
                + "|R" + redemption.restaurantId()
                + "|A" + redemption.accountId();
    }

    public record RewardCard(Long rewardId, String name, String description, long costPoints) {
    }
}
