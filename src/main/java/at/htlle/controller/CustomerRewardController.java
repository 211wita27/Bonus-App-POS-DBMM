package at.htlle.controller;

import at.htlle.dto.AccountResponse;
import at.htlle.dto.RedemptionRequest;
import at.htlle.dto.RedemptionResponse;
import at.htlle.entity.AppUser;
import at.htlle.entity.Branch;
import at.htlle.entity.LoyaltyAccount;
import at.htlle.entity.Reward;
import at.htlle.service.AccountService;
import at.htlle.service.CurrentUserService;
import at.htlle.service.LoyaltyService;
import at.htlle.repository.BranchRepository;
import at.htlle.repository.RewardRepository;
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

    private final AccountService accountService;
    private final CurrentUserService currentUserService;
    private final LoyaltyService loyaltyService;
    private final RewardRepository rewardRepository;
    private final BranchRepository branchRepository;

    public CustomerRewardController(AccountService accountService,
                                    CurrentUserService currentUserService,
                                    LoyaltyService loyaltyService,
                                    RewardRepository rewardRepository,
                                    BranchRepository branchRepository) {
        this.accountService = accountService;
        this.currentUserService = currentUserService;
        this.loyaltyService = loyaltyService;
        this.rewardRepository = rewardRepository;
        this.branchRepository = branchRepository;
    }

    @GetMapping("/customer/rewards")
    public String rewards(@RequestParam(name = "accountId", required = false) Long accountId,
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
        LoyaltyAccount selected = selectAccount(accounts, accountId);
        AccountResponse accountResponse = accountService.buildAccountResponse(selected, false);
        List<RewardCard> rewardCards = buildRewardCards(selected);
        List<Branch> branches = branchRepository.findByRestaurantId(selected.getRestaurant().getId());
        if (branches.isEmpty()) {
            model.addAttribute("errorMessage", "No branches configured for this restaurant.");
        }

        model.addAttribute("accounts", accounts);
        model.addAttribute("account", accountResponse);
        model.addAttribute("accountId", selected.getId());
        model.addAttribute("rewardCards", rewardCards);
        model.addAttribute("branches", branches);
        return "rewards";
    }

    @PostMapping("/customer/rewards/redeem")
    public String redeem(@RequestParam("accountId") Long accountId,
                         @RequestParam("rewardId") Long rewardId,
                         @RequestParam("branchId") Long branchId,
                         @RequestParam(name = "notes", required = false) String notes,
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
            RedemptionRequest payload = new RedemptionRequest(account.getId(), rewardId, branchId, notes);
            RedemptionResponse response = toResponse(loyaltyService.redeemReward(payload));
            model.addAttribute("redemption", response);
            model.addAttribute("accountId", account.getId());
            return "redemption-success";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return rewards(account.getId(), model);
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

    private RedemptionResponse toResponse(at.htlle.entity.Redemption redemption) {
        return new RedemptionResponse(
                redemption.getId(),
                redemption.getLoyaltyAccount().getId(),
                redemption.getReward().getId(),
                redemption.getBranch().getId(),
                redemption.getLedgerEntry().getId(),
                redemption.getPointsSpent(),
                redemption.getLedgerEntry().getBalanceAfter(),
                redemption.getStatus(),
                redemption.getRedeemedAt());
    }

    public record RewardCard(Long rewardId, String name, String description, long costPoints) {
    }
}
