package ledgerly;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Answers "I know you, but no": a logged-in user without permission. */
@Component
public class ProblemDeniedHandler implements AccessDeniedHandler {


  // Task 2: 403 problem: title "Forbidden", detail "You do not have permission for this action".
  //   Log a WARN line with the user and the path (request.getRemoteUser(), getRequestURI()).
  @Override
  public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
    res.sendError(403);
  }
}
