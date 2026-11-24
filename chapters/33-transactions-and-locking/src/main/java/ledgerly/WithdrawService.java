package ledgerly;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;

/** The entry point. NOT @Transactional: each attempt gets a fresh transaction in WithdrawWorker. */
@Service
public class WithdrawService {

  static final int MAX_ATTEMPTS = 3;
  private final WithdrawWorker worker;

  public WithdrawService(WithdrawWorker worker) {
    this.worker = worker;
  }

  // Task 3: retry on a version conflict. Call worker.withdrawOnce(id, amount); on
  //   ObjectOptimisticLockingFailureException try again (a NEW transaction re-reads the fresh balance),
  //   at most MAX_ATTEMPTS times in total, then re-throw the last exception.
  //   Do NOT retry InsufficientFunds. Return the new balance.
  public BigDecimal withdrawWithRetry(long id, BigDecimal amount) {
    return worker.withdrawOnce(id, amount);
  }
}
