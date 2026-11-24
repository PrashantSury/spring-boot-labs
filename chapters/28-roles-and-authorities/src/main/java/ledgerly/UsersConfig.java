package ledgerly;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Users with ROLES (ROLE_ prefix, "who you are") and fine-grained AUTHORITIES ("what you may do").
 * Finished; NOT one of your tasks.
 */
@Configuration
public class UsersConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService users(PasswordEncoder enc) {
    return new InMemoryUserDetailsManager(
        User.withUsername("asha").password(enc.encode("asha-pass")).authorities("ROLE_USER", "transfer:write").build(),
        User.withUsername("meera").password(enc.encode("meera-pass")).authorities("ROLE_USER").build(),
        User.withUsername("ravi").password(enc.encode("ravi-pass")).authorities("ROLE_TELLER", "report:read").build(),
        User.withUsername("ops").password(enc.encode("ops-pass")).authorities("ROLE_ADMIN").build());
  }
}
