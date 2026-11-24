package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Checks for chapter 36. */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter36CheckTest {

  @Autowired MockMvc mvc;
  @Autowired PaymentsGateway gateway;
  @Autowired MeterRegistry registry;

  @AfterEach
  void gatewayBackUp() {
    gateway.setReachable(true);
  }

  private MockHttpServletResponse call(MockHttpServletRequestBuilder rb) {
    try {
      return mvc.perform(rb).andReturn().getResponse();
    } catch (Exception e) {
      return fail("[request] " + e);
    }
  }

  private static String body(MockHttpServletResponse r) {
    try {
      return r.getContentAsString();
    } catch (Exception e) {
      return "";
    }
  }

  private double count(String outcome) {
    Counter c = registry.find("ledgerly.transfers").tag("outcome", outcome).counter();
    return c == null ? 0 : c.count();
  }

  // ---------- Example (green on the stub, stays green) ----------

  @Test
  @DisplayName("Example: /actuator/health is UP and a transfer of 100 works")
  void task0_examples() {
    var h = call(get("/actuator/health"));
    assertThat(h.getStatus()).isEqualTo(200);
    assertThat(body(h)).contains("\"status\":\"UP\"");
    assertThat(call(post("/transfers").param("amount", "100")).getStatus()).isEqualTo(200);
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: /actuator/info shows app name Ledgerly and version 1.4.0")
  void task1a_info() {
    var r = call(get("/actuator/info"));
    assertThat(r.getStatus()).withFailMessage("[task 1: exposure] /actuator/info is %s", r.getStatus()).isEqualTo(200);
    assertThat(body(r)).withFailMessage("[task 1: info] body %s", body(r))
        .contains("\"name\":\"Ledgerly\"", "\"version\":\"1.4.0\"");
  }

  @Test
  @DisplayName("Task 1: /actuator/metrics is exposed; env, beans and heapdump are NOT (404)")
  void task1b_onlyWhatIsNeeded() {
    assertThat(call(get("/actuator/metrics")).getStatus()).withFailMessage("[task 1: exposure] /actuator/metrics"
        + " is not exposed").isEqualTo(200);
    for (String hidden : new String[] {"env", "beans", "heapdump", "configprops"}) {
      assertThat(call(get("/actuator/" + hidden)).getStatus()).withFailMessage("[task 1: safe exposure]"
          + " /actuator/%s must stay hidden", hidden).isEqualTo(404);
    }
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: /actuator/health/paymentsGateway is UP with latencyMs 42")
  void task2a_up() {
    var r = call(get("/actuator/health/paymentsGateway"));
    assertThat(r.getStatus()).withFailMessage("[task 2: indicator] /actuator/health/paymentsGateway is %s;"
        + " register a HealthIndicator bean", r.getStatus()).isEqualTo(200);
    assertThat(body(r)).contains("\"status\":\"UP\"", "\"latencyMs\":42");
  }

  @Test
  @DisplayName("Task 2: gateway down -> /actuator/health is 503 DOWN with reason 'connection refused'")
  void task2b_down() {
    gateway.setReachable(false);
    var r = call(get("/actuator/health"));
    assertThat(r.getStatus()).withFailMessage("[task 2: down] /actuator/health is %s, body %s", r.getStatus(), body(r))
        .isEqualTo(503);
    assertThat(body(r)).contains("\"paymentsGateway\"", "\"reason\":\"connection refused\"");
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: 2 transfers OK + 1 over the limit -> ledgerly.transfers ok +2, rejected +1")
  void task3a_counters() {
    double ok = count("ok");
    double rejected = count("rejected");
    call(post("/transfers").param("amount", "100"));
    call(post("/transfers").param("amount", "2500"));
    assertThat(call(post("/transfers").param("amount", "75000")).getStatus()).isEqualTo(422);
    assertThat(count("ok") - ok).withFailMessage("[task 3: counter] outcome=ok went up by %s", count("ok") - ok)
        .isEqualTo(2.0);
    assertThat(count("rejected") - rejected).withFailMessage("[task 3: counter] outcome=rejected went up by %s",
        count("rejected") - rejected).isEqualTo(1.0);
  }

  @Test
  @DisplayName("Task 3: /actuator/metrics/ledgerly.transfers?tag=outcome:rejected answers with a COUNT")
  void task3b_endpoint() {
    call(post("/transfers").param("amount", "99999"));
    var r = call(get("/actuator/metrics/ledgerly.transfers").param("tag", "outcome:rejected"));
    assertThat(r.getStatus()).withFailMessage("[task 3: metric endpoint] status %s", r.getStatus()).isEqualTo(200);
    assertThat(body(r)).contains("\"statistic\":\"COUNT\"");
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: gateway down -> /actuator/health/liveness stays 200 UP (don't restart for a partner outage)")
  void task4a_liveness() {
    gateway.setReachable(false);
    var r = call(get("/actuator/health/liveness"));
    assertThat(r.getStatus()).withFailMessage("[task 4: liveness] status %s, body %s", r.getStatus(), body(r))
        .isEqualTo(200);
    assertThat(body(r)).contains("\"status\":\"UP\"");
  }

  @Test
  @DisplayName("Task 4: gateway down -> /actuator/health/readiness is 503; back up -> 200")
  void task4b_readiness() {
    gateway.setReachable(false);
    var down = call(get("/actuator/health/readiness"));
    assertThat(down.getStatus()).withFailMessage("[task 4: readiness group] status %s, body %s", down.getStatus(),
        body(down)).isEqualTo(503);
    gateway.setReachable(true);
    assertThat(call(get("/actuator/health/readiness")).getStatus()).isEqualTo(200);
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
  @DisplayName("Task 5: the answer covers the key ideas (health, readiness, liveness, expos, counter)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"health", "readiness", "liveness", "expos", "counter|micrometer"})
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
}
