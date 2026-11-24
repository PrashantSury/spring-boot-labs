package ledgerly;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Finished; NOT one of your tasks. At startup: 45 transactions for ACC-1 (UPI #1 on 2 Oct 2026
 * ... UPI #45 on 15 Nov 2026, amount = i*137 % 5000 + 100) and 3 for ACC-2.
 */
@Component
public class SampleData implements ApplicationRunner {

  private final TxnRepository repo;

  public SampleData(TxnRepository repo) {
    this.repo = repo;
  }

  @Override
  public void run(ApplicationArguments args) {
    List<Txn> txns = new ArrayList<>();
    for (int i = 1; i <= 45; i++) {
      txns.add(new Txn("ACC-1", i * 137 % 5000 + 100, LocalDate.of(2026, 10, 1).plusDays(i), "UPI #" + i));
    }
    for (int i = 1; i <= 3; i++) {
      txns.add(new Txn("ACC-2", 1000L * i, LocalDate.of(2026, 11, 1).plusDays(i), "NEFT #" + i));
    }
    repo.saveAll(txns);
  }
}
