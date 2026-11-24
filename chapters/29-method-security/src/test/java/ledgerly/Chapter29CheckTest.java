package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Checks for chapter 29. Account 1 = asha, accounts 2 and 3 = meera (3 is empty). */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter29CheckTest {

  @Autowired MockMvc mvc;
  @Autowired AccountService service;
  @Autowired TransferLimits limits;

  @BeforeEach
  void reset() {
    service.reset();
  }

  private int status(MockHttpServletRequestBuilder rb) {
    try {
      return mvc.perform(rb).andReturn().getResponse().getStatus();
    } catch (Exception e) {
      return fail("[request] " + e);
    }
  }

  private static BigDecimal amt(String s) {
    return new BigDecimal(s);
  }

  // ---------- Task 1 ----------

  @Test
  @WithMockUser(username = "asha", roles = "USER")
  @DisplayName("Task 1: asha calling service.close(2) gets AccessDeniedException")
  void task1a_denied() {
    assertThatThrownBy(() -> service.close(2)).withFailMessage("[task 1: close] a USER closed an account; enable method"
        + " security and add @PreAuthorize(\"hasRole('ADMIN')\")").isInstanceOf(AccessDeniedException.class);
  }

  @Test
  @WithMockUser(username = "ops", roles = "ADMIN")
  @DisplayName("Task 1: ops (ADMIN) DELETE /accounts/2 is 200; asha over HTTP is 403")
  void task1b_admin() {
    assertThat(status(delete("/accounts/2"))).isEqualTo(200);
  }

  @Test
  @WithMockUser(username = "asha", roles = "USER")
  @DisplayName("Task 1: asha DELETE /accounts/2 over HTTP is 403")
  void task1c_http403() {
    assertThat(status(delete("/accounts/2"))).withFailMessage("[task 1: close] expected 403").isEqualTo(403);
  }

  // ---------- Task 2 ----------

  @Test
  @WithMockUser(username = "meera", roles = "USER")
  @DisplayName("Task 2: meera lists her own accounts (2), but not asha's (403)")
  void task2a_owner() {
    assertThat(service.accountsOf("meera")).hasSize(2);
    assertThat(status(get("/customers/asha/accounts"))).withFailMessage("[task 2: owner] meera read asha's accounts")
        .isEqualTo(403);
  }

  @Test
  @WithMockUser(username = "ravi", roles = "TELLER")
  @DisplayName("Task 2: ravi (TELLER) may list anyone's accounts")
  void task2b_teller() {
    assertThat(service.accountsOf("asha")).hasSize(1);
  }

  // ---------- Task 3 ----------

  @Test
  @WithMockUser(username = "meera", roles = "USER")
  @DisplayName("Task 3: meera GET /accounts/1 (asha's) is 403; /accounts/2 is 200")
  void task3a_post() {
    assertThat(status(get("/accounts/2"))).isEqualTo(200);
    assertThat(status(get("/accounts/1"))).withFailMessage("[task 3: post] meera read asha's account").isEqualTo(403);
  }

  @Test
  @WithMockUser(username = "ravi", roles = "TELLER")
  @DisplayName("Task 3: ravi (TELLER) reads account 1")
  void task3b_teller() {
    assertThat(service.find(1).owner()).isEqualTo("asha");
  }

  // ---------- Task 4 ----------

  @Test
  @WithMockUser(username = "asha", roles = "USER")
  @DisplayName("Task 4: TransferLimits: USER up to 50,000, never zero or negative")
  void task4a_limits() {
    var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    try {
      assertThat(limits.allowed(auth, amt("50000"))).isTrue();
      assertThat(limits.allowed(auth, amt("50000.01"))).isFalse();
      assertThat(limits.allowed(auth, amt("0"))).isFalse();
      assertThat(limits.allowed(auth, amt("-5"))).isFalse();
    } catch (UnsupportedOperationException e) {
      fail("[task 4: limits] " + e.getMessage());
    }
  }

  @Test
  @WithMockUser(username = "asha", roles = "USER")
  @DisplayName("Task 4: asha POST /transfers 60000 is 403; 20000 is 200")
  void task4b_userHttp() {
    assertThat(status(post("/transfers?from=1&to=2&amount=20000"))).isEqualTo(200);
    assertThat(status(post("/transfers?from=1&to=2&amount=60000"))).withFailMessage("[task 4: limits] 60000 > 50000")
        .isEqualTo(403);
  }

  @Test
  @WithMockUser(username = "ravi", roles = "TELLER")
  @DisplayName("Task 4: ravi (TELLER) may move 4,00,000 but not 6,00,000")
  void task4c_teller() {
    assertThat(service.transfer(1, 2, amt("400000"))).startsWith("moved 400000");
    assertThatThrownBy(() -> service.transfer(1, 2, amt("600000"))).isInstanceOf(AccessDeniedException.class);
  }

  // ---------- Example (green on the stub, stays green) ----------

  @Test
  @WithMockUser(username = "asha", roles = "USER")
  @DisplayName("Example: self-invocation skips the proxy: asha's closeEmptyAccounts() closes account 3 anyway")
  void task0_selfInvocation() {
    assertThat(service.closeEmptyAccounts()).isEqualTo(1);
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
  @DisplayName("Task 5: the answer covers the key ideas (proxy, preauthorize, postauthorize, spel, self)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"proxy", "preauthorize", "postauthorize", "spel|#", "self"})
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
