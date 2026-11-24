package ledgerly;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ownership rules, used from @PreAuthorize / @PostAuthorize as @ownership.owns(...) (the bean name is "ownership").
 * Never trust an account number from the client: check it belongs to the logged-in user.
 */
@Component
public class Ownership {

  private final AccountRepository accounts;

  public Ownership(AccountRepository accounts) {
    this.accounts = accounts;
  }

  // Task 2 (part 1): true when the account exists AND its customer's username equals auth.getName().
  //   Unknown account -> false (don't tell a stranger which numbers exist).
  @Transactional(readOnly = true)
  public boolean owns(Authentication auth, String accountNumber) {
    throw new UnsupportedOperationException("Task 2: Ownership.owns");
  }

  // Task 3 (part 1): a customer may see a transfer when they own the 'from' OR the 'to' account.
  @Transactional(readOnly = true)
  public boolean involved(Authentication auth, TransferDto transfer) {
    throw new UnsupportedOperationException("Task 3: Ownership.involved");
  }
}
