package at.htlle.service;

import static org.mockito.Mockito.verify;

import at.htlle.repository.RewardRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RewardMaintenanceServiceTest {

    @Mock
    private RewardRepository rewardRepository;

    @Test
    void purgeExpiredRewardsUsesTodayAsCutoff() {
        RewardMaintenanceService service = new RewardMaintenanceService(rewardRepository);

        service.purgeExpiredRewards();

        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(rewardRepository).deleteExpiredRewards(captor.capture());
        assertThatSameDay(captor.getValue(), LocalDate.now());
    }

    private void assertThatSameDay(LocalDate actual, LocalDate expected) {
        if (!actual.equals(expected)) {
            throw new AssertionError("Expected cutoff date " + expected + " but was " + actual);
        }
    }
}
