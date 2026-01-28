package at.htlle.service;

import at.htlle.dto.RegistrationRequest;
import at.htlle.entity.AppUser;
import at.htlle.entity.Customer;
import at.htlle.entity.LoyaltyAccount;
import at.htlle.entity.Restaurant;
import at.htlle.repository.AppUserRepository;
import at.htlle.repository.CustomerRepository;
import at.htlle.repository.LoyaltyAccountRepository;
import at.htlle.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Handles end-user registration and account provisioning.
 */
@Service
public class UserService {

    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository appUserRepository,
                       CustomerRepository customerRepository,
                       RestaurantRepository restaurantRepository,
                       LoyaltyAccountRepository loyaltyAccountRepository,
                       PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.customerRepository = customerRepository;
        this.restaurantRepository = restaurantRepository;
        this.loyaltyAccountRepository = loyaltyAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new customer, loyalty account, and app user.
     *
     * @param request registration payload
     * @return created app user
     */
    @Transactional
    public AppUser registerCustomer(RegistrationRequest request) {
        String email = normalizeEmail(request.email());
        if (appUserRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (customerRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Customer email is already registered");
        }

        if (!isPasswordStrong(request.password())) {
            throw new IllegalArgumentException("Password must include upper, lower and number");
        }

        Restaurant restaurant = restaurantRepository.findByCode(normalizeCode(request.restaurantCode()))
                .orElseThrow(() -> new EntityNotFoundException("Restaurant code not found"));

        Customer customer = new Customer();
        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setEmail(email);
        customer.setUsername(generateUsername(email));
        customer.setPassword(passwordEncoder.encode(request.password()));
        if (StringUtils.hasText(request.phoneNumber())) {
            customer.setPhoneNumber(request.phoneNumber().trim());
        }
        customer.setStatus(Customer.Status.ACTIVE);
        customer.setRole(Customer.Role.USER);
        Customer savedCustomer = customerRepository.save(customer);

        loyaltyAccountRepository.findByCustomerIdAndRestaurantId(savedCustomer.getId(), restaurant.getId())
                .ifPresent(existing -> {
                    throw new IllegalStateException("Account already exists for this restaurant");
                });

        LoyaltyAccount account = new LoyaltyAccount();
        account.setCustomer(savedCustomer);
        account.setRestaurant(restaurant);
        account.setAccountNumber(generateAccountNumber());
        account.setStatus(LoyaltyAccount.Status.ACTIVE);
        account.setTier(LoyaltyAccount.Tier.STANDARD);
        account.setCurrentPoints(0L);
        loyaltyAccountRepository.save(account);

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(AppUser.Role.CUSTOMER);
        user.setCustomer(savedCustomer);
        user.setRestaurant(restaurant);
        return appUserRepository.save(user);
    }

    /**
     * Loads an application user by email address.
     *
     * @param email user email
     * @return app user
     */
    public AppUser loadByEmail(String email) {
        return appUserRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isPasswordStrong(String password) {
        if (!StringUtils.hasText(password)) {
            return false;
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        return hasUpper && hasLower && hasDigit;
    }

    private String generateUsername(String email) {
        String base = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        base = base.replaceAll("[^A-Za-z0-9._-]", "");
        if (base.length() > 50) {
            base = base.substring(0, 50);
        }
        String candidate = base;
        int suffix = 1;
        while (customerRepository.findByUsername(candidate).isPresent()) {
            String extra = String.valueOf(suffix++);
            int maxBase = Math.max(1, 60 - extra.length());
            String trimmed = base.length() > maxBase ? base.substring(0, maxBase) : base;
            candidate = trimmed + extra;
        }
        if (!StringUtils.hasText(candidate)) {
            candidate = "user" + System.currentTimeMillis();
        }
        return candidate;
    }

    private String generateAccountNumber() {
        String candidate;
        do {
            candidate = "ACCT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        } while (loyaltyAccountRepository.findByAccountNumber(candidate).isPresent());
        return candidate;
    }
}
