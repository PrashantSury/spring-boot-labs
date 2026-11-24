package ledgerly;

import org.springframework.stereotype.Component;

/** Prints statement lines. Finished; NOT one of your tasks. It is @Metered. */
@Metered
@Component
public class StatementPrinter {

  public String line(String account, long amount) {
    return account + " " + amount;
  }

  /** Calls line() twice ON ITSELF (this.line). Watch what the counter says. */
  public String statement(String account) {
    return line(account, 500) + " / " + line(account, -200);
  }
}
