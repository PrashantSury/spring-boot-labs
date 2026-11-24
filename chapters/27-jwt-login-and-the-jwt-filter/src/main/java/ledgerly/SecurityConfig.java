package ledgerly;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

/** The chain. Everything is finished except where the JWT filter goes. */
@Configuration
public class SecurityConfig {

  // Task 3: put the JWT filter INTO the security chain, before the username/password filter:
  //   .addFilterBefore(new JwtAuthFilter(codec, users), UsernamePasswordAuthenticationFilter.class)
  //   It must run before the authorization rules, which then see the user it set.
  @Bean
  SecurityFilterChain api(HttpSecurity http, JwtCodec codec, UserDetailsService users) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/error").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(e -> e
            .authenticationEntryPoint((req, res, ex) -> {
              res.setStatus(401);
              res.setContentType("application/json");
              res.getWriter().write("{\"error\":\"Login required\"}");
            }));
    return http.build();
  }
}
