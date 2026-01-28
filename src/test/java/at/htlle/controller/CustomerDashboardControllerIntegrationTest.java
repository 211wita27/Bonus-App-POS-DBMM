package at.htlle.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import at.htlle.repository.LoyaltyAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerDashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Test
    @WithMockUser(username = "user@user", roles = "CUSTOMER")
    void dashboardLoadsForCustomer() throws Exception {
        mockMvc.perform(get("/customer/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(containsString("Customer Dashboard")));
    }

    @Test
    @WithMockUser(username = "user@user", roles = "CUSTOMER")
    void dashboardShowsSelectedRestaurant() throws Exception {
        Long accountId = loyaltyAccountRepository.findByAccountNumber("ACCT-U002")
                .orElseThrow()
                .getId();

        mockMvc.perform(get("/customer/dashboard").param("accountId", accountId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(containsString("Sushi Harbor")));
    }
}
