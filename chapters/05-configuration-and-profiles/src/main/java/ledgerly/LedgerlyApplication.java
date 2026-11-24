package ledgerly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** The application. Task 2 adds one annotation here. */
// Task 2 (part 2): a @ConfigurationProperties record is NOT a bean by itself.
// Add @ConfigurationPropertiesScan so Spring finds and binds TransferProperties.
@SpringBootApplication
public class LedgerlyApplication {

  public static void main(String[] args) {
    SpringApplication.run(LedgerlyApplication.class, args);
  }
}
