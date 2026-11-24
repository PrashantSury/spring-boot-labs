package ledgerly;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Finished. */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
  Optional<AppUser> findByUsername(String username);

  boolean existsByUsername(String username);
}
