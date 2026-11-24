package ledgerly;

import java.util.ArrayList;
import java.util.List;

/** 5 customers with 2 accounts each. Finished. */
public final class SampleData {

  private SampleData() {}

  public static final List<String> NAMES = List.of("Asha Rao", "Vikram Shah", "Meera Iyer", "Rohan Das", "Zoya Khan");

  /** Saves the customers and their accounts; ACC-n1 has 1000*n, ACC-n2 has 50000*n. */
  public static void load(CustomerRepository customers, AccountRepository accounts) {
    List<Account> all = new ArrayList<>();
    int n = 1;
    for (String name : NAMES) {
      Customer c = customers.save(new Customer(name));
      Account small = new Account("ACC-" + n + "1", 1000L * n);
      Account big = new Account("ACC-" + n + "2", 50_000L * n);
      c.addAccount(small);
      c.addAccount(big);
      all.add(small);
      all.add(big);
      n++;
    }
    accounts.saveAll(all);
  }
}
