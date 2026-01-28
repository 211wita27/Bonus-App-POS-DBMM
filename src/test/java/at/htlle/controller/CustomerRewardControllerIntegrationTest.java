package at.htlle.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import at.htlle.entity.LoyaltyAccount;
import at.htlle.entity.Reward;
import at.htlle.repository.LoyaltyAccountRepository;
import at.htlle.repository.RewardRepository;
import java.time.LocalDate;
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
class CustomerRewardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Autowired
    private RewardRepository rewardRepository;

    @Test
    @WithMockUser(username = "user@user", roles = "CUSTOMER")
    void rewardsPageLoadsForCustomer() throws Exception {
        mockMvc.perform(get("/customer/rewards"))
                .andExpect(status().isOk())
                .andExpect(view().name("rewards"))
                .andExpect(content().string(containsString("Redeem Your Points")));
    }

    @Test
    @WithMockUser(username = "user@user", roles = "CUSTOMER")
    void redeemRewardShowsSuccessView() throws Exception {
        LoyaltyAccount account = loyaltyAccountRepository.findByAccountNumber("ACCT-U001")
                .orElseThrow();
        Reward reward = rewardRepository
                .findActiveRewardsForDate(account.getRestaurant().getId(), LocalDate.now())
                .stream()
                .filter(candidate -> candidate.getCostPoints() <= account.getCurrentPoints())
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/customer/rewards/redeem")
                        .with(csrf())
                        .param("accountId", account.getId().toString())
                        .param("rewardId", reward.getId().toString())
                        .param("notes", "Test redemption"))
                .andExpect(status().isOk())
                .andExpect(view().name("redemption-success"))
                .andExpect(content().string(containsString("Reward Redeemed")));
    }
}
