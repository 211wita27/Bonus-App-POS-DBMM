package at.htlle.service;

import at.htlle.entity.Customer;
import at.htlle.entity.LoyaltyAccount;
import at.htlle.entity.Restaurant;
import at.htlle.repository.CustomerRepository;
import at.htlle.repository.LoyaltyAccountRepository;
import at.htlle.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final RestaurantRepository restaurantRepository;

    public AuthService(CustomerRepository customerRepository,
                       LoyaltyAccountRepository loyaltyAccountRepository,
                       RestaurantRepository restaurantRepository) {
        this.customerRepository = customerRepository;
        this.loyaltyAccountRepository = loyaltyAccountRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public Optional<LoyaltyAccount> authenticate(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return Optional.empty();
        }
        return customerRepository.findByUsername(username.trim())
                .filter(customer -> password.equals(customer.getPassword()))
                .flatMap(this::resolvePrimaryAccount);
    }

    public LoyaltyAccount register(String firstName,
                                   String lastName,
                                   String email,
                                   String username,
                                   String password) {
        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim().toLowerCase();
        if (customerRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (customerRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        Customer customer = new Customer();
        customer.setFirstName(firstName.trim());
        customer.setLastName(lastName.trim());
        customer.setEmail(normalizedEmail);
        customer.setUsername(normalizedUsername);
        customer.setPassword(password);
        Customer savedCustomer = customerRepository.save(customer);

        Restaurant restaurant = restaurantRepository.findByCode("DEMO")
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        LoyaltyAccount account = new LoyaltyAccount();
        account.setCustomer(savedCustomer);
        account.setRestaurant(restaurant);
        account.setAccountNumber(buildAccountNumber(savedCustomer.getId()));

        return loyaltyAccountRepository.save(account);
    }

    private Optional<LoyaltyAccount> resolvePrimaryAccount(Customer customer) {
        Restaurant restaurant = restaurantRepository.findByCode("DEMO")
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
        return loyaltyAccountRepository.findByCustomerIdAndRestaurantId(customer.getId(), restaurant.getId())
                .or(() -> customer.getLoyaltyAccounts().stream()
                        .min(Comparator.comparing(LoyaltyAccount::getId)));
    }

    private String buildAccountNumber(Long customerId) {
        return String.format("ACCT-%04d", customerId);
    }
}
