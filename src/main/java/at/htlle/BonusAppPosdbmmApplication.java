package at.htlle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BonusAppPosdbmmApplication {

    public static void main(String[] args) {
        SpringApplication.run(BonusAppPosdbmmApplication.class, args);
    }
}
