package ledgerly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Task 1 (part 1): switch caching on with @EnableCaching. */
@SpringBootApplication
public class LedgerlyApplication {

  public static void main(String[] args) {
    SpringApplication.run(LedgerlyApplication.class, args);
  }
}
