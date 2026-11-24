package ledgerly;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** One withdrawal attempt per transaction. Finished; NOT one of your tasks (except calling it). */
@Service
public class WithdrawWorker {

  private final AccountRepository accounts;
  private final AuditLog audit;
  /** Test hook: runs after reading the balance and before the UPDATE (simulates another user). */
  private volatile Runnable afterRead = () -> {};

  public WithdrawWorker(AccountRepository accounts, AuditLog audit) {
    this.accounts = accounts;
    this.audit = audit;
  }

  /** Test hook setter. A method, not a field: through the transaction proxy a field write would miss the real bean. */
  public void setAfterRead(Runnable hook) {
    this.afterRead = hook;
  }

  /** Optimistic: plain read; the @Version check happens at commit. */
  @Transactional
  public BigDecimal withdrawOnce(long id, BigDecimal amount) {
    Account a = accounts.findById(id).orElseThrow();
    audit.record("withdraw " + amount + " from " + id);
    afterRead.run();
    if (a.getBalance().compareTo(amount) < 0) throw new InsufficientFunds("balance " + a.getBalance());
    a.setBalance(a.getBalance().subtract(amount));
    return a.getBalance();
  }

  /** Pessimistic: SELECT ... FOR UPDATE, then the same logic. */
  @Transactional
  public BigDecimal withdrawLocked(long id, BigDecimal amount) {
    Account a = accounts.findForUpdate(id).orElseThrow();
    if (a.getBalance().compareTo(amount) < 0) throw new InsufficientFunds("balance " + a.getBalance());
    a.setBalance(a.getBalance().subtract(amount));
    return a.getBalance();
  }
}
