package ledgerly;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** At startup: asha/asha-pass (USER), ops/ops-pass (ADMIN), ravi/ravi-pass (USER, DISABLED). Finished. */
@Component
public class SampleUsers implements ApplicationRunner {

  private final AppUserRepository users;
  private final PasswordEncoder encoder;

  public SampleUsers(AppUserRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    users.save(new AppUser("asha", encoder.encode("asha-pass"), "USER", true));
    users.save(new AppUser("ops", encoder.encode("ops-pass"), "ADMIN", true));
    users.save(new AppUser("ravi", encoder.encode("ravi-pass"), "USER", false));
  }
}
