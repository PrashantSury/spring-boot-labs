package ledgerly.cycle;


/** Builds a final statement. It needs AccountService for the balance. */
// Task 5: break the circular dependency
//
// AccountService needs StatementService, and StatementService needs
// AccountService, both through the constructor. Spring cannot build either
// one first: BeanCurrentlyInCreationException.
//
// Break the cycle WITHOUT field injection and WITHOUT changing AccountService:
// keep the final field and the constructor here, but let Spring hand in a
// stand-in that finds the real AccountService on first use. One annotation
// on the parameter does it (or use ObjectProvider<AccountService>).
public class StatementService {

  private final AccountService accounts;

  public StatementService(AccountService accounts) {
    this.accounts = accounts;
  }

  public String finalStatement(String account) {
    return "final balance " + accounts.balanceOf(account);
  }
}
