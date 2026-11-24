package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

/** Checks for chapter 38. Every check starts from: ACC-1 = 1000, ACC-2 = 500, ACC-3 = 0 (frozen). */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter38CheckTest {

  @Autowired MockMvc mvc;
  @Autowired DemoData demo;
  @Autowired TransferService service;

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

  private MockHttpServletResponse transfer(String key, String json) {
    return call(post("/transfers").header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON).content(json));
  }

  private MockHttpServletResponse transfer(String from, String to, String amount) {
    return transfer("k-" + (++keySeq), "{\"from\":\"" + from + "\",\"to\":\"" + to + "\",\"amount\":" + amount + "}");
  }

  private BigDecimal balance(String number) {
    var r = call(get("/accounts/" + number));
    return new BigDecimal(JsonPath.read(body(r), "$.balance").toString());
  }

  private static void status(String task, MockHttpServletResponse r, int expected) {
    assertThat(r.getStatus()).withFailMessage("[%s] status %s, body %s", task, r.getStatus(), body(r))
        .isEqualTo(expected);
  }

  // ---------- Example (green on the stub, stays green) ----------

  @Test
  @DisplayName("Example: GET /accounts/ACC-1 -> Asha, 1000.00")
  void task0_account() {
    var r = call(get("/accounts/ACC-1"));
    status("example", r, 200);
    assertThat(body(r)).isEqualTo("{\"number\":\"ACC-1\",\"owner\":\"Asha\",\"balance\":1000.00}");
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: amount -5 -> 400 with errors.amount")
  void task1a_negative() {
    var r = transfer("ACC-1", "ACC-2", "-5");
    status("task 1: @Positive + @Valid", r, 400);
    assertThat(body(r)).contains("\"amount\":\"must be greater than 0\"");
  }

  @Test
  @DisplayName("Task 1: missing 'to' and a bad 'from' -> 400 with both fields in errors")
  void task1b_fields() {
    var r = transfer("k-x", "{\"from\":\"1\",\"amount\":10}");
    status("task 1: @NotBlank/@Pattern", r, 400);
    assertThat(body(r)).contains("\"from\":\"must look like ACC-123\"", "\"to\":");
  }

  @Test
  @DisplayName("Task 1: from == to -> 400 'from and to must differ'")
  void task1c_sameAccount() {
    var r = transfer("ACC-1", "ACC-1", "10");
    status("task 1: @AssertTrue", r, 400);
    assertThat(body(r)).contains("\"differentAccounts\":\"from and to must differ\"");
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: 100 from ACC-1 to ACC-2 -> balances 900.00 and 600.00")
  void task2a_moves() {
    var r = transfer("ACC-1", "ACC-2", "100");
    assertThat(r.getStatus()).withFailMessage("[task 2: transfer] status %s, body %s", r.getStatus(), body(r))
        .isIn(200, 201);
    assertThat(balance("ACC-1")).isEqualByComparingTo("900");
    assertThat(balance("ACC-2")).isEqualByComparingTo("600");
  }

  @Test
  @DisplayName("Task 2: 5000 from ACC-1 -> 422 'insufficient funds in ACC-1', nothing moves")
  void task2b_insufficient() {
    var r = transfer("ACC-1", "ACC-2", "5000");
    status("task 2: insufficient funds", r, 422);
    assertThat(body(r)).contains("insufficient funds in ACC-1");
    assertThat(balance("ACC-1")).isEqualByComparingTo("1000");
  }

  @Test
  @DisplayName("Task 2: to ACC-9 -> 404 'no account ACC-9'")
  void task2c_unknown() {
    var r = transfer("ACC-1", "ACC-9", "10");
    status("task 2: unknown account", r, 404);
    assertThat(body(r)).contains("no account ACC-9");
  }

  @Test
  @DisplayName("Task 2: credit to frozen ACC-3 fails -> 422 and the DEBIT is rolled back (ACC-1 still 1000.00)")
  void task2d_rollback() {
    var r = transfer("ACC-1", "ACC-3", "10");
    status("task 2: frozen", r, 422);
    assertThat(body(r)).contains("account ACC-3 is frozen");
    assertThat(balance("ACC-1")).withFailMessage("[task 2: all or nothing] ACC-1 lost money").isEqualByComparingTo("1000");
  }

  @Test
  @DisplayName("Task 2: 10 parallel transfers of 100 from ACC-1 -> all succeed, ACC-1 = 0.00, ACC-2 = 1500.00")
  void task2e_parallel() throws Exception {
    List<String> errors = runParallel(10, i -> service.transfer("p-" + i,
        new TransferRequest("ACC-1", "ACC-2", new BigDecimal("100"))));
    assertThat(errors).withFailMessage("[task 2: lock] failures: %s", errors).isEmpty();
    assertThat(balance("ACC-1")).isEqualByComparingTo("0");
    assertThat(balance("ACC-2")).isEqualByComparingTo("1500");
  }

  @Test
  @DisplayName("Task 2: 5 transfers each way at the same time -> no deadlock, money is conserved")
  void task2f_bothWays() throws Exception {
    List<String> errors = runParallel(10, i -> i % 2 == 0
        ? service.transfer("b-" + i, new TransferRequest("ACC-1", "ACC-2", new BigDecimal("10")))
        : service.transfer("b-" + i, new TransferRequest("ACC-2", "ACC-1", new BigDecimal("20"))));
    assertThat(errors).withFailMessage("[task 2: lock order] failures: %s", errors).isEmpty();
    assertThat(balance("ACC-1")).isEqualByComparingTo("1050");
    assertThat(balance("ACC-2")).isEqualByComparingTo("450");
  }

  interface Job {
    Object run(int i);
  }

  private static List<String> runParallel(int n, Job job) throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(n);
    try {
      List<Future<Object>> fs = new ArrayList<>();
      for (int i = 0; i < n; i++) {
        int k = i;
        fs.add(pool.submit((Callable<Object>) () -> job.run(k)));
      }
      List<String> errors = new ArrayList<>();
      for (var f : fs) {
        try {
          f.get();
        } catch (Exception e) {
          Throwable c = e.getCause() != null ? e.getCause() : e;
          errors.add(c.getClass().getSimpleName() + ": " + c.getMessage());
        }
      }
      return errors;
    } finally {
      pool.shutdownNow();
    }
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: same Idempotency-Key + same body twice -> same transfer id, ACC-1 debited ONCE")
  void task3a_doubleSubmit() {
    String json = "{\"from\":\"ACC-1\",\"to\":\"ACC-2\",\"amount\":100}";
    var first = transfer("pay-42", json);
    var second = transfer("pay-42", json);
    assertThat(first.getStatus()).withFailMessage("[task 2] first call %s %s", first.getStatus(), body(first)).isIn(200, 201);
    assertThat(second.getStatus()).withFailMessage("[task 3: replay] second call %s %s", second.getStatus(),
        body(second)).isIn(200, 201);
    Object id1 = JsonPath.read(body(first), "$.id");
    Object id2 = JsonPath.read(body(second), "$.id");
    assertThat(id2).withFailMessage("[task 3: replay] ids %s and %s", id1, id2).isEqualTo(id1);
    assertThat(balance("ACC-1")).withFailMessage("[task 3: double debit] ACC-1 = %s", balance("ACC-1"))
        .isEqualByComparingTo("900");
  }

  @Test
  @DisplayName("Task 3: same key, different amount -> 409")
  void task3b_conflict() {
    transfer("pay-43", "{\"from\":\"ACC-1\",\"to\":\"ACC-2\",\"amount\":100}");
    var r = transfer("pay-43", "{\"from\":\"ACC-1\",\"to\":\"ACC-2\",\"amount\":999}");
    status("task 3: conflict", r, 409);
    assertThat(balance("ACC-1")).isEqualByComparingTo("900");
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: a new transfer -> 201 + Location /transfers/{id}")
  void task4a_created() {
    var r = transfer("ACC-1", "ACC-2", "25.50");
    status("task 4: 201", r, 201);
    Object id = JsonPath.read(body(r), "$.id");
    assertThat(r.getHeader("Location")).isEqualTo("/transfers/" + id);
    assertThat(body(r)).contains("\"from\":\"ACC-1\"", "\"to\":\"ACC-2\"", "\"amount\":25.50");
  }

  @Test
  @DisplayName("Task 4: a replay -> 200 (not 201) with the original body")
  void task4b_replay200() {
    String json = "{\"from\":\"ACC-1\",\"to\":\"ACC-2\",\"amount\":10}";
    var first = transfer("pay-44", json);
    var again = transfer("pay-44", json);
    status("task 4: replay status", again, 200);
    assertThat(body(again)).isEqualTo(body(first));
  }

  @Test
  @DisplayName("Task 4: GET /transfers/{id} -> 200; GET /transfers/999999 -> 404")
  void task4c_get() {
    var r = transfer("ACC-1", "ACC-2", "10");
    Object id = JsonPath.read(body(r), "$.id");
    var got = call(get("/transfers/" + id));
    status("task 4: get", got, 200);
    assertThat(body(got)).isEqualTo(body(r));
    status("task 4: get unknown", call(get("/transfers/999999")), 404);
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
  @DisplayName("Task 5: the answer covers the key ideas (transaction, lock, idempoten, valid, rollback)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"transaction", "lock", "idempoten", "valid", "rollback|roll back"})
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
