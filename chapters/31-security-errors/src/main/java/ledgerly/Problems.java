package ledgerly;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/** Writes an RFC 9457 problem body straight to the response (filters have no @ControllerAdvice). Finished. */
public final class Problems {

  private static final JsonMapper JSON = JsonMapper.builder().build();

  private Problems() {}

  public static void write(HttpServletRequest req, HttpServletResponse res, int status, String title, String detail)
      throws IOException {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("type", "about:blank");
    body.put("title", title);
    body.put("status", status);
    body.put("detail", detail);
    body.put("instance", req.getRequestURI());
    res.setStatus(status);
    res.setContentType("application/problem+json");
    res.getWriter().write(JSON.writeValueAsString(body));
  }
}
