package at.htlle.controller;

import at.htlle.dto.PurchaseRequest;
import at.htlle.entity.AppUser;
import at.htlle.entity.LoyaltyAccount;
import at.htlle.entity.PointLedger;
import at.htlle.entity.Purchase;
import at.htlle.entity.Redemption;
import at.htlle.entity.Reward;
import at.htlle.entity.Restaurant;
import at.htlle.repository.LoyaltyAccountRepository;
import at.htlle.repository.PurchaseRepository;
import at.htlle.repository.RedemptionRepository;
import at.htlle.repository.RewardRepository;
import at.htlle.service.CurrentUserService;
import at.htlle.service.LoyaltyService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/restaurant")
public class RestaurantDashboardController {

    private final CurrentUserService currentUserService;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final PurchaseRepository purchaseRepository;
    private final RedemptionRepository redemptionRepository;
    private final RewardRepository rewardRepository;
    private final LoyaltyService loyaltyService;

    public RestaurantDashboardController(CurrentUserService currentUserService,
                                         LoyaltyAccountRepository loyaltyAccountRepository,
                                         PurchaseRepository purchaseRepository,
                                         RedemptionRepository redemptionRepository,
                                         RewardRepository rewardRepository,
                                         LoyaltyService loyaltyService) {
        this.currentUserService = currentUserService;
        this.loyaltyAccountRepository = loyaltyAccountRepository;
        this.purchaseRepository = purchaseRepository;
        this.redemptionRepository = redemptionRepository;
        this.rewardRepository = rewardRepository;
        this.loyaltyService = loyaltyService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Restaurant restaurant = requireRestaurant();
        List<LoyaltyAccount> accounts = loyaltyAccountRepository.findByRestaurantIdOrderByIdAsc(restaurant.getId());
        List<Purchase> purchases = purchaseRepository.findAllByRestaurantIdOrderByPurchasedAtDesc(restaurant.getId());
        List<Redemption> redemptions = redemptionRepository.findByRestaurantIdOrderByRedeemedAtDesc(restaurant.getId());
        List<Reward> rewards = rewardRepository.findByRestaurantIdOrderByIdDesc(restaurant.getId());

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("accounts", accounts);
        model.addAttribute("purchases", purchases.stream().limit(20).toList());
        model.addAttribute("redemptions", redemptions.stream().limit(20).toList());
        model.addAttribute("rewards", rewards);
        model.addAttribute("customerCount", accounts.size());
        model.addAttribute("purchaseCount", purchases.size());
        model.addAttribute("redemptionCount", redemptions.size());
        return "restaurant-dashboard";
    }

    @PostMapping("/rewards")
    public String createReward(@RequestParam("rewardCode") String rewardCode,
                               @RequestParam("name") String name,
                               @RequestParam("description") String description,
                               @RequestParam("costPoints") Integer costPoints,
                               @RequestParam(name = "validUntil", required = false) LocalDate validUntil,
                               RedirectAttributes redirectAttributes) {
        Restaurant restaurant = requireRestaurant();
        try {
            Reward reward = new Reward();
            reward.setRestaurant(restaurant);
            reward.setRewardCode(rewardCode.trim().toUpperCase());
            reward.setName(name.trim());
            reward.setDescription(description.trim());
            reward.setCostPoints(costPoints);
            reward.setValidFrom(LocalDate.now());
            reward.setValidUntil(validUntil);
            reward.setActive(true);
            rewardRepository.save(reward);
            redirectAttributes.addFlashAttribute("successMessage", "Reward created for " + restaurant.getName() + ".");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/restaurant/dashboard";
    }

    @PostMapping("/purchases")
    public String createPurchase(@RequestParam("accountId") Long accountId,
                                 @RequestParam("totalAmount") BigDecimal totalAmount,
                                 @RequestParam("currency") String currency,
                                 @RequestParam(name = "purchaseNumber", required = false) String purchaseNumber,
                                 @RequestParam(name = "notes", required = false) String notes,
                                 @RequestParam(name = "description", required = false) String description,
                                 RedirectAttributes redirectAttributes) {
        Restaurant restaurant = requireRestaurant();
        try {
            LoyaltyAccount account = loyaltyAccountRepository.findById(accountId)
                    .orElseThrow(() -> new EntityNotFoundException("Account not found"));
            if (!account.getRestaurant().getId().equals(restaurant.getId())) {
                throw new IllegalArgumentException("Account does not belong to this restaurant");
            }

            String resolvedNumber = StringUtils.hasText(purchaseNumber)
                    ? purchaseNumber.trim()
                    : generatePurchaseNumber(restaurant);

            PurchaseRequest request = new PurchaseRequest(
                    account.getId(),
                    restaurant.getId(),
                    resolvedNumber,
                    totalAmount,
                    currency,
                    Instant.now(),
                    notes,
                    description,
                    null);

            PointLedger ledger = loyaltyService.recordPurchase(request);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Purchase recorded: +" + ledger.getPoints() + " points for "
                            + account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName());
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/restaurant/dashboard";
    }

    private Restaurant requireRestaurant() {
        AppUser user = currentUserService.getCurrentUser();
        if (user == null || user.getRestaurant() == null) {
            throw new EntityNotFoundException("No restaurant linked to this account.");
        }
        return user.getRestaurant();
    }

    private String generatePurchaseNumber(Restaurant restaurant) {
        // Keep purchase numbers unique and traceable per restaurant.
        String prefix = StringUtils.hasText(restaurant.getCode()) ? restaurant.getCode().trim() : "REST";
        return prefix + "-" + System.currentTimeMillis();
    }
}
