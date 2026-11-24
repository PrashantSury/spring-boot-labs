package ledgerly;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Writes one access-log line AFTER the request: "cid=... GET /path 200".
 * Finished; NOT one of your tasks. It reads the cid from the MDC, so CorrelationIdFilter
 * must be wrapped AROUND it (run first). That is task 4.
 */
public class AccessLogFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);
  private final List<String> lines = new CopyOnWriteArrayList<>();

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    try {
      chain.doFilter(req, res);
    } finally {
      String line = "cid=" + MDC.get("cid") + " " + req.getMethod() + " " + req.getRequestURI() + " " + res.getStatus();
      lines.add(line);
      log.info(line);
    }
  }

  public List<String> lines() {
    return lines;
  }
}
