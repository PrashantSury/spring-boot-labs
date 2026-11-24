package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

/** Checks for chapter 24. Every request goes through the real security filter chain. Users: asha (USER), ops (ADMIN). */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter24CheckTest {

  @Autowired MockMvc mvc;

  private MvcResult call(RequestBuilder rb) {
    try {
      return mvc.perform(rb).andReturn();
    } catch (Exception e) {
      return fail("[request] " + e);
    }
  }

  private int status(RequestBuilder rb) {
    return call(rb).getResponse().getStatus();
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: GET /public/rates needs no login (200)")
  void task1a_public() {
    assertThat(status(get("/public/rates"))).withFailMessage("[task 1: public] /public/** should be permitAll").isEqualTo(200);
  }

  @Test
  @DisplayName("Task 1: GET /accounts/me without login is 401; with asha/asha-pass it is 200 'accounts of asha'")
  void task1b_authenticated() throws Exception {
    assertThat(status(get("/accounts/me"))).isEqualTo(401);
    MvcResult r = call(get("/accounts/me").with(httpBasic("asha", "asha-pass")));
    assertThat(r.getResponse().getStatus()).isEqualTo(200);
    assertThat(r.getResponse().getContentAsString()).isEqualTo("accounts of asha");
  }

  @Test
  @DisplayName("Task 1: a wrong password is 401")
  void task1c_wrongPassword() {
    assertThat(status(get("/accounts/me").with(httpBasic("asha", "guess")))).isEqualTo(401);
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: asha (USER) on /admin/audit is 403")
  void task2a_forbidden() {
    assertThat(status(get("/admin/audit").with(httpBasic("asha", "asha-pass")))).withFailMessage(
        "[task 2: role] a USER reached /admin/**. Add hasRole(\"ADMIN\") BEFORE anyRequest()").isEqualTo(403);
  }

  @Test
  @DisplayName("Task 2: ops (ADMIN) on /admin/audit is 200")
  void task2b_admin() {
    assertThat(status(get("/admin/audit").with(httpBasic("ops", "ops-pass")))).isEqualTo(200);
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: POST /transfers with Basic auth and NO CSRF token is 201")
  void task3a_csrf() {
    int s = status(post("/transfers").with(httpBasic("asha", "asha-pass")).contentType("application/json").content("{}"));
    assertThat(s).withFailMessage("[task 3: csrf] got %d. A stateless API without cookies disables CSRF", s).isEqualTo(201);
  }

  @Test
  @DisplayName("Task 3: no HTTP session is created (STATELESS)")
  void task3b_stateless() {
    MvcResult r = call(get("/accounts/me").with(httpBasic("asha", "asha-pass")));
    assertThat(r.getRequest().getSession(false)).withFailMessage("[task 3: stateless] a session was created").isNull();
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: 401 body is {\"status\":401,\"error\":\"Login required\"}")
  void task4a_401() throws Exception {
    MvcResult r = call(get("/accounts/me"));
    assertThat(r.getResponse().getStatus()).isEqualTo(401);
    assertThat(r.getResponse().getContentAsString()).isEqualTo("{\"status\":401,\"error\":\"Login required\"}");
  }

  @Test
  @DisplayName("Task 4: wrong password also gets the JSON 401 body")
  void task4b_401basic() throws Exception {
    MvcResult r = call(get("/accounts/me").with(httpBasic("asha", "guess")));
    assertThat(r.getResponse().getContentAsString()).withFailMessage("[task 4: basic] give httpBasic the same entry point")
        .isEqualTo("{\"status\":401,\"error\":\"Login required\"}");
  }

  @Test
  @DisplayName("Task 4: 403 body is {\"status\":403,\"error\":\"Not allowed\"}")
  void task4c_403() throws Exception {
    MvcResult r = call(get("/admin/audit").with(httpBasic("asha", "asha-pass")));
    assertThat(r.getResponse().getStatus()).isEqualTo(403);
    assertThat(r.getResponse().getContentAsString()).isEqualTo("{\"status\":403,\"error\":\"Not allowed\"}");
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
  @DisplayName("Task 5: the answer covers the key ideas (authentication, authorization, 401, 403, first match, csrf)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"authentication", "authorization", "401", "403", "first match|order", "csrf"})
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
