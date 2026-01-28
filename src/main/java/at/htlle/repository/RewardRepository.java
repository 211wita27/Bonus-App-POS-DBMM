package at.htlle.repository;

import at.htlle.entity.Reward;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    Optional<Reward> findByRestaurantIdAndRewardCode(Long restaurantId, String rewardCode);

    List<Reward> findByRestaurantIdAndActiveTrue(Long restaurantId);

    List<Reward> findByRestaurantId(Long restaurantId);

    List<Reward> findByRestaurantIdOrderByIdDesc(Long restaurantId);

    @Query("select r from Reward r where r.restaurant.id = :restaurantId and r.active = true " +
            "and (r.validFrom is null or r.validFrom <= :referenceDate) " +
            "and (r.validUntil is null or r.validUntil >= :referenceDate)")
    List<Reward> findActiveRewardsForDate(@Param("restaurantId") Long restaurantId,
                                          @Param("referenceDate") LocalDate referenceDate);

    @org.springframework.data.jpa.repository.Modifying
    @Query("delete from Reward r where r.validUntil is not null and r.validUntil < :cutoff")
    int deleteExpiredRewards(@Param("cutoff") LocalDate cutoff);
}
