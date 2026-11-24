package ledgerly;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Gives every request a correlation id, so all log lines of one request can be found together.
 *
 * <p>Task 1:
 * <ul>
 *   <li>Read the header X-Correlation-Id. If it is missing or blank, use UUID.randomUUID().
 *   <li>If it is present but does NOT match [A-Za-z0-9-]{1,64}: answer 400 yourself
 *       (status, content type application/problem+json, body
 *       {"title":"Invalid X-Correlation-Id","status":400}) and do NOT call the chain.
 *       An exception thrown here would never reach a @RestControllerAdvice.
 *   <li>Put the id in the MDC under "cid" and in the response header X-Correlation-Id.
 *   <li>Call the chain, and remove "cid" from the MDC in a finally block
 *       (threads are reused: a leftover id would be stamped on the NEXT customer's logs).
 * </ul>
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

  public static final String HEADER = "X-Correlation-Id";

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    chain.doFilter(req, res);
  }
}
