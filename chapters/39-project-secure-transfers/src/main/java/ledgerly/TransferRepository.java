package ledgerly;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Finished. */
public interface TransferRepository extends JpaRepository<Transfer, Long> {

  Optional<Transfer> findByIdempotencyKey(String idempotencyKey);
}
