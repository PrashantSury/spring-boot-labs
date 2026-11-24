package ledgerly;

import org.springframework.context.annotation.Configuration;

/**
 * Who may call what. Without a SecurityFilterChain bean, Spring Boot locks EVERYTHING
 * (form login, a generated password, CSRF on). Write your own chain here.
 */
@Configuration
public class SecurityConfig {

  // Task 1: a SecurityFilterChain bean (http.authorizeHttpRequests(...).httpBasic(...); return http.build())
  //   /public/**  -> permitAll
  //   anything else -> authenticated
  //   HTTP Basic for login.
  // Task 2: /admin/** -> hasRole("ADMIN"). Rules are checked top to bottom, first match wins:
  //   put it BEFORE anyRequest().
  // Task 3: this is a stateless JSON API: csrf disabled, sessions STATELESS (no JSESSIONID cookie).
  // Task 4: JSON error bodies, so the app can tell the two cases apart:
  //   not logged in / wrong password -> 401  {"status":401,"error":"Login required"}
  //   logged in but not allowed       -> 403  {"status":403,"error":"Not allowed"}
  //   http.exceptionHandling(e -> e.authenticationEntryPoint(...).accessDeniedHandler(...))
  //   and give httpBasic the same entry point (b -> b.authenticationEntryPoint(...)).
}
