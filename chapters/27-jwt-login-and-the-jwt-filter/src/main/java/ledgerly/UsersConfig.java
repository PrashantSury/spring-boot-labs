package ledgerly;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/** Two demo users in memory (chapter 25 moves them to the database). Finished; NOT one of your tasks. */
@Configuration
public class UsersConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService users(PasswordEncoder encoder) {
    return new InMemoryUserDetailsManager(
        User.withUsername("asha").password(encoder.encode("asha-pass")).roles("USER").build(),
        User.withUsername("ops").password(encoder.encode("ops-pass")).roles("USER", "ADMIN").build());
  }
}
