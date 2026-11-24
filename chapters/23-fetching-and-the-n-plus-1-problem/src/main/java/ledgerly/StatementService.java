package ledgerly;

import java.util.List;
import org.springframework.stereotype.Service;

/** Branch reports. */
@Service
public class StatementService {

  private final CustomerRepository customers;

  public StatementService(CustomerRepository customers) {
    this.customers = customers;
  }

  // Task 4: "Asha Rao: 2 accounts" for every customer.
  //   findAll() returns, its persistence context closes, and then getAccounts().size() tries to
  //   load a lazy collection with no session: LazyInitializationException.
  //   Keep the session open for the whole method: @Transactional(readOnly = true).
  //   (With task 3 done this is 2 selects, not 1 + N.)
  public List<String> ownerSummaries() {
    return customers.findAll().stream()
        .map(c -> c.getName() + ": " + c.getAccounts().size() + " accounts")
        .sorted()
        .toList();
  }
}
