package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Checks for chapter 9. Every test gets a fresh controller (a fresh in-memory bank). */
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter09CheckTest {

  private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new AccountController()).build();

  // ---------- Task 1: POST /accounts -> 201 ----------

  @Test
  @DisplayName("Task 1: POST /accounts answers 201 with Location /accounts/ACC-4")
  void task1a_created() {
    MvcResult r = open("{\"holder\":\"Asha Rao\",\"openingBalance\":5000}");
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 1: status] expected 201 Created but got %d",
        r.getResponse().getStatus()).isEqualTo(201);
    assertThat(r.getResponse().getHeader("Location")).withFailMessage("[task 1: Location] expected a Location header"
        + " ending in /accounts/ACC-4 but got %s", r.getResponse().getHeader("Location")).endsWith("/accounts/ACC-4");
  }

  @Test
  @DisplayName("Task 1: the 201 body is the new account, and GET finds it afterwards")
  void task1b_bodyAndStored() {
    MvcResult r = open("{\"holder\":\"Asha Rao\",\"openingBalance\":5000}");
    assertThat(body(r)).isEqualTo("{\"number\":\"ACC-4\",\"holder\":\"Asha Rao\",\"balance\":5000}");
    assertThat(call(get("/accounts/ACC-4")).getResponse().getStatus())
        .withFailMessage("[task 1: stored] GET /accounts/ACC-4 after creating it should be 200").isEqualTo(200);
  }

  @Test
  @DisplayName("Task 1: two accounts get ACC-4 then ACC-5")
  void task1c_numbers() {
    open("{\"holder\":\"A\",\"openingBalance\":1}");
    MvcResult r = open("{\"holder\":\"B\",\"openingBalance\":2}");
    assertThat(r.getResponse().getHeader("Location")).endsWith("/accounts/ACC-5");
  }

  @Test
  @DisplayName("Task 1: unknown JSON fields (nickname) are ignored, not rejected")
  void task1d_unknownFields() {
    MvcResult r = open("{\"holder\":\"Asha Rao\",\"openingBalance\":5000,\"nickname\":\"A\",\"referral\":\"X1\"}");
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 1: unknown fields] an old app sends nickname and"
        + " referral; expected 201 but got %d", r.getResponse().getStatus()).isEqualTo(201);
    var ann = OpenAccountRequest.class.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnoreProperties.class);
    assertThat(ann != null && ann.ignoreUnknown()).withFailMessage("[task 1: explicit] mark OpenAccountRequest with"
        + " @JsonIgnoreProperties(ignoreUnknown = true) so it does not depend on the global setting").isTrue();
  }

  @Test
  @DisplayName("Task 1: a blank holder or a negative opening balance answers 400 and creates nothing")
  void task1e_badInput() {
    assertThat(open("{\"holder\":\"  \",\"openingBalance\":5000}").getResponse().getStatus()).isEqualTo(400);
    assertThat(open("{\"openingBalance\":5000}").getResponse().getStatus()).isEqualTo(400);
    assertThat(open("{\"holder\":\"X\",\"openingBalance\":-1}").getResponse().getStatus()).isEqualTo(400);
    assertThat(call(get("/accounts/ACC-4")).getResponse().getStatus())
        .withFailMessage("[task 1: bad input] a rejected request must not create ACC-4").isEqualTo(404);
  }

  @Test
  @DisplayName("Task 1: a body that is not JSON answers 400 before your method runs")
  void task1f_notJson() {
    assertThat(call(post("/accounts").contentType("application/json").content("{holder:")).getResponse().getStatus())
        .isEqualTo(400);
  }

  // ---------- Task 2: DELETE -> 204 ----------

  @Test
  @DisplayName("Task 2: DELETE /accounts/ACC-2 answers 204 with an empty body")
  void task2a_noContent() {
    MvcResult r = call(delete("/accounts/ACC-2"));
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 2: status] expected 204 No Content but got %d",
        r.getResponse().getStatus()).isEqualTo(204);
    assertThat(body(r)).isEmpty();
  }

  @Test
  @DisplayName("Task 2: after DELETE, GET /accounts/ACC-2 is 404 and a second DELETE is 404")
  void task2b_gone() {
    call(delete("/accounts/ACC-2"));
    assertThat(call(get("/accounts/ACC-2")).getResponse().getStatus()).isEqualTo(404);
    assertThat(call(delete("/accounts/ACC-2")).getResponse().getStatus())
        .withFailMessage("[task 2: unknown] deleting a missing account should be 404").isEqualTo(404);
  }

  // ---------- Task 3: params, defaults, headers ----------

  @Test
  @DisplayName("Task 3: no params answers all 3 transactions (limit defaults to 10)")
  void task3a_defaults() {
    MvcResult r = call(get("/accounts/ACC-1/transactions"));
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 3: status] expected 200 but got %d",
        r.getResponse().getStatus()).isEqualTo(200);
    assertThat(ids(r)).containsExactly("T1", "T2", "T3");
  }

  @Test
  @DisplayName("Task 3: ?limit=2 answers the first 2")
  void task3b_limit() {
    assertThat(ids(call(get("/accounts/ACC-1/transactions").param("limit", "2")))).containsExactly("T1", "T2");
  }

  @Test
  @DisplayName("Task 3: ?from=2026-11-03 keeps transactions ON or after that date")
  void task3c_from() {
    assertThat(ids(call(get("/accounts/ACC-1/transactions").param("from", "2026-11-03"))))
        .withFailMessage("[task 3: from] 2026-11-03 itself must be included").containsExactly("T2", "T3");
  }

  @Test
  @DisplayName("Task 3: ?from=03-11-2026 (not ISO) answers 400")
  void task3d_badDate() {
    assertThat(call(get("/accounts/ACC-1/transactions").param("from", "03-11-2026")).getResponse().getStatus())
        .isEqualTo(400);
  }

  @Test
  @DisplayName("Task 3: X-Request-Id is echoed back, and is optional")
  void task3e_header() {
    MvcResult r = call(get("/accounts/ACC-1/transactions").header("X-Request-Id", "req-42"));
    assertThat(r.getResponse().getHeader("X-Request-Id")).withFailMessage("[task 3: header] expected X-Request-Id: req-42"
        + " in the response but got %s", r.getResponse().getHeader("X-Request-Id")).isEqualTo("req-42");
    MvcResult none = call(get("/accounts/ACC-1/transactions"));
    assertThat(none.getResponse().getStatus()).isEqualTo(200);
    assertThat(none.getResponse().getHeader("X-Request-Id")).isNull();
  }

  // ---------- Task 4: JSON shape ----------

  @Test
  @DisplayName("Task 4: a transaction is {\"txn_id\":\"T1\",\"date\":\"2026-11-01\",\"amount\":50000}")
  void task4a_shape() {
    MvcResult r = call(get("/accounts/ACC-1/transactions").param("limit", "1"));
    assertThat(body(r)).withFailMessage("[task 4: json] expected [{\"txn_id\":\"T1\",\"date\":\"2026-11-01\",\"amount\":50000}]"
        + " but got %s", body(r)).isEqualTo("[{\"txn_id\":\"T1\",\"date\":\"2026-11-01\",\"amount\":50000}]");
  }

  @Test
  @DisplayName("Task 4: internalRef never leaves the bank")
  void task4b_hidden() {
    assertThat(body(call(get("/accounts/ACC-1/transactions")))).doesNotContain("internalRef").doesNotContain("CBS-");
  }

  // ---------- Task 5: ResponseEntity with headers ----------

  @Test
  @DisplayName("Task 5: GET /accounts/ACC-1/balance answers 200 with the balance as of 2026-11-24")
  void task5a_balance() {
    MvcResult r = call(get("/accounts/ACC-1/balance"));
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 5: status] expected 200 but got %d",
        r.getResponse().getStatus()).isEqualTo(200);
    assertThat(body(r)).isEqualTo("{\"number\":\"ACC-1\",\"balance\":52000,\"asOf\":\"2026-11-24\"}");
  }

  @Test
  @DisplayName("Task 5: the balance answer says Cache-Control: no-store")
  void task5b_noStore() {
    String cc = call(get("/accounts/ACC-1/balance")).getResponse().getHeader("Cache-Control");
    assertThat(cc).withFailMessage("[task 5: Cache-Control] expected no-store but got %s", cc).isEqualTo("no-store");
  }

  @Test
  @DisplayName("Task 5: an unknown account's balance is 404 with no body")
  void task5c_unknown() {
    MvcResult r = call(get("/accounts/ACC-9/balance"));
    assertThat(r.getResponse().getStatus()).isEqualTo(404);
    assertThat(body(r)).isEmpty();
  }

  // ---------- Task 6 (Interview) ----------

  @Test
  @DisplayName("Task 6: interview-answer.md has your answer, not the TODO line")
  void task6a_answered() {
    assertThat(answer()).withFailMessage("[task 6: interview answer] interview-answer.md still has the TODO line")
        .doesNotContain("TODO: write your answer here");
  }

  @Test
  @DisplayName("Task 6: the answer is at least 40 words")
  void task6b_longEnough() {
    int words = answerBody().isBlank() ? 0 : answerBody().split("\\s+").length;
    assertThat(words).withFailMessage("[task 6: length] your answer has %d words. Write at least 40", words)
        .isGreaterThanOrEqualTo(40);
  }

  @Test
  @DisplayName("Task 6: the answer covers the key ideas (@requestbody, 201, location, 204, content-type)")
  void task6c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"@requestbody", "201", "location", "204", "content-type|accept"})
        .filter(w -> java.util.Arrays.stream(w.split("\\|")).noneMatch(body::contains)).toList();
    assertThat(missing).withFailMessage("[task 6: key ideas] your answer should also mention: %s", missing).isEmpty();
  }

  private static String answer() {
    java.nio.file.Path file = java.nio.file.Path.of(System.getProperty("user.dir")).resolve("interview-answer.md");
    try {
      return java.nio.file.Files.readString(file);
    } catch (java.io.IOException e) {
      return org.junit.jupiter.api.Assertions.fail("[task 6] cannot read " + file.toAbsolutePath());
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

  private MvcResult open(String json) {
    return call(post("/accounts").contentType("application/json").content(json));
  }

  private MvcResult call(RequestBuilder rb) {
    try {
      return mvc.perform(rb).andReturn();
    } catch (Exception e) {
      return fail("[request] the request threw " + e);
    }
  }

  private static List<String> ids(MvcResult r) {
    String b = body(r);
    if (b.isEmpty()) {
      return fail("[task 3] the response body is empty (status " + r.getResponse().getStatus() + ")");
    }
    List<String> ids = JsonPath.parse(b).read("$[*].txn_id");
    if (ids.isEmpty()) {
      ids = JsonPath.parse(b).read("$[*].id");
    }
    return ids;
  }

  private static String body(MvcResult r) {
    try {
      return r.getResponse().getContentAsString();
    } catch (Exception e) {
      return fail("cannot read body: " + e);
    }
  }
}
