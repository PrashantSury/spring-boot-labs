package ledgerly;

import org.springframework.data.jpa.repository.JpaRepository;

/** Finished. */
public interface PayeeRepository extends JpaRepository<Payee, Long> {

  boolean existsByAccountNumber(String accountNumber);
}
