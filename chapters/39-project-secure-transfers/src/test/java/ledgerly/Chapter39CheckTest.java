package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Checks for chapter 39. Real logins through POST /auth/login.
 * Start: asha ACC-1 1000, ravi ACC-2 500 + ACC-3 0 (frozen), meera ACC-4 500; tara = teller, audit = auditor.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter39CheckTest {

  @Autowired MockMvc mvc;
  @Autowired DemoData demo;

  private int keySeq;

  @BeforeEach
  void reset() {
    demo.reset();
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

  private String token(String user) {
    var r = call(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"" + user + "\",\"password\":\"" + user + "-pass\"}"));
    assertThat(r.getStatus()).withFailMessage("[login] %s: %s %s", user, r.getStatus(), body(r)).isEqualTo(200);
    return JsonPath.read(body(r), "$.accessToken");
  }

  private MockHttpServletResponse as(String user, MockHttpServletRequestBuilder rb) {
    return call(rb.header("Authorization", "Bearer " + token(user)));
  }

  private MockHttpServletResponse transfer(String user, String from, String to, String amount) {
    return as(user, post("/transfers").header("Idempotency-Key", "k-" + (++keySeq))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"from\":\"" + from + "\",\"to\":\"" + to + "\",\"amount\":" + amount + "}"));
  }

  private BigDecimal balance(String number) {
    return new BigDecimal(JsonPath.read(body(as("tara", get("/accounts/" + number))), "$.balance").toString());
  }

  private static void status(String task, MockHttpServletResponse r, int expected) {
    assertThat(r.getStatus()).withFailMessage("[%s] status %s, body %s", task, r.getStatus(), body(r))
        .isEqualTo(expected);
  }

  // ---------- Examples (green on the stub, stay green) ----------

  @Test
  @DisplayName("Example: no token -> 401 problem; asha logs in and reads her own ACC-1")
  void task0_examples() {
    var anon = call(get("/accounts/ACC-1"));
    status("example: 401", anon, 401);
    assertThat(body(anon)).contains("\"title\":\"Unauthorized\"");
    var own = as("asha", get("/accounts/ACC-1"));
    status("example: own account", own, 200);
    assertThat(body(own)).contains("\"owner\":\"Asha\"");
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: /audit/transfers -> auditor 200, customer asha 403, teller tara 403")
  void task1a_auditArea() {
    status("task 1: auditor", as("audit", get("/audit/transfers")), 200);
    status("task 1: /audit/** is AUDITOR only (asha)", as("asha", get("/audit/transfers")), 403);
    status("task 1: /audit/** is AUDITOR only (tara)", as("tara", get("/audit/transfers")), 403);
  }

  @Test
  @DisplayName("Task 1: the auditor can't POST /transfers (403), and no money moves")
  void task1b_auditorNeverMovesMoney() {
    var r = transfer("audit", "ACC-1", "ACC-2", "10");
    status("task 1: POST /transfers is CUSTOMER or TELLER", r, 403);
    assertThat(balance("ACC-1")).isEqualByComparingTo("1000");
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: asha moves 100 from her own ACC-1 -> 201")
  void task2a_ownAccount() {
    status("task 2: own account", transfer("asha", "ACC-1", "ACC-2", "100"), 201);
    assertThat(balance("ACC-1")).isEqualByComparingTo("900");
  }

  @Test
  @DisplayName("Task 2: ravi tries to move 100 from asha's ACC-1 -> 403, ACC-1 untouched")
  void task2b_someoneElsesAccount() {
    var r = transfer("ravi", "ACC-1", "ACC-2", "100");
    status("task 2: ownership", r, 403);
    assertThat(balance("ACC-1")).withFailMessage("[task 2: ownership] ravi moved asha's money").isEqualByComparingTo("1000");
  }

  @Test
  @DisplayName("Task 2: teller tara moves 50 from ACC-4 for meera -> 201; ravi from an unknown ACC-9 -> 403 (not 404)")
  void task2c_tellerAndUnknown() {
    status("task 2: teller", transfer("tara", "ACC-4", "ACC-2", "50"), 201);
    status("task 2: unknown account is 'not yours'", transfer("ravi", "ACC-9", "ACC-2", "1"), 403);
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: GET /accounts/ACC-1 -> asha 200, ravi 403, tara 200, audit 200")
  void task3a_readAccount() {
    status("task 3: owner", as("asha", get("/accounts/ACC-1")), 200);
    status("task 3: not the owner", as("ravi", get("/accounts/ACC-1")), 403);
    status("task 3: teller", as("tara", get("/accounts/ACC-1")), 200);
    status("task 3: auditor", as("audit", get("/accounts/ACC-1")), 200);
  }

  @Test
  @DisplayName("Task 3: asha's transfer to ravi -> asha 200, ravi (receiver) 200, meera 403")
  void task3b_readTransfer() {
    var created = transfer("asha", "ACC-1", "ACC-2", "10");
    assertThat(created.getStatus()).withFailMessage("[task 2] asha's transfer failed: %s %s", created.getStatus(),
        body(created)).isEqualTo(201);
    Object id = JsonPath.read(body(created), "$.id");
    status("task 3: sender", as("asha", get("/transfers/" + id)), 200);
    status("task 3: receiver", as("ravi", get("/transfers/" + id)), 200);
    status("task 3: stranger", as("meera", get("/transfers/" + id)), 403);
  }

  @Test
  @DisplayName("Task 3: the auditor and the teller can read any transfer")
  void task3c_staffReadTransfer() {
    var created = transfer("asha", "ACC-1", "ACC-2", "10");
    Object id = JsonPath.read(body(created), "$.id");
    status("task 3: auditor", as("audit", get("/transfers/" + id)), 200);
    status("task 3: teller", as("tara", get("/transfers/" + id)), 200);
    status("task 3: stranger", as("meera", get("/transfers/" + id)), 403);
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
  @DisplayName("Task 5: the answer covers the key ideas (jwt, role, owner, preauthorize, 403)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"jwt|token", "role", "owner", "preauthorize|method security", "403"})
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
