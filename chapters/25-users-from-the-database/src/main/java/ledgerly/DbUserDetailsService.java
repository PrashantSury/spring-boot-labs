package ledgerly;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Task 1: Spring Security asks THIS bean "who is asha?" on every login.
 * Load the AppUser by username and turn it into a UserDetails:
 *   User.withUsername(u.getUsername()).password(u.getPasswordHash()).roles(u.getRole())
 *       .disabled(!u.isEnabled()).build()
 * Unknown username -> throw new UsernameNotFoundException(...).
 */
@Service
public class DbUserDetailsService implements UserDetailsService {

  private final AppUserRepository users;

  public DbUserDetailsService(AppUserRepository users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    throw new UsernameNotFoundException("Task 1: load " + username + " from the database");
  }
}
