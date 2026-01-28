package at.htlle.controller;

import at.htlle.entity.Branch;
import at.htlle.entity.PointRule;
import at.htlle.entity.Restaurant;
import at.htlle.entity.Reward;
import at.htlle.entity.Customer;
import at.htlle.entity.LoyaltyAccount;
import at.htlle.entity.PointLedger;
import at.htlle.entity.Purchase;
import at.htlle.dto.AdminRedemptionSummary;
import at.htlle.repository.BranchRepository;
import at.htlle.repository.CustomerRepository;
import at.htlle.repository.LoyaltyAccountRepository;
import at.htlle.repository.PointLedgerRepository;
import at.htlle.repository.PointRuleRepository;
import at.htlle.repository.PurchaseRepository;
import at.htlle.repository.RedemptionRepository;
import at.htlle.repository.RestaurantRepository;
import at.htlle.repository.RewardRepository;
import at.htlle.service.AdminManagementService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin console controller for customers, rewards, ledger, and restaurants.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final String DEFAULT_POINT_RULE_NAME = "Default Points";
    private static final String FIXED_ADMIN_USERNAME = "admin";
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final RestaurantRepository restaurantRepository;
    private final BranchRepository branchRepository;
    private final RewardRepository rewardRepository;
    private final PointRuleRepository pointRuleRepository;
    private final CustomerRepository customerRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final PurchaseRepository purchaseRepository;
    private final RedemptionRepository redemptionRepository;
    private final AdminManagementService adminManagementService;

    public AdminController(RestaurantRepository restaurantRepository,
                           BranchRepository branchRepository,
                           RewardRepository rewardRepository,
                           PointRuleRepository pointRuleRepository,
                           CustomerRepository customerRepository,
                           LoyaltyAccountRepository loyaltyAccountRepository,
                           PointLedgerRepository pointLedgerRepository,
                           PurchaseRepository purchaseRepository,
                           RedemptionRepository redemptionRepository,
                           AdminManagementService adminManagementService) {
        this.restaurantRepository = restaurantRepository;
        this.branchRepository = branchRepository;
        this.rewardRepository = rewardRepository;
        this.pointRuleRepository = pointRuleRepository;
        this.customerRepository = customerRepository;
        this.loyaltyAccountRepository = loyaltyAccountRepository;
        this.pointLedgerRepository = pointLedgerRepository;
        this.purchaseRepository = purchaseRepository;
        this.redemptionRepository = redemptionRepository;
        this.adminManagementService = adminManagementService;
    }

    /**
     * Displays the admin home summary.
     *
     * @param model view model
     * @return view name
     */
    @GetMapping
    public String adminHome(Model model) {
        model.addAttribute("customerCount", customerRepository.count());
        model.addAttribute("restaurantCount", restaurantRepository.count());
        model.addAttribute("pointsInCirculation", loyaltyAccountRepository.sumCurrentPoints());
        List<PointLedger> recentLedger = pointLedgerRepository
                .findAllByOrderByOccurredAtDescIdDesc(PageRequest.of(0, 8))
                .getContent();
        model.addAttribute("recentLedger", recentLedger);
        return "admin";
    }

    /**
     * Displays the customer list for administrators.
     *
     * @param model view model
     * @return view name
     */
    @GetMapping("/customers")
    public String customers(Model model) {
        List<Customer> customers = customerRepository.findAll().stream()
                .sorted(Comparator.comparing(Customer::getId).reversed())
                .toList();
        List<CustomerSummary> summaries = customers.stream()
                .map(customer -> new CustomerSummary(customer, primaryAccount(customer.getId())))
                .toList();
        model.addAttribute("customers", summaries);
        model.addAttribute("adminUsername", FIXED_ADMIN_USERNAME);
        return "admin-customers";
    }

    /**
     * Updates a customer's role.
     *
     * @param customerId customer id
     * @param role new role
     * @param redirectAttributes redirect flash messages
     * @return redirect target
     */
    @PostMapping("/customers/{id}/role")
    public String updateRole(@PathVariable("id") Long customerId,
                             @RequestParam("role") Customer.Role role,
                             RedirectAttributes redirectAttributes) {
        adminManagementService.updateCustomerRole(customerId, role)
                .ifPresent(message -> redirectAttributes.addFlashAttribute("errorMessage", message));
        return "redirect:/admin/customers";
    }

    /**
     * Updates a customer's status.
     *
     * @param customerId customer id
     * @param status new status
     * @param redirectAttributes redirect flash messages
     * @return redirect target
     */
    @PostMapping("/customers/{id}/status")
    public String updateStatus(@PathVariable("id") Long customerId,
                               @RequestParam("status") Customer.Status status,
                               RedirectAttributes redirectAttributes) {
        adminManagementService.updateCustomerStatus(customerId, status)
                .ifPresent(message -> redirectAttributes.addFlashAttribute("errorMessage", message));
        return "redirect:/admin/customers";
    }

    /**
     * Deletes a customer account.
     *
     * @param customerId customer id
     * @param redirectAttributes redirect flash messages
     * @return redirect target
     */
    @PostMapping("/customers/{id}/delete")
    public String deleteCustomer(@PathVariable("id") Long customerId,
                                 RedirectAttributes redirectAttributes) {
        adminManagementService.deleteCustomer(customerId)
                .ifPresent(message -> redirectAttributes.addFlashAttribute("errorMessage", message));
        return "redirect:/admin/customers";
    }

    /**
     * Displays a single customer detail page.
     *
     * @param customerId customer id
     * @param model view model
     * @return view name
     */
    @GetMapping("/customers/{id}")
    public String customerDetail(@PathVariable("id") Long customerId, Model model) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        List<LoyaltyAccount> accounts = loyaltyAccountRepository.findByCustomerId(customerId).stream()
                .sorted(Comparator.comparing(LoyaltyAccount::getId).reversed())
                .toList();
        model.addAttribute("customer", customer);
        model.addAttribute("accounts", accounts);
        return "admin-customer-detail";
    }

    /**
     * Applies a manual points adjustment for a customer account.
     *
     * @param customerId customer id
     * @param accountId loyalty account id
     * @param pointsDelta points delta
     * @param reason adjustment reason
     * @param redirectAttributes redirect flash messages
     * @return redirect target
     */
    @PostMapping("/customers/{id}/adjust")
    public String adjustCustomerPoints(@PathVariable("id") Long customerId,
                                       @RequestParam("accountId") Long accountId,
                                       @RequestParam("pointsDelta") Long pointsDelta,
                                       @RequestParam("reason") String reason,
                                       RedirectAttributes redirectAttributes) {
        adminManagementService.adjustPoints(accountId, pointsDelta, reason)
                .ifPresent(message -> redirectAttributes.addFlashAttribute("errorMessage", message));
        return "redirect:/admin/customers/" + customerId;
    }

    /**
     * Displays ledger entries and point rule configuration.
     *
     * @param model view model
     * @return view name
     */
    @GetMapping("/ledger")
    public String ledger(Model model) {
        List<PointLedger> entries = pointLedgerRepository
                .findAllByOrderByOccurredAtDescIdDesc(PageRequest.of(0, 200))
                .getContent();
        model.addAttribute("entries", entries);
        loadPointRules(model);
        return "admin-ledger";
    }

    /**
     * Displays the manual adjustment page.
     *
     * @param model view model
     * @return view name
     */
    @GetMapping("/adjustments")
    public String adjustments(Model model) {
        List<LoyaltyAccount> accounts = loyaltyAccountRepository.findAll().stream()
                .sorted(Comparator.comparing(LoyaltyAccount::getId).reversed())
                .toList();
        model.addAttribute("accounts", accounts);
        return "admin-adjustments";
    }

    /**
     * Applies a manual adjustment from the adjustments page.
     *
     * @param accountId loyalty account id
     * @param pointsDelta points delta
     * @param reason adjustment reason
     * @param redirectAttributes redirect flash messages
     * @return redirect target
     */
    @PostMapping("/adjustments")
    public String applyAdjustment(@RequestParam("accountId") Long accountId,
                                  @RequestParam("pointsDelta") Long pointsDelta,
                                  @RequestParam("reason") String reason,
                                  RedirectAttributes redirectAttributes) {
        adminManagementService.adjustPoints(accountId, pointsDelta, reason)
                .ifPresent(message -> redirectAttributes.addFlashAttribute("errorMessage", message));
        return "redirect:/admin/adjustments";
    }

    /**
     * Legacy adjustment endpoint redirecting to the adjustments page.
     *
     * @param accountId loyalty account id
     * @param pointsDelta points delta
     * @param reason adjustment reason
     * @param redirectAttributes redirect flash messages
     * @return redirect target
     */
    @PostMapping("/ledger/adjust")
    public String adjustPoints(@RequestParam("accountId") Long accountId,
                               @RequestParam("pointsDelta") Long pointsDelta,
                               @RequestParam("reason") String reason,
                               RedirectAttributes redirectAttributes) {
        adminManagementService.adjustPoints(accountId, pointsDelta, reason)
                .ifPresent(message -> redirectAttributes.addFlashAttribute("errorMessage", message));
        return "redirect:/admin/adjustments";
    }

    /**
     * Displays recent purchases.
     *
     * @param model view model
     * @return view name
     */
    @GetMapping("/purchases")
    public String purchases(Model model) {
        List<Purchase> purchases = purchaseRepository.findAllByOrderByPurchasedAtDesc();
        model.addAttribute("purchases", purchases);
        return "admin-purchases";
    }

    /**
     * Displays rewards and redemption history.
     *
     * @param model view model
     * @return view name
     */
    @GetMapping("/rewards")
    public String rewards(Model model) {
        List<Restaurant> restaurants = restaurantRepository.findAll().stream()
                .sorted(Comparator.comparing(Restaurant::getId).reversed())
                .toList();
        List<Reward> rewards = rewardRepository.findAll().stream()
                .sorted(Comparator.comparing(Reward::getId).reversed())
                .toList();
        java.util.Map<Long, java.util.List<Reward>> rewardsByRestaurant = new java.util.HashMap<>();
        for (Restaurant restaurant : restaurants) {
            rewardsByRestaurant.put(restaurant.getId(), new java.util.ArrayList<>());
        }
        for (Reward reward : rewards) {
            if (reward.getRestaurant() != null) {
                rewardsByRestaurant
                        .computeIfAbsent(reward.getRestaurant().getId(), key -> new java.util.ArrayList<>())
                        .add(reward);
            }
        }
        List<AdminRedemptionSummary> redemptions = redemptionRepository.findAllByOrderByRedeemedAtDesc().stream()
                .map(redemption -> new AdminRedemptionSummary(
                        redemption.getRedemptionCode(),
                        redemption.getLoyaltyAccount().getCustomer().getEmail(),
                        redemption.getReward().getName(),
                        redemption.getRestaurant().getName(),
                        redemption.getPointsSpent(),
                        redemption.getStatus(),
                        redemption.getRedeemedAt()))
                .toList();
        model.addAttribute("rewards", rewards);
        model.addAttribute("rewardsByRestaurant", rewardsByRestaurant);
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("redemptions", redemptions);
        return "admin-rewards";
    }

    /**
     * Displays restaurant, branch, and reward management view.
     *
     * @param model view model
     * @return view name
     */
    @GetMapping("/restaurants")
    public String restaurants(Model model) {
        loadAdminData(model);
        return "admin-restaurants";
    }

    /**
     * Creates a new restaurant.
     *
     * @param name restaurant name
     * @param code restaurant code
     * @param active active flag
     * @param defaultCurrency default currency code
     * @return redirect target
     */
    @PostMapping("/restaurants/create")
    public String createRestaurant(@RequestParam("name") String name,
                                   @RequestParam("code") String code,
                                   @RequestParam(name = "active", defaultValue = "false") boolean active,
                                   @RequestParam(name = "defaultCurrency", defaultValue = "EUR") String defaultCurrency) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name.trim());
        restaurant.setCode(code.trim().toUpperCase(Locale.ROOT));
        restaurant.setActive(active);
        restaurant.setDefaultCurrency(defaultCurrency.trim().toUpperCase(Locale.ROOT));
        restaurantRepository.save(restaurant);
        return "redirect:/admin";
    }

    /**
     * Updates restaurant details.
     *
     * @param restaurantId restaurant id
     * @param name restaurant name
     * @param code restaurant code
     * @param active active flag
     * @param defaultCurrency default currency code
     * @return redirect target
     */
    @PostMapping("/restaurants/{id}/update")
    public String updateRestaurant(@PathVariable("id") Long restaurantId,
                                   @RequestParam("name") String name,
                                   @RequestParam("code") String code,
                                   @RequestParam(name = "active", defaultValue = "false") boolean active,
                                   @RequestParam(name = "defaultCurrency", defaultValue = "EUR") String defaultCurrency) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        restaurant.setName(name.trim());
        restaurant.setCode(code.trim().toUpperCase(Locale.ROOT));
        restaurant.setActive(active);
        restaurant.setDefaultCurrency(defaultCurrency.trim().toUpperCase(Locale.ROOT));
        restaurantRepository.save(restaurant);
        return "redirect:/admin";
    }

    /**
     * Creates or updates the default point rule for a restaurant.
     *
     * @param restaurantId restaurant id
     * @param pointsPerEuro multiplier value
     * @param active active flag
     * @return redirect target
     */
    @PostMapping("/points-rules/set")
    public String setPointsRule(@RequestParam("restaurantId") Long restaurantId,
                                @RequestParam("pointsPerEuro") BigDecimal pointsPerEuro,
                                @RequestParam(name = "active", defaultValue = "true") boolean active) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        PointRule rule = pointRuleRepository.findByRestaurantIdAndName(restaurantId, DEFAULT_POINT_RULE_NAME)
                .orElseGet(PointRule::new);
        rule.setRestaurant(restaurant);
        rule.setName(DEFAULT_POINT_RULE_NAME);
        rule.setRuleType(PointRule.RuleType.MULTIPLIER);
        rule.setMultiplier(pointsPerEuro);
        rule.setAmountThreshold(BigDecimal.ONE);
        rule.setBasePoints(0);
        rule.setActive(active);
        pointRuleRepository.save(rule);
        return "redirect:/admin";
    }

    /**
     * Creates a new restaurant branch.
     *
     * @param restaurantId restaurant id
     * @param branchCode branch code
     * @param name branch name
     * @param defaultBranch default branch flag
     * @return redirect target
     */
    @PostMapping("/branches/create")
    public String createBranch(@RequestParam("restaurantId") Long restaurantId,
                               @RequestParam("branchCode") String branchCode,
                               @RequestParam("name") String name,
                               @RequestParam(name = "defaultBranch", defaultValue = "false") boolean defaultBranch) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        Branch branch = new Branch();
        branch.setRestaurant(restaurant);
        branch.setBranchCode(branchCode.trim().toUpperCase(Locale.ROOT));
        branch.setName(name.trim());
        branch.setDefaultBranch(defaultBranch);
        if (defaultBranch) {
            clearDefaultBranch(restaurantId);
        }
        branchRepository.save(branch);
        return "redirect:/admin";
    }

    /**
     * Updates a branch.
     *
     * @param branchId branch id
     * @param restaurantId restaurant id
     * @param branchCode branch code
     * @param name branch name
     * @param defaultBranch default branch flag
     * @return redirect target
     */
    @PostMapping("/branches/{id}/update")
    public String updateBranch(@PathVariable("id") Long branchId,
                               @RequestParam("restaurantId") Long restaurantId,
                               @RequestParam("branchCode") String branchCode,
                               @RequestParam("name") String name,
                               @RequestParam(name = "defaultBranch", defaultValue = "false") boolean defaultBranch) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        branch.setRestaurant(restaurant);
        branch.setBranchCode(branchCode.trim().toUpperCase(Locale.ROOT));
        branch.setName(name.trim());
        branch.setDefaultBranch(defaultBranch);
        if (defaultBranch) {
            clearDefaultBranch(restaurantId);
        }
        branchRepository.save(branch);
        return "redirect:/admin";
    }

    /**
     * Marks a branch as the default branch.
     *
     * @param branchId branch id
     * @return redirect target
     */
    @PostMapping("/branches/{id}/default")
    public String setDefaultBranch(@PathVariable("id") Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));
        clearDefaultBranch(branch.getRestaurant().getId());
        branch.setDefaultBranch(true);
        branchRepository.save(branch);
        return "redirect:/admin";
    }

    /**
     * Creates a new reward.
     *
     * @param restaurantId restaurant id
     * @param rewardCode reward code
     * @param name reward name
     * @param description reward description
     * @param costPoints points cost
     * @param validUntil optional expiration date
     * @param active active flag
     * @return redirect target
     */
    @PostMapping("/rewards/create")
    public String createReward(@RequestParam("restaurantId") Long restaurantId,
                               @RequestParam("rewardCode") String rewardCode,
                               @RequestParam("name") String name,
                               @RequestParam("description") String description,
                               @RequestParam("costPoints") Integer costPoints,
                               @RequestParam(name = "validUntil", required = false) java.time.LocalDate validUntil,
                               @RequestParam(name = "active", defaultValue = "false") boolean active) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        Reward reward = new Reward();
        reward.setRestaurant(restaurant);
        reward.setRewardCode(rewardCode.trim().toUpperCase(Locale.ROOT));
        reward.setName(name.trim());
        reward.setDescription(description.trim());
        reward.setCostPoints(costPoints);
        reward.setActive(active);
        reward.setValidFrom(java.time.LocalDate.now());
        reward.setValidUntil(validUntil);
        rewardRepository.save(reward);
        return "redirect:/admin";
    }

    /**
     * Updates an existing reward.
     *
     * @param rewardId reward id
     * @param restaurantId restaurant id
     * @param rewardCode reward code
     * @param name reward name
     * @param description reward description
     * @param costPoints points cost
     * @param validUntil optional expiration date
     * @param active active flag
     * @return redirect target
     */
    @PostMapping("/rewards/{id}/update")
    public String updateReward(@PathVariable("id") Long rewardId,
                               @RequestParam("restaurantId") Long restaurantId,
                               @RequestParam("rewardCode") String rewardCode,
                               @RequestParam("name") String name,
                               @RequestParam("description") String description,
                               @RequestParam("costPoints") Integer costPoints,
                               @RequestParam(name = "validUntil", required = false) java.time.LocalDate validUntil,
                               @RequestParam(name = "active", defaultValue = "false") boolean active) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new IllegalArgumentException("Reward not found"));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        reward.setRestaurant(restaurant);
        reward.setRewardCode(rewardCode.trim().toUpperCase(Locale.ROOT));
        reward.setName(name.trim());
        reward.setDescription(description.trim());
        reward.setCostPoints(costPoints);
        reward.setActive(active);
        reward.setValidUntil(validUntil);
        rewardRepository.save(reward);
        return "redirect:/admin";
    }

    private void clearDefaultBranch(Long restaurantId) {
        List<Branch> branches = branchRepository.findByRestaurantId(restaurantId);
        for (Branch branch : branches) {
            if (branch.isDefaultBranch()) {
                branch.setDefaultBranch(false);
                branchRepository.save(branch);
            }
        }
    }

    private void loadAdminData(Model model) {
        List<Restaurant> restaurants = restaurantRepository.findAll().stream()
                .filter(restaurant -> {
                    if (restaurant == null) {
                        logger.warn("Skipping null restaurant in admin data load");
                        return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Restaurant::getId).reversed())
                .toList();
        List<Branch> branches = branchRepository.findAll().stream()
                .filter(branch -> {
                    if (branch == null) {
                        logger.warn("Skipping null branch in admin data load");
                        return false;
                    }
                    if (branch.getRestaurant() == null || branch.getRestaurant().getId() == null) {
                        logger.warn("Skipping branch with missing restaurant. branchId={}", branch.getId());
                        return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Branch::getId).reversed())
                .toList();
        List<Reward> rewards = rewardRepository.findAll().stream()
                .filter(reward -> {
                    if (reward == null) {
                        logger.warn("Skipping null reward in admin data load");
                        return false;
                    }
                    if (reward.getRestaurant() == null || reward.getRestaurant().getId() == null) {
                        logger.warn("Skipping reward with missing restaurant. rewardId={}", reward.getId());
                        return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Reward::getId).reversed())
                .toList();

        model.addAttribute("restaurants", restaurants);
        model.addAttribute("branches", branches);
        model.addAttribute("rewards", rewards);
    }

    private void loadPointRules(Model model) {
        List<Restaurant> restaurants = restaurantRepository.findAll().stream()
                .sorted(Comparator.comparing(Restaurant::getId).reversed())
                .toList();
        Map<Long, PointRule> defaultRules = new java.util.HashMap<>();
        for (Restaurant restaurant : restaurants) {
            if (restaurant == null || restaurant.getId() == null) {
                continue;
            }
            PointRule rule = pointRuleRepository
                    .findByRestaurantIdAndName(restaurant.getId(), DEFAULT_POINT_RULE_NAME)
                    .orElse(null);
            defaultRules.put(restaurant.getId(), rule);
        }
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("defaultRules", defaultRules);
    }

    private LoyaltyAccount primaryAccount(Long customerId) {
        return loyaltyAccountRepository.findByCustomerId(customerId).stream()
                .max(Comparator.comparing(LoyaltyAccount::getId))
                .orElse(null);
    }

    private record CustomerSummary(Customer customer, LoyaltyAccount account) {
    }
}
