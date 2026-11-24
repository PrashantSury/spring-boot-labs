package ledgerly;

import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/** POST /auth/login {"username":"asha","password":"asha-pass"} -> {"accessToken":"..."}. Finished. */
@RestController
public class AuthController {

  public record Login(String username, String password) {}

  private final AuthenticationManager auth;
  private final TokenService tokens;

  public AuthController(AuthenticationManager auth, TokenService tokens) {
    this.auth = auth;
    this.tokens = tokens;
  }

  @PostMapping("/auth/login")
  public Map<String, String> login(@RequestBody Login login) {
    try {
      var result = auth.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(login.username(), login.password()));
      return Map.of("accessToken", tokens.issueFor((UserDetails) result.getPrincipal()));
    } catch (AuthenticationException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "bad credentials");
    }
  }
}
