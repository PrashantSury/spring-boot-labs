package ledgerly;

import java.math.BigDecimal;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seed data, loaded at startup and by the tests before each check. Finished.
 *   Asha: ACC-1 = 1000.00      Ravi: ACC-2 = 500.00, ACC-3 = 0.00 (frozen: can't receive)
 */
@Component
public class DemoData implements ApplicationRunner {

  private final AccountRepository accounts;
  private final TransferRepository transfers;
  private final CustomerRepository customers;

  public DemoData(AccountRepository accounts, TransferRepository transfers, CustomerRepository customers) {
    this.accounts = accounts;
    this.transfers = transfers;
    this.customers = customers;
  }

  @Override
  public void run(ApplicationArguments args) {
    reset();
  }

  @Transactional
  public void reset() {
    transfers.deleteAllInBatch();
    accounts.deleteAllInBatch();
    customers.deleteAllInBatch();
    Customer asha = customers.save(new Customer(1L, "Asha"));
    Customer ravi = customers.save(new Customer(2L, "Ravi"));
    accounts.save(new Account(1L, "ACC-1", asha, new BigDecimal("1000.00"), false));
    accounts.save(new Account(2L, "ACC-2", ravi, new BigDecimal("500.00"), false));
    accounts.save(new Account(3L, "ACC-3", ravi, new BigDecimal("0.00"), true));
  }
}
