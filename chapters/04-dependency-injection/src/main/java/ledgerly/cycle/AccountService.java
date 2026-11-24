package ledgerly.cycle;

import java.util.Map;

/**
 * Closing an account prints a final statement. Finished; NOT one of your
 * tasks. The test (and the tour) register this class and StatementService
 * as beans. The problem is in StatementService: task 5.
 */
public class AccountService {

  private static final Map<String, Long> BALANCES = Map.of("ACC-1", 52_000L, "ACC-2", 700L);

  private final StatementService statements;

  public AccountService(StatementService statements) {
    this.statements = statements;
  }

  public long balanceOf(String account) {
    return BALANCES.getOrDefault(account, 0L);
  }

  public String close(String account) {
    return "closed " + account + " | " + statements.finalStatement(account);
  }
}
