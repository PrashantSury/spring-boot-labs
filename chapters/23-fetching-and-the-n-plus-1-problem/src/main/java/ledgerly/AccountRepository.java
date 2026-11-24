package ledgerly;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accounts. */
public interface AccountRepository extends JpaRepository<Account, Long> {

  // Task 2: this works, but printing each account's customer name sends 1 + N selects.
  //   Add @EntityGraph(attributePaths = "customer") so the customer comes in the same select (a join).
  List<Account> findByBalanceGreaterThanOrderByNumber(long min);
}
