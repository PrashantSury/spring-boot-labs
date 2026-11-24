package ledgerly;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Guards the ops endpoints with a shared key (real security comes in chapter 24).
 *
 * <p>Task 2: in preHandle, compare the header X-Api-Key with the key given to the constructor.
 * Wrong or missing: status 401, body "missing or wrong API key", return false (the controller
 * must NOT run). Right: return true. Register it for /ops/** only (WebConfig).
 */
public class ApiKeyInterceptor implements HandlerInterceptor {

  private final String key;

  public ApiKeyInterceptor(String key) {
    this.key = key;
  }

  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
    return true;
  }
}
