package ledgerly;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/** HTTP Basic; asha (USER) and ops (ADMIN), password = name + "-pass". */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  // Task 3: plug your two handlers in, in BOTH places:
  //   .httpBasic(b -> b.authenticationEntryPoint(entryPoint))      <- wrong password goes HERE
  //   .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(denied))
  @Bean
  SecurityFilterChain api(HttpSecurity http, ProblemEntryPoint entryPoint, ProblemDeniedHandler denied) throws Exception {
    http.authorizeHttpRequests(a -> a
            .requestMatchers("/public/**", "/error").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults())
        .csrf(c -> c.disable());
    return http.build();
  }

  @Bean
  UserDetailsService users() {
    return new InMemoryUserDetailsManager(
        User.withUsername("asha").password("{noop}asha-pass").roles("USER").build(),
        User.withUsername("ops").password("{noop}ops-pass").roles("ADMIN").build());
  }
}
