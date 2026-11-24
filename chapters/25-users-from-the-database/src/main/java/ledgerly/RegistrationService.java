package ledgerly;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Opens a new login. */
@Service
public class RegistrationService {

  private final AppUserRepository users;
  private final PasswordEncoder encoder;

  public RegistrationService(AppUserRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  // Task 2: the stub stores the password as typed: anyone who can read the table can log in as anyone.
  //   - a taken username -> throw new DuplicateUserException(username)
  //   - store encoder.encode(rawPassword), never rawPassword
  //   - role USER, enabled
  public long register(String username, String rawPassword) {
    return users.save(new AppUser(username, rawPassword, "USER", true)).getId();
  }
}
