package ledgerly;

import java.math.BigDecimal;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Seed data: Asha (ACC-1, ACC-2), Ravi (ACC-3), Meera (ACC-4, ACC-5, ACC-6). No payees. Finished. */
@Component
public class DemoData implements ApplicationRunner {

  private final CustomerRepository customers;
  private final AccountRepository accounts;
  private final PayeeRepository payees;

  public DemoData(CustomerRepository customers, AccountRepository accounts, PayeeRepository payees) {
    this.customers = customers;
    this.accounts = accounts;
    this.payees = payees;
  }

  @Override
  public void run(ApplicationArguments args) {
    reset();
  }

  @Transactional
  public void reset() {
    payees.deleteAllInBatch();
    accounts.deleteAllInBatch();
    customers.deleteAllInBatch();
    Customer asha = customers.save(new Customer(1L, "Asha", "asha"));
    Customer ravi = customers.save(new Customer(2L, "Ravi", "ravi"));
    Customer meera = customers.save(new Customer(3L, "Meera", "meera"));
    accounts.save(new Account(1L, "ACC-1", asha, new BigDecimal("1000.00")));
    accounts.save(new Account(2L, "ACC-2", asha, new BigDecimal("250.00")));
    accounts.save(new Account(3L, "ACC-3", ravi, new BigDecimal("500.00")));
    accounts.save(new Account(4L, "ACC-4", meera, new BigDecimal("10.00")));
    accounts.save(new Account(5L, "ACC-5", meera, new BigDecimal("20.00")));
    accounts.save(new Account(6L, "ACC-6", meera, new BigDecimal("30.00")));
  }
}
