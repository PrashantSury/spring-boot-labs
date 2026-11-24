package ledgerly.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/** In-memory storage. Finished; NOT one of your tasks. Only storage, no rules. */
@Repository
public class CustomerRepository {

  private final Map<Long, Customer> rows = new ConcurrentHashMap<>();
  private final AtomicLong ids = new AtomicLong(100);

  public CustomerRepository() {
    save("Asha Rao", "ABCPR1234F", "4821", "VIP; called about loan", List.of(50_000L, -1_200L, -800L));
  }

  public Customer save(String name, String pan, String pin, String notes, List<Long> transactions) {
    Customer c = new Customer(ids.incrementAndGet(), name, pan, pin, notes, new ArrayList<>(transactions));
    rows.put(c.id(), c);
    return c;
  }

  public Optional<Customer> findById(long id) {
    return Optional.ofNullable(rows.get(id));
  }

  public boolean existsByPan(String pan) {
    return rows.values().stream().anyMatch(c -> c.pan().equals(pan));
  }
}
