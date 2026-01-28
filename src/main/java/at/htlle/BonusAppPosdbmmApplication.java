package at.htlle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot entry point for the Bonus App POS DBMM application.
 */
@SpringBootApplication
@EnableScheduling
public class BonusAppPosdbmmApplication {

    /**
     * Boots the Spring application context.
     *
     * @param args application arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BonusAppPosdbmmApplication.class, args);
    }
}
