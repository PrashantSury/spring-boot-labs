package ledgerly;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** The chain from chapter 24 (finished) plus the AuthenticationManager (task 3). */
@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain api(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/error").permitAll()   // /error: so a 409 from @ResponseStatus is not turned into 401
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults())
        .csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }

  /** Finished: BCrypt, cost 10 (about 70 ms per check: slow on purpose). */
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // Task 3 (part 1): the AuthenticationManager that /auth/check uses.
  //   A DaoAuthenticationProvider loads the user with YOUR UserDetailsService (task 1) and
  //   compares passwords with the PasswordEncoder:
  //     DaoAuthenticationProvider p = new DaoAuthenticationProvider(users); p.setPasswordEncoder(encoder);
  //     return new ProviderManager(p);
  @Bean
  AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder encoder) {
    return authentication -> {
      throw new BadCredentialsException("Task 3: build a ProviderManager with a DaoAuthenticationProvider");
    };
  }
}
