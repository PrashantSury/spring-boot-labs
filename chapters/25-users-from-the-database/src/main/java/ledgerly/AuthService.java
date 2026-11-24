package ledgerly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/** Checks a username + password (chapter 27 turns the result into a JWT). */
@Service
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);
  private final AuthenticationManager manager;

  public AuthService(AuthenticationManager manager) {
    this.manager = manager;
  }

  /** Returns the username if the password is right; rethrows the AuthenticationException otherwise. */
  public String check(String username, String password) {
    try {
      Authentication a = manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(username, password));
      return a.getName();
    } catch (AuthenticationException e) {
      // Task 3 (part 2): log the failure WITHOUT the password. Log: "login failed for {username}".
      log.warn("login failed for {} with password {}", username, password);
      throw e;
    }
  }
}
