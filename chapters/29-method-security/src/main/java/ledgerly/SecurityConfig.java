package ledgerly;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * URL rules stay simple (logged in = OK); the real rules live on the service methods.
 * Users (password = name + "-pass"): asha, meera (USER), ravi (TELLER), ops (ADMIN).
 */
// Task 1 (part 1): switch method security on with @EnableMethodSecurity.
@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain api(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(a -> a.requestMatchers("/error").permitAll().anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults())
        .csrf(c -> c.disable())
        .exceptionHandling(e -> e.accessDeniedHandler((q, r, x) -> {
          r.setStatus(403);
          r.setContentType("application/json");
          r.getWriter().write("{\"status\":403,\"error\":\"Not allowed\"}");
        }));
    return http.build();
  }

  @Bean
  UserDetailsService users() {
    return new InMemoryUserDetailsManager(
        User.withUsername("asha").password("{noop}asha-pass").roles("USER").build(),
        User.withUsername("meera").password("{noop}meera-pass").roles("USER").build(),
        User.withUsername("ravi").password("{noop}ravi-pass").roles("TELLER").build(),
        User.withUsername("ops").password("{noop}ops-pass").roles("ADMIN").build());
  }
}
