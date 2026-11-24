package ledgerly;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/** Answers "who are you?" problems: no credentials, or wrong ones. */
@Component
public class ProblemEntryPoint implements AuthenticationEntryPoint {

  // Task 1: 401 as a problem body: title "Unauthorized", detail "Login required"
  //   (use Problems.write), plus the header  WWW-Authenticate: Basic realm="ledgerly"
  //   so clients know HOW to log in. Never put ex.getMessage() in the body (it can say "user not found").
  @Override
  public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
    res.sendError(401);
  }
}
