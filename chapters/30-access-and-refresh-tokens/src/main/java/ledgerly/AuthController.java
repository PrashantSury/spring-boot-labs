package ledgerly;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Finished; NOT one of your tasks. It only calls your TokenService. */
@RestController
public class AuthController {

  private final AuthenticationManager manager;
  private final TokenService tokens;

  public AuthController(AuthenticationManager manager, TokenService tokens) {
    this.manager = manager;
    this.tokens = tokens;
  }

  public record Login(String username, String password) {}

  public record Refresh(String refreshToken) {}

  @PostMapping("/auth/login")
  public ResponseEntity<?> login(@RequestBody Login body) {
    try {
      var auth = manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(body.username(), body.password()));
      return ResponseEntity.ok(tokens.issue(auth.getName()));
    } catch (AuthenticationException e) {
      return ResponseEntity.status(401).body(Map.of("error", "Bad credentials"));
    }
  }

  @PostMapping("/auth/refresh")
  public ResponseEntity<?> refresh(@RequestBody Refresh body) {
    try {
      return ResponseEntity.ok(tokens.refresh(body.refreshToken()));
    } catch (InvalidRefreshToken e) {
      return ResponseEntity.status(401).body(Map.of("error", "invalid refresh token: " + e.getMessage()));
    }
  }

  @PostMapping("/auth/logout")
  public ResponseEntity<Void> logout(@RequestBody Refresh body) {
    tokens.logout(body.refreshToken());
    return ResponseEntity.noContent().build();
  }
}
