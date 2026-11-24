package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

/** Checks for chapter 27. Users: asha/asha-pass (USER), ops/ops-pass (USER, ADMIN). */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter27CheckTest {

  @Autowired MockMvc mvc;
  @Autowired JwtCodec codec;
  @Value("${ledgerly.jwt.secret}") String secret;

  private MvcResult call(RequestBuilder rb) {
    try {
      return mvc.perform(rb).andReturn();
    } catch (Exception e) {
      return fail("[request] " + e);
    }
  }

  private static String body(MvcResult r) {
    try {
      return r.getResponse().getContentAsString();
    } catch (Exception e) {
      return fail(e.toString());
    }
  }

  private MvcResult login(String u, String p) {
    return call(post("/auth/login").contentType("application/json")
        .content("{\"username\":\"" + u + "\",\"password\":\"" + p + "\"}"));
  }

  private String tokenFor(String u, String p) {
    MvcResult r = login(u, p);
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 1: login] /auth/login answered %d: %s",
        r.getResponse().getStatus(), body(r)).isEqualTo(200);
    return JsonPath.read(body(r), "$.token");
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: /auth/login asha/asha-pass is 200 with a token whose sub is asha and expiresIn 900")
  void task1a_login() {
    MvcResult r = login("asha", "asha-pass");
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 1] got %d", r.getResponse().getStatus()).isEqualTo(200);
    String token = JsonPath.read(body(r), "$.token");
    assertThat(codec.verify(token)).containsEntry("sub", "asha");
    assertThat((Integer) JsonPath.read(body(r), "$.expiresIn")).isEqualTo(900);
  }

  @Test
  @DisplayName("Task 1: a wrong password is 401 {\"error\":\"Bad credentials\"}")
  void task1b_bad() {
    MvcResult r = login("asha", "guess");
    assertThat(r.getResponse().getStatus()).isEqualTo(401);
    assertThat(body(r)).isEqualTo("{\"error\":\"Bad credentials\"}");
  }

  // ---------- Task 2 + 3 ----------

  @Test
  @DisplayName("Task 2+3: GET /me with 'Bearer <asha's token>' is 200 'asha [ROLE_USER]'")
  void task2a_bearer() {
    String token = tokenFor("asha", "asha-pass");
    MvcResult r = call(get("/me").header("Authorization", "Bearer " + token));
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 2/3: bearer] got %d %s. Does the filter set the"
        + " SecurityContext, and is it in the chain (addFilterBefore)?", r.getResponse().getStatus(), body(r)).isEqualTo(200);
    assertThat(body(r)).isEqualTo("asha [ROLE_USER]");
  }

  @Test
  @DisplayName("Task 2+3: roles come from the user store: ops's token opens /admin/audit, asha's gets 403")
  void task2b_roles() {
    assertThat(call(get("/admin/audit").header("Authorization", "Bearer " + tokenFor("ops", "ops-pass")))
        .getResponse().getStatus()).isEqualTo(200);
    assertThat(call(get("/admin/audit").header("Authorization", "Bearer " + tokenFor("asha", "asha-pass")))
        .getResponse().getStatus()).isEqualTo(403);
  }

  @Test
  @DisplayName("Task 2: no Authorization header -> 401 {\"error\":\"Login required\"} (anonymous, rules decide)")
  void task2c_none() {
    MvcResult r = call(get("/me"));
    assertThat(r.getResponse().getStatus()).isEqualTo(401);
    assertThat(body(r)).isEqualTo("{\"error\":\"Login required\"}");
  }

  @Test
  @DisplayName("Task 2+3: an expired token -> 401 {\"error\":\"invalid token: expired\"}")
  void task2d_expired() {
    JwtCodec past = new JwtCodec(secret.getBytes(StandardCharsets.UTF_8), Clock.offset(Clock.systemUTC(), Duration.ofHours(-1)));
    MvcResult r = call(get("/me").header("Authorization", "Bearer " + past.issue("asha", 900)));
    assertThat(r.getResponse().getStatus()).isEqualTo(401);
    assertThat(body(r)).withFailMessage("[task 2: expired] got %s", body(r)).isEqualTo("{\"error\":\"invalid token: expired\"}");
  }

  @Test
  @DisplayName("Task 2+3: a token signed with another secret -> 401 {\"error\":\"invalid token: bad signature\"}")
  void task2e_forged() {
    JwtCodec other = new JwtCodec("someone-elses-secret-32-bytes!!!!".getBytes(StandardCharsets.UTF_8), Clock.systemUTC());
    MvcResult r = call(get("/me").header("Authorization", "Bearer " + other.issue("ops", 900)));
    assertThat(r.getResponse().getStatus()).isEqualTo(401);
    assertThat(body(r)).isEqualTo("{\"error\":\"invalid token: bad signature\"}");
  }

  @Test
  @DisplayName("Task 3: the response sets no session cookie (stateless: every request carries its token)")
  void task3a_noSession() {
    MvcResult r = call(get("/me").header("Authorization", "Bearer " + tokenFor("asha", "asha-pass")));
    assertThat(r.getRequest().getSession(false)).isNull();
    assertThat(r.getResponse().getHeader("Set-Cookie")).isNull();
  }

  // ---------- Task 4 (Interview) ----------

  @Test
  @DisplayName("Task 4: interview-answer.md has your answer, not the TODO line")
  void task4a_answered() {
    assertThat(answer()).withFailMessage("[task 4: interview answer] interview-answer.md still has the TODO line")
        .doesNotContain("TODO: write your answer here");
  }

  @Test
  @DisplayName("Task 4: the answer is at least 40 words")
  void task4b_longEnough() {
    int words = answerBody().isBlank() ? 0 : answerBody().split("\\s+").length;
    assertThat(words).withFailMessage("[task 4: length] your answer has %d words. Write at least 40", words)
        .isGreaterThanOrEqualTo(40);
  }

  @Test
  @DisplayName("Task 4: the answer covers the key ideas (bearer, filter, securitycontext, addfilterbefore, stateless)")
  void task4c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"bearer", "filter", "securitycontext", "addfilterbefore|before", "stateless"})
        .filter(w -> java.util.Arrays.stream(w.split("\\|")).noneMatch(body::contains)).toList();
    assertThat(missing).withFailMessage("[task 4: key ideas] your answer should also mention: %s", missing).isEmpty();
  }

  private static String answer() {
    java.nio.file.Path file = java.nio.file.Path.of(System.getProperty("user.dir")).resolve("interview-answer.md");
    try {
      return java.nio.file.Files.readString(file);
    } catch (java.io.IOException e) {
      return org.junit.jupiter.api.Assertions.fail("[task 4] cannot read " + file.toAbsolutePath());
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
