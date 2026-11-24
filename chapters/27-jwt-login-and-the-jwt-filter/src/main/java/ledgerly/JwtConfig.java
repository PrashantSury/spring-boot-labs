package ledgerly;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/** The codec, a clock and the AuthenticationManager (chapter 25). Finished; NOT one of your tasks. */
@Configuration
public class JwtConfig {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  JwtCodec jwtCodec(@Value("${ledgerly.jwt.secret}") String secret, Clock clock) {
    return new JwtCodec(secret.getBytes(StandardCharsets.UTF_8), clock);
  }

  @Bean
  AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder encoder) {
    DaoAuthenticationProvider p = new DaoAuthenticationProvider(users);
    p.setPasswordEncoder(encoder);
    return new ProviderManager(p);
  }
}
