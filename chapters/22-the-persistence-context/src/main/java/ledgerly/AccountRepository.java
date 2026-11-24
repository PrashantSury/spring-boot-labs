package ledgerly;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Finished. */
public interface AccountRepository extends JpaRepository<Account, Long> {
  Optional<Account> findByNumber(String number);
}
