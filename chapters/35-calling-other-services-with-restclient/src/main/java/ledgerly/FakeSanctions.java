package ledgerly;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A tiny stand-in for the external sanctions partner (a real HTTP server on localhost). Finished.
 *   GET  /screen?name=X  -> 200 {"name":X,"blocked":true|false}; empty name -> 400; "Slow Co" answers after 4 s
 *   POST /reports        -> 202 (records the Idempotency-Key header)
 *   GET  /health         -> 200 UP
 *   failNext(n)          -> the next n requests get 503
 */
public class FakeSanctions {

  private static final Set<String> BLOCKED = Set.of("Evil Corp", "Shady Shell Ltd");

  private final HttpServer server;
  private final AtomicInteger failNext = new AtomicInteger();
  private final List<String> log = new CopyOnWriteArrayList<>();

  public FakeSanctions(int port) {
    try {
      server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    server.setExecutor(Executors.newCachedThreadPool(r -> {
      Thread t = new Thread(r, "fake-sanctions");
      t.setDaemon(true); // don't keep the JVM alive after the tour
      return t;
    }));
    server.createContext("/", this::handle);
    server.start();
  }

  public String baseUrl() {
    return "http://localhost:" + server.getAddress().getPort();
  }

  public void failNext(int n) {
    failNext.set(n);
  }

  /** One line per request: "GET /screen?name=Acme Traders -> 200", "POST /reports key=TX-1 -> 202". */
  public List<String> log() {
    return List.copyOf(log);
  }

  public long count(String methodAndPath) {
    return log.stream().filter(l -> l.startsWith(methodAndPath)).count();
  }

  public void reset() {
    failNext.set(0);
    log.clear();
  }

  public void stop() {
    server.stop(0);
  }

  private void handle(HttpExchange ex) throws IOException {
    String method = ex.getRequestMethod();
    String path = ex.getRequestURI().getPath();
    String rawQuery = ex.getRequestURI().getRawQuery();
    String name = rawQuery != null && rawQuery.startsWith("name=")
        ? URLDecoder.decode(rawQuery.substring(5), StandardCharsets.UTF_8) : null;
    String what = method + " " + path + (name != null ? "?name=" + name : "");
    if ("POST".equals(method)) {
      ex.getRequestBody().readAllBytes();
      what += " key=" + ex.getRequestHeaders().getFirst("Idempotency-Key");
    }
    int status;
    String body = "";
    if (failNext.getAndUpdate(n -> Math.max(0, n - 1)) > 0) {
      status = 503;
    } else if (path.equals("/health")) {
      status = 200;
      body = "UP";
    } else if (path.equals("/reports") && method.equals("POST")) {
      status = 202;
    } else if (path.equals("/screen") && method.equals("GET")) {
      if (name == null || name.isBlank()) {
        status = 400;
      } else {
        if (name.equals("Slow Co")) sleep(4000);
        status = 200;
        body = "{\"name\":\"" + name + "\",\"blocked\":" + BLOCKED.contains(name) + "}";
      }
    } else {
      status = 404;
    }
    log.add(what + " -> " + status);
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    ex.getResponseHeaders().add("Content-Type", body.startsWith("{") ? "application/json" : "text/plain");
    try {
      ex.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
      if (bytes.length > 0) ex.getResponseBody().write(bytes);
    } catch (IOException clientGone) {
      // the caller timed out and hung up
    }
    ex.close();
  }

  private static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
