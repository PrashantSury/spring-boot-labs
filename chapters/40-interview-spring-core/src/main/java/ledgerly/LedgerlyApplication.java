package ledgerly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The application. No web server: the tour prints and exits.
 * Task 3 (part 2): let Boot find @ConfigurationProperties records with @ConfigurationPropertiesScan.
 */
@SpringBootApplication
public class LedgerlyApplication {

  public static void main(String[] args) {
    SpringApplication.run(LedgerlyApplication.class, args);
  }
}
