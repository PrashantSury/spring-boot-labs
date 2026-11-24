package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Checks for chapter 13. Each test sends one real request through the controller and your ApiErrors. */
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter13CheckTest {

  private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new AccountController(new Accounts()))
      .setControllerAdvice(new ApiErrors()).build();

  private static String transfer(String from, String to, String amount) {
    return "{\"from\":\"" + from + "\",\"to\":\"" + to + "\",\"amount\":" + amount + "}";
  }

  // ---------- Task 1: 404 for an unknown account ----------

  @Test
  @DisplayName("Task 1: a known account still answers 200 with its balance")
  void task1a_known() {
    MvcResult r = call(get("/accounts/ACC-1/balance"));
    assertThat(r.getResponse().getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName("Task 1: GET /accounts/ACC-9/balance is 404 with content type application/problem+json")
  void task1b_status() {
    MvcResult r = call(get("/accounts/ACC-9/balance"));
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 1: status] expected 404 but got %d",
        r.getResponse().getStatus()).isEqualTo(404);
    assertThat(r.getResponse().getContentType()).withFailMessage("[task 1: content type] expected"
        + " application/problem+json (return a ProblemDetail) but got %s", r.getResponse().getContentType())
        .contains("application/problem+json");
  }

  @Test
  @DisplayName("Task 1: the 404 body has type, title 'Account not found' and detail 'No account ACC-9'")
  void task1c_body() {
    String body = body(call(get("/accounts/ACC-9/balance")));
    assertThat(field(body, "$.title")).withFailMessage("[task 1: title] got %s", field(body, "$.title"))
        .isEqualTo("Account not found");
    assertThat(field(body, "$.detail")).isEqualTo("No account ACC-9");
    assertThat(field(body, "$.type")).isEqualTo("https://ledgerly.dev/errors/account-not-found");
    assertThat(field(body, "$.status")).isEqualTo(404);
  }

  // ---------- Task 2: 422 for insufficient funds ----------

  @Test
  @DisplayName("Task 2: a transfer within the balance is still 201")
  void task2a_ok() {
    assertThat(call(post("/transfers").contentType("application/json").content(transfer("ACC-1", "ACC-2", "5000")))
        .getResponse().getStatus()).isEqualTo(201);
  }

  @Test
  @DisplayName("Task 2: ₹60,000 from ACC-1 (balance ₹52,000) is 422, title 'Insufficient funds'")
  void task2b_status() {
    MvcResult r = call(post("/transfers").contentType("application/json").content(transfer("ACC-1", "ACC-2", "60000")));
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 2: status] expected 422 but got %d",
        r.getResponse().getStatus()).isEqualTo(422);
    assertThat(field(body(r), "$.title")).isEqualTo("Insufficient funds");
  }

  @Test
  @DisplayName("Task 2: the 422 body has the extra property shortBy = 8000")
  void task2c_shortBy() {
    String body = body(call(post("/transfers").contentType("application/json").content(transfer("ACC-1", "ACC-2", "60000"))));
    assertThat(field(body, "$.shortBy")).withFailMessage("[task 2: shortBy] expected 8000 at the top level of"
        + " the body (pd.setProperty) but the body was %s", body).isEqualTo(8000);
  }

  // ---------- Task 3: validation errors as ProblemDetail ----------

  @Test
  @DisplayName("Task 3: ApiErrors extends ResponseEntityExceptionHandler")
  void task3a_extends() {
    assertThat(ResponseEntityExceptionHandler.class.isAssignableFrom(ApiErrors.class))
        .withFailMessage("[task 3: base class] ApiErrors should extend ResponseEntityExceptionHandler").isTrue();
  }

  @Test
  @DisplayName("Task 3: amount 0 and blank 'to' are 400, title 'Invalid request', errors map names both fields")
  void task3b_errors() {
    MvcResult r = call(post("/transfers").contentType("application/json").content(transfer("ACC-1", "", "0")));
    assertThat(r.getResponse().getStatus()).isEqualTo(400);
    String body = body(r);
    assertThat(field(body, "$.title")).withFailMessage("[task 3: title] got %s in %s", field(body, "$.title"), body)
        .isEqualTo("Invalid request");
    Object errors = field(body, "$.errors");
    assertThat(errors).withFailMessage("[task 3: errors] expected a map 'errors' in %s", body).isInstanceOf(Map.class);
    java.util.Set<String> keys = new java.util.HashSet<>();
    ((Map<?, ?>) errors).keySet().forEach(k -> keys.add(String.valueOf(k)));
    assertThat(keys).withFailMessage("[task 3: errors] expected keys amount and to in %s", errors).contains("amount", "to");
    assertThat(((Map<?, ?>) errors).get("amount")).isEqualTo("must be greater than 0");
  }

  @Test
  @DisplayName("Task 3: broken JSON is a 400 problem+json (from the base class), not a 500")
  void task3c_badJson() {
    MvcResult r = call(post("/transfers").contentType("application/json").content("{\"amount\": 5"));
    assertThat(r.getResponse().getStatus()).isEqualTo(400);
    assertThat(r.getResponse().getContentType()).withFailMessage("[task 3: bad JSON] expected problem+json but got %s",
        r.getResponse().getContentType()).contains("application/problem+json");
  }

  // ---------- Task 4: the safety net ----------

  @Test
  @DisplayName("Task 4: the statement bug answers 500, title 'Internal error'")
  void task4a_500() {
    MvcResult r = call(get("/accounts/ACC-1/statement"));
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 4: status] expected 500 but got %d",
        r.getResponse().getStatus()).isEqualTo(500);
    assertThat(field(body(r), "$.title")).isEqualTo("Internal error");
    assertThat(field(body(r), "$.detail")).isEqualTo("Something went wrong. Quote this to support.");
  }

  @Test
  @DisplayName("Task 4: the 500 body leaks nothing (no NullPointerException, no 'service', no 'at ledgerly.')")
  void task4b_noLeak() {
    String body = body(call(get("/accounts/ACC-1/statement")));
    assertThat(body).withFailMessage("[task 4: leak] the body tells the client about the bug: %s", body)
        .doesNotContain("NullPointer").doesNotContain("\"service\"").doesNotContain("at ledgerly.");
  }

  @Test
  @DisplayName("Task 4: with the safety net in place, DELETE /transfers is still 405 and text/plain is still 415")
  void task4c_springStatuses() {
    assertThat(call(delete("/transfers")).getResponse().getStatus()).withFailMessage(
        "[task 4: 405] the catch-all swallowed Spring's 405. Extend ResponseEntityExceptionHandler").isEqualTo(405);
    assertThat(call(post("/transfers").contentType("text/plain").content("hi")).getResponse().getStatus())
        .withFailMessage("[task 4: 415] the catch-all swallowed Spring's 415").isEqualTo(415);
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
  @DisplayName("Task 5: the answer covers the key ideas (@restcontrolleradvice, problemdetail, 422, responseentityexceptionhandler, 405)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"@restcontrolleradvice", "problemdetail", "422", "responseentityexceptionhandler", "405|415"})
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

  // ---------- helpers ----------

  private MvcResult call(RequestBuilder rb) {
    try {
      return mvc.perform(rb).andReturn();
    } catch (Exception e) {
      return fail("[no handler] the exception escaped to the server: " + e.getCause()
          + ". Add an @ExceptionHandler for it in ApiErrors");
    }
  }

  private static String body(MvcResult r) {
    try {
      return r.getResponse().getContentAsString();
    } catch (Exception e) {
      return fail(e.toString());
    }
  }

  private static Object field(String json, String path) {
    try {
      return JsonPath.read(json, path);
    } catch (Exception e) {
      return null;
    }
  }
}
