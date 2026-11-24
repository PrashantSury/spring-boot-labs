package ledgerly;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Tells the client which controller method answered (handy when debugging the mobile app).
 *
 * <p>Task 3: in preHandle, if the handler is a HandlerMethod, set the response header
 * X-Handler to "SimpleClassName#methodName", e.g. "LedgerController#whoami".
 * Anything else (a static resource, an unmapped path) gets no header. Always return true.
 * This is what an interceptor can see and a filter cannot: WHICH method will run.
 */
public class HandlerNameInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
    return true;
  }
}
