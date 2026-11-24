package ledgerly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The application. No web server: the tour prints and exits.
 * Task 3 (part 1): switch on @Async with @EnableAsync.
 * Task 4 (part 1): switch on @Scheduled with @EnableScheduling.
 */
@SpringBootApplication
public class LedgerlyApplication {

  public static void main(String[] args) {
    SpringApplication.run(LedgerlyApplication.class, args);
  }
}
