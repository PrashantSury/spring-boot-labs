package ledgerly;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** URL rules by role and by authority. */
@Configuration
public class SecurityConfig {

  // Task 3: the rules (top to bottom, first match wins), BEFORE anyRequest():
  //   POST /transfers/**  -> hasAuthority("transfer:write")   (an authority: no ROLE_ prefix)
  //   /reports/**         -> hasAuthority("report:read")
  //   /teller/**          -> hasRole("TELLER")                (checks ROLE_TELLER)
  //   /admin/**           -> hasRole("ADMIN")
  @Bean
  SecurityFilterChain api(HttpSecurity http, TokenService tokens) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/error").permitAll()
            .anyRequest().authenticated())
        .csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(new JwtAuthFilter(tokens), UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(e -> e
            .authenticationEntryPoint((q, r, x) -> json(r, 401, "Login required"))
            .accessDeniedHandler((q, r, x) -> json(r, 403, "Not allowed")));
    return http.build();
  }

  // Task 4: a role hierarchy: an ADMIN can do everything a TELLER can, and a TELLER everything a USER can.
  //   @Bean static RoleHierarchy roleHierarchy() {
  //     return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_TELLER\nROLE_TELLER > ROLE_USER"); }
  //   (Spring Security picks the bean up for hasRole checks.)
  private static void json(jakarta.servlet.http.HttpServletResponse r, int status, String error) throws java.io.IOException {
    r.setStatus(status);
    r.setContentType("application/json");
    r.getWriter().write("{\"status\":" + status + ",\"error\":\"" + error + "\"}");
  }
}
