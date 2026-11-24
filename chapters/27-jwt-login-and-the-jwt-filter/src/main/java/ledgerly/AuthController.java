package ledgerly;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** POST /auth/login: username + password in, token out. */
@RestController
public class AuthController {

  private final AuthenticationManager manager;
  private final JwtCodec codec;
  private final long ttl;

  public AuthController(AuthenticationManager manager, JwtCodec codec, @Value("${ledgerly.jwt.ttl-seconds}") long ttl) {
    this.manager = manager;
    this.codec = codec;
    this.ttl = ttl;
  }

  public record Login(String username, String password) {}

  // Task 1: authenticate with the manager (UsernamePasswordAuthenticationToken.unauthenticated(u, p)).
  //   Success -> 200 {"token": codec.issue(name, ttl), "expiresIn": ttl}
  //   AuthenticationException -> 401 {"error": "Bad credentials"}
  @PostMapping("/auth/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody Login body) {
    return ResponseEntity.status(501).body(Map.of("error", "Task 1: not implemented"));
  }
}
