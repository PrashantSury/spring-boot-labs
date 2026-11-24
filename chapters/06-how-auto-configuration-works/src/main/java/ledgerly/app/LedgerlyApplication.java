package ledgerly.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The application. Finished; NOT one of your tasks. It lives in package
 * ledgerly.app, so component scanning covers ledgerly.app only. The
 * auto-configuration in ledgerly.autoconfigure must be found another way.
 */
@SpringBootApplication
public class LedgerlyApplication {

  public static void main(String[] args) {
    SpringApplication.run(LedgerlyApplication.class, args);
  }
}
