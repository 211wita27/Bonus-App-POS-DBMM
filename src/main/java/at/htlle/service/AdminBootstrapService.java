package at.htlle.service;

import at.htlle.entity.Customer;
import at.htlle.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrapService {

    private static final String ADMIN_USERNAME = "Admin";
    private static final String ADMIN_EMAIL = "admin@bonus.local";
    private static final String ADMIN_PASSWORD = "123";

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrapService(CustomerRepository customerRepository,
                                 PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void ensureAdminUser() {
        Customer admin = customerRepository.findByUsernameIgnoreCase(ADMIN_USERNAME)
                .orElseGet(Customer::new);
        admin.setFirstName("Admin");
        admin.setLastName("Admin");
        admin.setEmail(ADMIN_EMAIL);
        admin.setUsername(ADMIN_USERNAME);
        admin.setRole(Customer.Role.ADMIN);
        admin.setStatus(Customer.Status.ACTIVE);
        if (!isBcrypt(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        }
        customerRepository.save(admin);
    }

    private boolean isBcrypt(String password) {
        return password != null && password.startsWith("$2");
    }
}
