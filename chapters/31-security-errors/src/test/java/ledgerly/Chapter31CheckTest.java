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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Checks for chapter 31. */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter31CheckTest {

  @Autowired MockMvc mvc;

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

  private static void problem(String task, MockHttpServletResponse r, int status, String title) {
    assertThat(r.getStatus()).withFailMessage("[%s] status %s, body %s", task, r.getStatus(), body(r)).isEqualTo(status);
    assertThat(r.getContentType()).withFailMessage("[%s] content type %s, body '%s'", task, r.getContentType(), body(r))
        .startsWith("application/problem+json");
    assertThat(body(r)).contains("\"title\":\"" + title + "\"", "\"status\":" + status, "\"instance\":\"/");
  }

  // ---------- Examples (green on the stub, stay green) ----------

  @Test
  @DisplayName("Example: /public/rates needs no login; /accounts/9 as asha is a 404 problem from the advice")
  void task0_examples() {
    assertThat(call(get("/public/rates")).getStatus()).isEqualTo(200);
    var r = call(get("/accounts/9").with(httpBasic("asha", "asha-pass")));
    assertThat(r.getStatus()).isEqualTo(404);
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: no credentials: 401 problem 'Login required' + WWW-Authenticate")
  void task1a_noLogin() {
    var r = call(get("/me"));
    problem("task 1: entry point", r, 401, "Unauthorized");
    assertThat(body(r)).contains("Login required");
    assertThat(r.getHeader("WWW-Authenticate")).isEqualTo("Basic realm=\"ledgerly\"");
  }

  @Test
  @DisplayName("Task 1: the 401 body does not leak why (no 'Bad credentials', no user names)")
  void task1b_noLeak() {
    var r = call(get("/me").with(httpBasic("nobody", "x")));
    assertThat(body(r)).withFailMessage("[task 1: body] expected the problem body, got '%s'", body(r)).contains("Login required");
    assertThat(body(r)).withFailMessage("[task 1: leak] %s", body(r)).doesNotContain("Bad credentials", "nobody");
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2+3: asha on /admin/audit: 403 problem 'Forbidden'")
  void task2a_forbidden() {
    var r = call(get("/admin/audit").with(httpBasic("asha", "asha-pass")));
    problem("task 2: denied handler", r, 403, "Forbidden");
    assertThat(body(r)).contains("You do not have permission for this action");
  }

  @Test
  @DisplayName("Task 2: ops on /admin/audit: 200")
  void task2b_admin() {
    assertThat(call(get("/admin/audit").with(httpBasic("ops", "ops-pass"))).getStatus()).isEqualTo(200);
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: WRONG password: the same 401 problem (httpBasic uses your entry point)")
  void task3a_wrongPassword() {
    var r = call(get("/me").with(httpBasic("asha", "wrong")));
    problem("task 3: httpBasic entry point", r, 401, "Unauthorized");
  }

  @Test
  @DisplayName("Task 3: a missing account WITHOUT login is 401, not 404 (security runs before the controller)")
  void task3b_order() {
    problem("task 3: order", call(get("/accounts/9")), 401, "Unauthorized");
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: asha POST /accounts/1/freeze (@PreAuthorize ADMIN): 403 problem, NOT 500")
  void task4a_methodDenied() {
    var r = call(post("/accounts/1/freeze").with(httpBasic("asha", "asha-pass")));
    assertThat(r.getStatus()).withFailMessage("[task 4: advice] got %s: the catch-all swallowed the AccessDeniedException",
        r.getStatus()).isNotEqualTo(500);
    problem("task 4: advice", r, 403, "Forbidden");
  }

  @Test
  @DisplayName("Task 4: ops can freeze (200)")
  void task4b_opsFreeze() {
    var r = call(post("/accounts/1/freeze").with(httpBasic("ops", "ops-pass")));
    assertThat(body(r)).isEqualTo("account 1 frozen");
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
  @DisplayName("Task 5: the answer covers the key ideas (entry, accessdenied, 401, 403, filter)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"entry", "accessdenied|access denied", "401", "403", "filter"})
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
