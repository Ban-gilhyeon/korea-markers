package ban.koreamarkers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ban.koreamarkers.repository")
@EnableJpaAuditing
public class KoreaMarkersApplication {

    public static void main(String[] args) {
        SpringApplication.run(KoreaMarkersApplication.class, args);
    }

}
