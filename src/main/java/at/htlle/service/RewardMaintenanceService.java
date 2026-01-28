package at.htlle.service;

import at.htlle.repository.RewardRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RewardMaintenanceService {

    private final RewardRepository rewardRepository;

    public RewardMaintenanceService(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeExpiredRewards() {
        rewardRepository.deleteExpiredRewards(LocalDate.now());
    }
}
