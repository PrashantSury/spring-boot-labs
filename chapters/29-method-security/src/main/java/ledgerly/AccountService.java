package ledgerly;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Bank operations. The method bodies are finished; your job is the security annotations.
 * Account 1 belongs to asha, account 2 to meera.
 */
@Service
public class AccountService {

  private final Map<Long, Account> accounts = new ConcurrentHashMap<>();

  public AccountService() {
    reset();
  }

  /** Finished: test helper. */
  public void reset() {
    accounts.clear();
    accounts.put(1L, new Account(1, "asha", new BigDecimal("82000.00")));
    accounts.put(2L, new Account(2, "meera", new BigDecimal("1500.00")));
    accounts.put(3L, new Account(3, "meera", new BigDecimal("0.00")));
  }

  // Task 1 (part 2): only ADMIN may close an account: @PreAuthorize("hasRole('ADMIN')")
  public void close(long id) {
    accounts.remove(id);
  }

  // Task 2: a user may list only THEIR OWN accounts, tellers may list anyone's:
  //   @PreAuthorize("#owner == authentication.name or hasRole('TELLER')")
  public List<Account> accountsOf(String owner) {
    return accounts.values().stream().filter(a -> a.owner().equals(owner)).toList();
  }

  // Task 3: we only know the owner AFTER loading, so check the returned object:
  //   @PostAuthorize("returnObject.owner() == authentication.name or hasRole('TELLER')")
  public Account find(long id) {
    Account a = accounts.get(id);
    if (a == null) throw new NoSuchElementException("no account " + id);
    return a;
  }

  // Task 4 (part 2): @PreAuthorize("@transferLimits.allowed(authentication, #amount)")
  public String transfer(long from, long to, BigDecimal amount) {
    return "moved " + amount + " from " + from + " to " + to;
  }

  /**
   * Finished EXAMPLE of the proxy trap: this.close(...) skips the proxy, so @PreAuthorize on close never runs.
   * Do not "fix" it here; the test documents the behaviour.
   */
  public int closeEmptyAccounts() {
    List<Long> empty = accounts.values().stream().filter(a -> a.balance().signum() == 0).map(Account::id).toList();
    empty.forEach(this::close);
    return empty.size();
  }
}
