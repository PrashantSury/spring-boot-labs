package ledgerly;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read side for customers. The bean name "customerQueries" is usable from @PreAuthorize. */
@Service
public class CustomerQueries {

  private final CustomerRepository customers;

  public CustomerQueries(CustomerRepository customers) {
    this.customers = customers;
  }

  // Task 1 (part 1): accounts is LAZY and open-in-view is off, so touching it outside a transaction throws
  //   LazyInitializationException (a 500). Make both methods @Transactional(readOnly = true).
  // Task 1 (part 2): list() runs 1 query for customers + 1 per customer for accounts (N+1). Use
  //   customers.findAllWithAccounts() (a join fetch you add to CustomerRepository).
  public List<Views.CustomerSummary> list() {
    return customers.findAll(org.springframework.data.domain.Sort.by("id")).stream()
        .map(c -> new Views.CustomerSummary(c.getId(), c.getName(), c.getAccounts().size())).toList();
  }

  // Task 4 (part 2): ADMIN may read anyone; a customer only themselves:
  //   @PreAuthorize("hasRole('ADMIN') or @customerQueries.isSelf(authentication, #id)")
  public Views.CustomerDetail detail(long id) {
    Customer c = customers.findById(id).orElseThrow(() -> new NoSuchThing("customer", id));
    return new Views.CustomerDetail(c.getId(), c.getName(),
        c.getAccounts().stream().map(a -> new Views.AccountLine(a.getNumber(), a.getBalance())).toList());
  }

  /** Finished: is customer #id the logged-in user? */
  @Transactional(readOnly = true)
  public boolean isSelf(Authentication auth, long id) {
    return customers.findById(id).map(c -> c.getUsername().equals(auth.getName())).orElse(false);
  }
}
