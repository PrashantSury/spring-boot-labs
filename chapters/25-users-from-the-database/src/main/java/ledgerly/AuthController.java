package ledgerly;

import java.security.Principal;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Finished; NOT one of your tasks. */
@RestController
public class AuthController {

  private final RegistrationService registration;
  private final AuthService auth;

  public AuthController(RegistrationService registration, AuthService auth) {
    this.registration = registration;
    this.auth = auth;
  }

  public record Credentials(String username, String password) {}

  @PostMapping("/auth/register")
  public ResponseEntity<String> register(@RequestBody Credentials c) {
    return ResponseEntity.status(201).body("user id " + registration.register(c.username(), c.password()));
  }

  @PostMapping("/auth/check")
  public ResponseEntity<Map<String, String>> check(@RequestBody Credentials c) {
    try {
      return ResponseEntity.ok(Map.of("user", auth.check(c.username(), c.password())));
    } catch (AuthenticationException e) {
      return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/me")
  public String me(Principal p) {
    return "hello " + p.getName();
  }

  @GetMapping("/admin/users")
  public String adminUsers() {
    return "3 users";
  }
}
