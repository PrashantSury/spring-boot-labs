package ledgerly;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Lab users (password = name + "-pass"). Finished; NOT one of your tasks.
 *   asha, ravi, meera: CUSTOMER (each owns accounts, see DemoData)
 *   tara: TELLER (works at the branch counter: may read and move money for any customer)
 *   audit: AUDITOR (reads everything, never moves money)
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
        User.withUsername("asha").password(enc.encode("asha-pass")).roles("CUSTOMER").build(),
        User.withUsername("ravi").password(enc.encode("ravi-pass")).roles("CUSTOMER").build(),
        User.withUsername("meera").password(enc.encode("meera-pass")).roles("CUSTOMER").build(),
        User.withUsername("tara").password(enc.encode("tara-pass")).roles("TELLER").build(),
        User.withUsername("audit").password(enc.encode("audit-pass")).roles("AUDITOR").build());
  }
}
