package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

/** Checks for chapter 14. Task 1 calls the filter directly; tasks 2-4 go through the whole app. */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter14CheckTest {

  @Autowired MockMvc mvc;
  @Autowired FilterRegistrationBean<AccessLogFilter> accessLog;

  // ---------- Task 1: CorrelationIdFilter on its own ----------

  @Test
  @DisplayName("Task 1: an incoming X-Correlation-Id 'abc-123' is in the MDC during the chain and in the response header")
  void task1a_reuse() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/whoami");
    req.addHeader("X-Correlation-Id", "abc-123");
    MockHttpServletResponse res = new MockHttpServletResponse();
    AtomicReference<String> seen = new AtomicReference<>();
    new CorrelationIdFilter().doFilter(req, res, new MockFilterChain(new jakarta.servlet.http.HttpServlet() {}, (rq, rs, ch) -> seen.set(MDC.get("cid"))));
    assertThat(seen.get()).withFailMessage("[task 1: MDC] during the chain MDC 'cid' was %s, expected abc-123", seen.get())
        .isEqualTo("abc-123");
    assertThat(res.getHeader("X-Correlation-Id")).isEqualTo("abc-123");
  }

  @Test
  @DisplayName("Task 1: no header -> a fresh UUID in the MDC and the response header")
  void task1b_generate() throws Exception {
    MockHttpServletResponse res = new MockHttpServletResponse();
    AtomicReference<String> seen = new AtomicReference<>();
    new CorrelationIdFilter().doFilter(new MockHttpServletRequest("GET", "/whoami"), res,
        new MockFilterChain(new jakarta.servlet.http.HttpServlet() {}, (rq, rs, ch) -> seen.set(MDC.get("cid"))));
    String uuid = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    assertThat(String.valueOf(seen.get())).withFailMessage("[task 1: generate] MDC 'cid' was %s, expected a UUID", seen.get())
        .matches(uuid);
    assertThat(res.getHeader("X-Correlation-Id")).isEqualTo(seen.get());
  }

  @Test
  @DisplayName("Task 1: after the request the MDC 'cid' is gone (finally), even when the controller throws")
  void task1c_cleanup() {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/whoami");
    req.addHeader("X-Correlation-Id", "abc-123");
    try {
      new CorrelationIdFilter().doFilter(req, new MockHttpServletResponse(),
          new MockFilterChain(new jakarta.servlet.http.HttpServlet() {}, (rq, rs, ch) -> {
            throw new IllegalStateException("boom");
          }));
    } catch (Exception expected) {
      // the exception may pass through; the MDC must still be clean
    }
    assertThat(MDC.get("cid")).withFailMessage("[task 1: cleanup] MDC 'cid' is still '%s' after the request."
        + " Remove it in a finally block", MDC.get("cid")).isNull();
  }

  @Test
  @DisplayName("Task 1: header 'bad id!' -> 400 problem+json 'Invalid X-Correlation-Id', the chain is NOT called")
  void task1d_invalid() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/whoami");
    req.addHeader("X-Correlation-Id", "bad id!");
    MockHttpServletResponse res = new MockHttpServletResponse();
    AtomicReference<Boolean> called = new AtomicReference<>(false);
    new CorrelationIdFilter().doFilter(req, res,
        new MockFilterChain(new jakarta.servlet.http.HttpServlet() {}, (rq, rs, ch) -> called.set(true)));
    assertThat(res.getStatus()).withFailMessage("[task 1: invalid] expected 400 but got %d", res.getStatus()).isEqualTo(400);
    assertThat(called.get()).withFailMessage("[task 1: invalid] the chain ran; return before chain.doFilter").isFalse();
    assertThat(res.getContentType()).contains("application/problem+json");
    assertThat(res.getContentAsString()).contains("Invalid X-Correlation-Id");
  }

  // ---------- Task 2: ApiKeyInterceptor ----------

  @Test
  @DisplayName("Task 2: GET /ops/cache-stats without a key is 401 'missing or wrong API key'")
  void task2a_noKey() throws Exception {
    MvcResult r = call(get("/ops/cache-stats"));
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 2: no key] expected 401 but got %d",
        r.getResponse().getStatus()).isEqualTo(401);
    assertThat(r.getResponse().getContentAsString()).isEqualTo("missing or wrong API key");
  }

  @Test
  @DisplayName("Task 2: a wrong key is 401; the right key (ops-key-2026) is 200 'hits=42 misses=3'")
  void task2b_keys() throws Exception {
    assertThat(call(get("/ops/cache-stats").header("X-Api-Key", "guess")).getResponse().getStatus()).isEqualTo(401);
    MvcResult ok = call(get("/ops/cache-stats").header("X-Api-Key", "ops-key-2026"));
    assertThat(ok.getResponse().getStatus()).isEqualTo(200);
    assertThat(ok.getResponse().getContentAsString()).isEqualTo("hits=42 misses=3");
  }

  @Test
  @DisplayName("Task 2: the key is only for /ops/**: /accounts/ACC-1/balance without a key is 200")
  void task2c_scope() {
    assertThat(call(get("/accounts/ACC-1/balance")).getResponse().getStatus())
        .withFailMessage("[task 2: scope] the key check must only cover /ops/**").isEqualTo(200);
  }

  // ---------- Task 3: HandlerNameInterceptor ----------

  @Test
  @DisplayName("Task 3: /whoami answers with header X-Handler: LedgerController#whoami")
  void task3a_name() {
    String h = call(get("/whoami")).getResponse().getHeader("X-Handler");
    assertThat(h).withFailMessage("[task 3: X-Handler] expected LedgerController#whoami but got %s", h)
        .isEqualTo("LedgerController#whoami");
  }

  @Test
  @DisplayName("Task 3: /accounts/ACC-1/balance -> LedgerController#balance; unmapped /nope -> 404, no X-Handler")
  void task3b_otherAndUnmapped() {
    assertThat(call(get("/accounts/ACC-1/balance")).getResponse().getHeader("X-Handler")).isEqualTo("LedgerController#balance");
    MvcResult r = call(get("/nope"));
    assertThat(r.getResponse().getStatus()).isEqualTo(404);
    assertThat(r.getResponse().getHeader("X-Handler")).withFailMessage("[task 3: unmapped] /nope has no controller"
        + " method, so no X-Handler header (check instanceof HandlerMethod)").isNull();
  }

  // ---------- Task 4: filter registration and order ----------

  @Test
  @DisplayName("Task 4: through the whole app, /whoami with X-Correlation-Id r-42 answers 'cid=r-42'")
  void task4a_registered() throws Exception {
    MvcResult r = call(get("/whoami").header("X-Correlation-Id", "r-42"));
    assertThat(r.getResponse().getContentAsString()).withFailMessage("[task 4: registered] got '%s'."
        + " Is CorrelationIdFilter registered in WebConfig?", r.getResponse().getContentAsString()).isEqualTo("cid=r-42");
    assertThat(r.getResponse().getHeader("X-Correlation-Id")).isEqualTo("r-42");
  }

  @Test
  @DisplayName("Task 4: the access log line carries the cid: 'cid=r-77 GET /accounts/ACC-1/balance 200' (order!)")
  void task4b_order() {
    call(get("/accounts/ACC-1/balance").header("X-Correlation-Id", "r-77"));
    List<String> lines = accessLog.getFilter().lines();
    assertThat(lines).withFailMessage("[task 4: order] the access log has %s. If it says cid=null, the correlation"
        + " filter runs INSIDE the access log; give it a lower order", lines)
        .contains("cid=r-77 GET /accounts/ACC-1/balance 200");
  }

  // ---------- Task 5 (Interview) ----------

  @Test
  @DisplayName("Task 5: interview-answer.md has your answer, not the TODO line")
  void task5a_answered() {
    assertThat(answer()).withFailMessage("[task 5: interview answer] interview-answer.md still has the TODO line")
        .doesNotContain("TODO: write your answer here");
  }

  @Test
  @DisplayName("Task 5: the answer is at least 40 words")
  void task5b_longEnough() {
    int words = answerBody().isBlank() ? 0 : answerBody().split("\\s+").length;
    assertThat(words).withFailMessage("[task 5: length] your answer has %d words. Write at least 40", words)
        .isGreaterThanOrEqualTo(40);
  }

  @Test
  @DisplayName("Task 5: the answer covers the key ideas (filter, interceptor, handlermethod, mdc, order)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"filter", "interceptor", "handlermethod", "mdc", "order"})
        .filter(w -> java.util.Arrays.stream(w.split("\\|")).noneMatch(body::contains)).toList();
    assertThat(missing).withFailMessage("[task 5: key ideas] your answer should also mention: %s", missing).isEmpty();
  }

  private static String answer() {
    java.nio.file.Path file = java.nio.file.Path.of(System.getProperty("user.dir")).resolve("interview-answer.md");
    try {
      return java.nio.file.Files.readString(file);
    } catch (java.io.IOException e) {
      return org.junit.jupiter.api.Assertions.fail("[task 5] cannot read " + file.toAbsolutePath());
    }
  }

  private static String answerBody() {
    String text = answer();
    String marker = "Delete the line that says TODO.";
    int i = text.indexOf(marker);
    String body = i >= 0 ? text.substring(i + marker.length()) : text;
    return body.replace("TODO: write your answer here", "").trim();
  }

  // ---------- helpers ----------

  private MvcResult call(RequestBuilder rb) {
    try {
      return mvc.perform(rb).andReturn();
    } catch (Exception e) {
      return fail("[request] the request threw " + e);
    }
  }
}
