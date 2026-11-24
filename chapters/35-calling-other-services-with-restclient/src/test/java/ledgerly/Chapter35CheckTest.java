package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Checks for chapter 35. FakeSanctions is a real HTTP server on a random local port. */
@SpringBootTest(properties = "ledgerly.tour=false")
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter35CheckTest {

  static final FakeSanctions partner = new FakeSanctions(0);

  @DynamicPropertySource
  static void partnerUrl(DynamicPropertyRegistry r) {
    r.add("sanctions.base-url", partner::baseUrl);
  }

  @AfterAll
  static void stop() {
    partner.stop();
  }

  @Autowired SanctionsClient sanctions;
  @Autowired Environment env;

  @BeforeEach
  void reset() {
    partner.reset();
  }

  // ---------- Example (green on the stub, stays green) ----------

  @Test
  @DisplayName("Example: MockRestServiceServer fakes the partner without any network (ping -> UP)")
  void task0_mockServer() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server.expect(requestTo("http://partner.test/health")).andRespond(withSuccess("UP", MediaType.TEXT_PLAIN));
    assertThat(new SanctionsClient(builder, "http://partner.test").ping()).isEqualTo("UP");
    server.verify();
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: screen(\"Acme Traders\") -> ScreenResult[name=Acme Traders, blocked=false]")
  void task1a_clean() {
    assertThat(sanctions.screen("Acme Traders")).isEqualTo(new ScreenResult("Acme Traders", false));
    assertThat(partner.log()).containsExactly("GET /screen?name=Acme Traders -> 200");
  }

  @Test
  @DisplayName("Task 1: screen(\"Evil Corp\") -> blocked=true")
  void task1b_blocked() {
    assertThat(sanctions.screen("Evil Corp").blocked()).isTrue();
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: a 400 from the partner becomes PayeeRejected")
  void task2a_badRequest() {
    assertThatThrownBy(() -> sanctions.screen(" ")).withFailMessage("[task 2: onStatus 400] expected PayeeRejected")
        .isInstanceOf(PayeeRejected.class);
  }

  @Test
  @DisplayName("Task 2: a 503 that never goes away becomes SanctionsUnavailable 'partner answered 503'")
  void task2b_unavailable() {
    partner.failNext(10);
    assertThatThrownBy(() -> sanctions.screen("Acme Traders")).withFailMessage("[task 2: onStatus 5xx] expected"
        + " SanctionsUnavailable").isInstanceOf(SanctionsUnavailable.class).hasMessage("partner answered 503");
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: a partner that answers after 4 s -> SanctionsUnavailable 'partner timed out' in under 3.5 s")
  void task3a_timeout() {
    long start = System.nanoTime();
    Throwable thrown = null;
    try {
      sanctions.screen("Slow Co");
    } catch (Throwable t) {
      thrown = t;
    }
    long ms = (System.nanoTime() - start) / 1_000_000;
    assertThat(thrown).withFailMessage("[task 3: read timeout] no exception after %s ms; set"
        + " spring.http.clients.read-timeout", ms).isInstanceOf(SanctionsUnavailable.class)
        .hasMessage("partner timed out");
    assertThat(ms).withFailMessage("[task 3: no retry on timeout] took %s ms", ms).isLessThan(3500);
    assertThat(partner.count("GET /screen")).withFailMessage("[task 3: no retry on timeout]").isLessThanOrEqualTo(1);
  }

  @Test
  @DisplayName("Task 3: connect-timeout and read-timeout are both 2s in application.properties")
  void task3b_properties() {
    assertThat(env.getProperty("spring.http.clients.connect-timeout")).withFailMessage("[task 3: connect timeout]"
        + " spring.http.clients.connect-timeout is not set").isEqualTo("2s");
    assertThat(env.getProperty("spring.http.clients.read-timeout")).isEqualTo("2s");
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: two 503s then OK -> screen succeeds after 3 GETs")
  void task4a_retry() {
    partner.failNext(2);
    assertThat(sanctions.screen("Acme Traders").blocked()).isFalse();
    assertThat(partner.count("GET /screen")).withFailMessage("[task 4: retry] partner saw %s", partner.log())
        .isEqualTo(3);
  }

  @Test
  @DisplayName("Task 4: gives up after exactly 3 attempts")
  void task4b_giveUp() {
    partner.failNext(10);
    assertThatThrownBy(() -> sanctions.screen("Acme Traders")).isInstanceOf(SanctionsUnavailable.class);
    assertThat(partner.count("GET /screen")).withFailMessage("[task 4: max attempts] partner saw %s", partner.log())
        .isEqualTo(3);
  }

  @Test
  @DisplayName("Task 4: a 400 is never retried")
  void task4c_noRetryOn400() {
    assertThatThrownBy(() -> sanctions.screen(" ")).isInstanceOf(PayeeRejected.class);
    assertThat(partner.count("GET /screen")).isEqualTo(1);
  }

  @Test
  @DisplayName("Task 4: report sends Idempotency-Key: TX-7")
  void task4d_idempotencyKey() {
    sanctions.report("TX-7", "Acme Traders");
    assertThat(partner.log()).withFailMessage("[task 4: idempotency key] partner saw %s", partner.log())
        .containsExactly("POST /reports key=TX-7 -> 202");
  }

  @Test
  @DisplayName("Task 4: a 503 on report -> SanctionsUnavailable and exactly ONE POST (never retried)")
  void task4e_postNotRetried() {
    partner.failNext(1);
    assertThatThrownBy(() -> sanctions.report("TX-8", "Acme Traders")).withFailMessage("[task 4: report 5xx]"
        + " expected SanctionsUnavailable").isInstanceOf(SanctionsUnavailable.class);
    assertThat(partner.count("POST /reports")).isEqualTo(1);
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
  @DisplayName("Task 5: the answer covers the key ideas (timeout, onStatus, retry, idempoten, MockRestServiceServer)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"timeout", "onStatus|status", "retry", "idempoten", "MockRestServiceServer|mock"})
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
