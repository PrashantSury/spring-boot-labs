package ledgerly;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Finished (the lock query is chapter 33's task). */
public interface AccountRepository extends JpaRepository<Account, Long> {

  Optional<Account> findByNumber(String number);

  /** SELECT ... FOR UPDATE: other transactions wanting this row wait until we commit. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from Account a where a.number = :number")
  Optional<Account> lockByNumber(@Param("number") String number);
}
