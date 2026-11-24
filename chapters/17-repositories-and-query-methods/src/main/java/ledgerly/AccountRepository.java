package ledgerly;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data writes the implementation of this interface at startup.
 * save, saveAll, findById, findAll, count, deleteById ... come free from JpaRepository.
 *
 * <p>Each method below is a stub with a default body that throws. For each task, delete the
 * default body (end the line with ;) so Spring Data implements it from the NAME, or add a @Query.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

  // Task 1: derived query. Spring reads the name: find By Number.
  default Optional<Account> findByNumber(String number) {
    throw new UnsupportedOperationException("Task 1: let Spring Data derive this from the method name");
  }

  // Task 2: accounts of one customer with balance strictly greater than min, biggest first.
  //   Rename the method so the name says all of that, e.g.
  //   findByCustomerIdAndBalanceGreaterThanOrderByBalanceDesc. The test calls THIS name, so keep it.
  default List<Account> findByCustomerIdAndBalanceGreaterThanOrderByBalanceDesc(long customerId, BigDecimal min) {
    throw new UnsupportedOperationException("Task 2: derived query with And, GreaterThan and OrderBy");
  }

  // Task 3: count and exists, derived too.
  default long countByStatus(AccountStatus status) {
    throw new UnsupportedOperationException("Task 3: countBy...");
  }
  
  default boolean existsByNumber(String number) {
    throw new UnsupportedOperationException("Task 3: existsBy...");
  }

  // Task 4a: a JPQL @Query (entity and field names, NOT table and column names):
  //   owner contains :name, case-insensitive, ordered by number.
  //   select a from Account a where lower(a.owner) like lower(concat('%', :name, '%')) order by a.number
  default List<Account> searchOwner(String name) {
    throw new UnsupportedOperationException("Task 4a: @Query with JPQL and a named parameter");
  }

  // Task 4b: a bulk update: freeze every account of a customer, return how many rows changed.
  //   Needs @Modifying, and @Transactional (a write needs a transaction; without it the tour fails with
  //   'No active transaction for update or delete query'). A bulk update skips the persistence context, so objects already loaded
  //   would still say ACTIVE. Use @Modifying(clearAutomatically = true).
  default int freezeAllOfCustomer(long customerId) {
    throw new UnsupportedOperationException("Task 4b: @Modifying @Query update");
  }
}
