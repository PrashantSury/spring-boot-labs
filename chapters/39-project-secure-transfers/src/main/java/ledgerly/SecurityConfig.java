package ledgerly;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security. The filter, the 401/403 bodies and the login are finished.
 * Task 1 (part 1): switch on @PreAuthorize / @PostAuthorize with @EnableMethodSecurity.
 */
@Configuration
public class SecurityConfig {

  // Task 1 (part 2): coarse URL rules first (cheap, and they protect whole areas), BEFORE anyRequest():
  //   /audit/**            -> hasRole("AUDITOR")
  //   POST /transfers/**   -> hasAnyRole("CUSTOMER", "TELLER")      (an auditor never moves money)
  //   The fine "whose account?" rules are method security (tasks 2 and 3).
  @Bean
  SecurityFilterChain api(HttpSecurity http, TokenService tokens) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/error").permitAll()
            .anyRequest().authenticated())
        .csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(new JwtAuthFilter(tokens), UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(e -> e
            .authenticationEntryPoint((q, r, x) -> problem(r, 401, "Unauthorized", "Login required", q.getRequestURI()))
            .accessDeniedHandler((q, r, x) -> problem(r, 403, "Forbidden", "You may not do this", q.getRequestURI())));
    return http.build();
  }

  private static void problem(jakarta.servlet.http.HttpServletResponse r, int status, String title, String detail,
      String path) throws java.io.IOException {
    r.setStatus(status);
    r.setContentType("application/problem+json");
    r.getWriter().write("{\"title\":\"" + title + "\",\"status\":" + status + ",\"detail\":\"" + detail
        + "\",\"instance\":\"" + path + "\"}");
  }
}
