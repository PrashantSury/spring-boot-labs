package ledgerly;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** The nightly interest job, used by the tests to update an account "at the same time". Finished. */
@Service
public class Interest {

  private final AccountRepository accounts;

  public Interest(AccountRepository accounts) {
    this.accounts = accounts;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void credit(long id, BigDecimal amount) {
    Account a = accounts.findById(id).orElseThrow();
    a.setBalance(a.getBalance().add(amount));
  }
}
