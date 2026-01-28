package at.htlle.controller;

import at.htlle.entity.Restaurant;
import at.htlle.repository.BranchRepository;
import at.htlle.repository.CustomerRepository;
import at.htlle.repository.LoyaltyAccountRepository;
import at.htlle.repository.PurchaseRepository;
import at.htlle.repository.RedemptionRepository;
import at.htlle.repository.RestaurantRepository;
import at.htlle.repository.RewardRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {

    private final RestaurantRepository restaurantRepository;
    private final CustomerRepository customerRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final RewardRepository rewardRepository;
    private final PurchaseRepository purchaseRepository;
    private final RedemptionRepository redemptionRepository;
    private final BranchRepository branchRepository;

    public AdminDashboardController(RestaurantRepository restaurantRepository,
                                    CustomerRepository customerRepository,
                                    LoyaltyAccountRepository loyaltyAccountRepository,
                                    RewardRepository rewardRepository,
                                    PurchaseRepository purchaseRepository,
                                    RedemptionRepository redemptionRepository,
                                    BranchRepository branchRepository) {
        this.restaurantRepository = restaurantRepository;
        this.customerRepository = customerRepository;
        this.loyaltyAccountRepository = loyaltyAccountRepository;
        this.rewardRepository = rewardRepository;
        this.purchaseRepository = purchaseRepository;
        this.redemptionRepository = redemptionRepository;
        this.branchRepository = branchRepository;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        List<Restaurant> restaurants = restaurantRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(Restaurant::getId).reversed())
                .toList();
        Map<Long, Integer> branchCounts = new HashMap<>();
        for (Restaurant restaurant : restaurants) {
            branchCounts.put(restaurant.getId(),
                    branchRepository.findByRestaurantId(restaurant.getId()).size());
        }

        model.addAttribute("restaurantCount", restaurantRepository.count());
        model.addAttribute("customerCount", customerRepository.count());
        model.addAttribute("accountCount", loyaltyAccountRepository.count());
        model.addAttribute("rewardCount", rewardRepository.count());
        model.addAttribute("purchaseCount", purchaseRepository.count());
        model.addAttribute("redemptionCount", redemptionRepository.count());
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("branchCounts", branchCounts);
        return "admin-dashboard";
    }
}
