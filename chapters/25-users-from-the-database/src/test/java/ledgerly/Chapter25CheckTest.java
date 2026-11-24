package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

/** Checks for chapter 25. Users in H2: asha (USER), ops (ADMIN), ravi (USER, disabled). */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter25CheckTest {

  @Autowired MockMvc mvc;
  @Autowired UserDetailsService uds;
  @Autowired PasswordEncoder encoder;
  @Autowired AppUserRepository users;

  private MvcResult call(RequestBuilder rb) {
    try {
      return mvc.perform(rb).andReturn();
    } catch (Exception e) {
      return fail("[request] " + e);
    }
  }

  private static String creds(String u, String p) {
    return "{\"username\":\"" + u + "\",\"password\":\"" + p + "\"}";
  }

  // ---------- given: @WithMockUser (stays green) ----------

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("Given: with @WithMockUser(roles = ADMIN) /admin/users answers 200, no password needed")
  void task0_withMockUser() {
    assertThat(call(get("/admin/users")).getResponse().getStatus()).isEqualTo(200);
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: loadUserByUsername(\"asha\") has the BCrypt hash and ROLE_USER")
  void task1a_load() {
    UserDetails d;
    try {
      d = uds.loadUserByUsername("asha");
    } catch (UsernameNotFoundException e) {
      d = fail("[task 1] " + e.getMessage());
    }
    assertThat(encoder.matches("asha-pass", d.getPassword())).withFailMessage("[task 1: password] use the stored hash").isTrue();
    assertThat(d.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_USER");
    assertThat(d.isEnabled()).isTrue();
  }

  @Test
  @DisplayName("Task 1: ravi is loaded as DISABLED; an unknown name throws UsernameNotFoundException")
  void task1b_disabledAndUnknown() {
    Throwable t = catchThrowable(() -> uds.loadUserByUsername("ravi"));
    assertThat(t).withFailMessage("[task 1: ravi] %s", t).isNull();
    assertThat(uds.loadUserByUsername("ravi").isEnabled()).withFailMessage("[task 1: disabled] map enabled -> disabled(!enabled)")
        .isFalse();
    assertThat(catchThrowable(() -> uds.loadUserByUsername("nobody"))).isInstanceOf(UsernameNotFoundException.class);
  }

  @Test
  @DisplayName("Task 1: HTTP Basic asha/asha-pass on /me is 200 'hello asha'; ravi (disabled) is 401")
  void task1c_http() throws Exception {
    MvcResult r = call(get("/me").with(httpBasic("asha", "asha-pass")));
    assertThat(r.getResponse().getStatus()).isEqualTo(200);
    assertThat(r.getResponse().getContentAsString()).isEqualTo("hello asha");
    assertThat(call(get("/me").with(httpBasic("ravi", "ravi-pass"))).getResponse().getStatus()).isEqualTo(401);
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: register meera/s3cret-Pass stores a BCrypt hash ($2...), not the password")
  void task2a_hash() {
    call(post("/auth/register").contentType("application/json").content(creds("meera", "s3cret-Pass")));
    String stored = users.findByUsername("meera").map(AppUser::getPasswordHash).orElse("(not saved)");
    assertThat(stored).withFailMessage("[task 2: hash] the table holds '%s'. Store encoder.encode(rawPassword)", stored)
        .startsWith("$2").isNotEqualTo("s3cret-Pass");
    assertThat(encoder.matches("s3cret-Pass", stored)).isTrue();
    users.findByUsername("meera").ifPresent(users::delete);
  }

  @Test
  @DisplayName("Task 2: registering 'asha' again is 409")
  void task2b_duplicate() {
    int s = call(post("/auth/register").contentType("application/json").content(creds("asha", "x"))).getResponse().getStatus();
    assertThat(s).withFailMessage("[task 2: duplicate] got %d, expected 409", s).isEqualTo(409);
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: /auth/check asha/asha-pass is 200 {\"user\":\"asha\"}")
  void task3a_ok() throws Exception {
    MvcResult r = call(post("/auth/check").contentType("application/json").content(creds("asha", "asha-pass")));
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 3: manager] got %d: %s", r.getResponse().getStatus(),
        r.getResponse().getContentAsString()).isEqualTo(200);
    assertThat(r.getResponse().getContentAsString()).isEqualTo("{\"user\":\"asha\"}");
  }

  @Test
  @DisplayName("Task 3: wrong password and unknown user give the SAME 401 body ('Bad credentials')")
  void task3b_vague() throws Exception {
    MvcResult wrong = call(post("/auth/check").contentType("application/json").content(creds("asha", "guess")));
    MvcResult unknown = call(post("/auth/check").contentType("application/json").content(creds("nobody", "guess")));
    assertThat(wrong.getResponse().getStatus()).isEqualTo(401);
    assertThat(unknown.getResponse().getContentAsString()).isEqualTo(wrong.getResponse().getContentAsString())
        .isEqualTo("{\"error\":\"Bad credentials\"}");
  }

  @Test
  @DisplayName("Task 3: a failed login is logged as 'login failed for asha' and the password is NOT in the log")
  void task3c_log(CapturedOutput out) {
    call(post("/auth/check").contentType("application/json").content(creds("asha", "Wr0ngPa55!")));
    assertThat(out.getAll()).contains("login failed for asha");
    assertThat(out.getAll()).withFailMessage("[task 3: log] the password is in the log").doesNotContain("Wr0ngPa55!");
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
  @DisplayName("Task 4: the answer covers the key ideas (userdetailsservice, bcrypt, hash, passwordencoder, bad credentials)")
  void task4c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"userdetailsservice", "bcrypt", "hash", "passwordencoder", "bad credentials|vague"})
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
