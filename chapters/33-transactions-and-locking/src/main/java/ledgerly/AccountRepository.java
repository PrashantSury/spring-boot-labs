package ledgerly;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

  // Task 2: pessimistic lock: SELECT ... FOR UPDATE. Other transactions asking for the same row WAIT.
  //   @Lock(LockModeType.PESSIMISTIC_WRITE)
  //   @Query("select a from Account a where a.id = :id")
  //   Optional<Account> findForUpdate(@Param("id") long id);
  default Optional<Account> findForUpdate(long id) {
    throw new UnsupportedOperationException("Task 2: findForUpdate with @Lock(PESSIMISTIC_WRITE)");
  }
}
