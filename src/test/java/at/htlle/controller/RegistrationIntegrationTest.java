package at.htlle.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.htlle.entity.AppUser;
import at.htlle.entity.Customer;
import at.htlle.entity.LoyaltyAccount;
import at.htlle.repository.AppUserRepository;
import at.htlle.repository.CustomerRepository;
import at.htlle.repository.LoyaltyAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Test
    void registrationCreatesCustomerUserAndAccount() throws Exception {
        String email = "new.customer@example.com";

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("firstName", "Nina")
                        .param("lastName", "Novak")
                        .param("email", email)
                        .param("phoneNumber", "+43123456789")
                        .param("password", "Welcome123")
                        .param("confirmPassword", "Welcome123")
                        .param("restaurantCode", "DEMO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        AppUser user = appUserRepository.findByEmail(email).orElseThrow();
        Customer customer = customerRepository.findByEmail(email).orElseThrow();
        LoyaltyAccount account = loyaltyAccountRepository.findByCustomerId(customer.getId()).stream().findFirst().orElseThrow();

        assertThat(user.getCustomer().getId()).isEqualTo(customer.getId());
        assertThat(account.getCustomer().getId()).isEqualTo(customer.getId());
        assertThat(account.getAccountNumber()).isNotBlank();
    }
}
