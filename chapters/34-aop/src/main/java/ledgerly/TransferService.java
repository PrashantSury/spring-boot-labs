package ledgerly;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

/** Moves money between accounts. Business logic is finished; only task 4 touches this file. */
@Service
public class TransferService {

  private final Map<String, BigDecimal> balances = new ConcurrentHashMap<>();
  private final AtomicInteger txSeq = new AtomicInteger();

  public TransferService() {
    reset();
  }

  /** Test helper: ACC-1 = 1000, ACC-2 = 500, ACC-3 = 0, transaction ids start again at TX-1. */
  public void reset() {
    balances.clear();
    balances.put("ACC-1", new BigDecimal("1000"));
    balances.put("ACC-2", new BigDecimal("500"));
    balances.put("ACC-3", BigDecimal.ZERO);
    txSeq.set(0);
  }

  @Audited("transfer")
  public String transfer(String from, String to, BigDecimal amount) {
    if (amount.signum() <= 0) throw new IllegalArgumentException("amount must be positive");
    if (balances.get(from).compareTo(amount) < 0) throw new InsufficientFunds(from);
    balances.merge(from, amount.negate(), BigDecimal::add);
    balances.merge(to, amount, BigDecimal::add);
    return "TX-" + txSeq.incrementAndGet();
  }

  public BigDecimal balance(String account) {
    return balances.get(account);
  }

  // Task 4: payroll pays every line with transfer(...), and EVERY line must be audited and timed.
  //   Right now it calls this.transfer(...): a call inside the object never goes through the proxy,
  //   so the aspects never see it. Call transfer through the Spring proxy instead:
  //   take an ObjectProvider<TransferService> self in the constructor and use self.getObject().transfer(...).
  public List<String> payroll(String from, Map<String, BigDecimal> lines) {
    List<String> ids = new ArrayList<>();
    for (var line : new LinkedHashMap<>(lines).entrySet()) {
      ids.add(transfer(from, line.getKey(), line.getValue()));
    }
    return ids;
  }

  /** Finished EXAMPLE of the other proxy trap: a final method can't be overridden, so no aspect ever runs. */
  public final BigDecimal feeFor(BigDecimal amount) {
    return amount.compareTo(new BigDecimal("10000")) >= 0 ? new BigDecimal("25") : new BigDecimal("5");
  }
}
