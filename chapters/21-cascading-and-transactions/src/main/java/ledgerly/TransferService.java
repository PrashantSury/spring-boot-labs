package ledgerly;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Money movement. Each method saves after every step, like the first Ledgerly version did. */
@Service
public class TransferService {

  private final AccountRepository accounts;

  public TransferService(AccountRepository accounts, PaymentWorker worker) {
    this.accounts = accounts;
  }

  // Task 2: debit and credit must succeed or fail TOGETHER.
  //   Without a transaction every save() commits on its own: a transfer to ACC-9 debits ACC-1,
  //   then fails, and the money is gone. One annotation fixes it (unchecked exceptions roll back).
  public void transfer(String fromNo, String toNo, long amount) {
    Account from = find(fromNo);
    from.debit(amount);
    accounts.save(from);
    Account to = find(toNo);
    to.credit(amount);
    accounts.save(to);
  }

  // Task 3: same, but the daily-limit check comes AFTER the debit and throws a CHECKED exception.
  //   @Transactional alone commits on checked exceptions. Tell it to roll back for this one:
  //   @Transactional(rollbackFor = DailyLimitExceededException.class)
  @Transactional
  public void transferWithLimit(String fromNo, String toNo, long amount) throws DailyLimitExceededException {
    Account from = find(fromNo);
    from.debit(amount);
    accounts.save(from);
    if (amount > 5000) {
      throw new DailyLimitExceededException(amount);
    }
    Account to = find(toNo);
    to.credit(amount);
    accounts.save(to);
  }

  // Task 4 (part 2): a salary batch. Each payment must be all-or-nothing on its OWN, and one bad
  //   payment must not stop or undo the others. Returns the payments that failed.
  //   The stub calls this.payOne(...): a call inside the same object skips the Spring proxy, so
  //   @Transactional on payOne is IGNORED. Call worker.pay(p) instead and delete payOne.
  public List<Payment> payAll(List<Payment> batch) {
    List<Payment> failed = new ArrayList<>();
    for (Payment p : batch) {
      try {
        payOne(p);
      } catch (RuntimeException e) {
        failed.add(p);
      }
    }
    return failed;
  }
  
  @Transactional
  public void payOne(Payment p) {
    Account from = find(p.from());
    from.debit(p.amount());
    accounts.save(from);
    Account to = find(p.to());
    to.credit(p.amount());
    accounts.save(to);
  }

  private Account find(String number) {
    return accounts.findByNumber(number).orElseThrow(() -> new AccountNotFoundException(number));
  }
}
