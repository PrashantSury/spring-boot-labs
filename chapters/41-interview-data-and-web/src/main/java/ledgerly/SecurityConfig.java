package ledgerly;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * HTTP Basic for the lab (asha and ravi are customers 1 and 2, ops is ADMIN; password = name + "-pass").
 * Task 4 (part 1): @EnableMethodSecurity, and only ADMIN may list all customers: GET /customers -> hasRole("ADMIN").
 */
@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain api(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults())
        .csrf(c -> c.disable());
    return http.build();
  }

  @Bean
  UserDetailsService users() {
    var enc = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    return new InMemoryUserDetailsManager(
        User.withUsername("asha").password(enc.encode("asha-pass")).roles("CUSTOMER").build(),
        User.withUsername("ravi").password(enc.encode("ravi-pass")).roles("CUSTOMER").build(),
        User.withUsername("ops").password(enc.encode("ops-pass")).roles("ADMIN").build());
  }
}
