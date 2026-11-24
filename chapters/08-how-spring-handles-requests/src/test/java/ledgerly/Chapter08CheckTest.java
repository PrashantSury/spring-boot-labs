package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Checks for chapter 8. Each task gets a MockMvc with only its own
 * controller, so one unfinished task does not break the others.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter08CheckTest {

  private static final String JSON = "{\"from\":\"ACC-1\",\"to\":\"ACC-2\",\"amount\":500}";

  // ---------- Task 1: specific path beats a pattern ----------

  @Test
  @DisplayName("Task 1: GET /accounts/dormant answers [\"ACC-3\"]")
  void task1a_dormant() {
    MvcResult r = call(mvc(new AccountController()), get("/accounts/dormant"));
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 1: /accounts/dormant] expected 200 but got %d (%s)."
        + " It is landing in byNumber(\"dormant\")", r.getResponse().getStatus(), body(r)).isEqualTo(200);
    assertThat(body(r)).isEqualTo("[\"ACC-3\"]");
  }

  @Test
  @DisplayName("Task 1: GET /accounts/ACC-1 still finds Asha's account")
  void task1b_patternStillWorks() {
    MvcResult r = call(mvc(new AccountController()), get("/accounts/ACC-1"));
    assertThat(r.getResponse().getStatus()).isEqualTo(200);
    assertThat(body(r)).contains("\"holder\":\"Asha Rao\"");
  }

  @Test
  @DisplayName("Task 1: GET /accounts/ACC-9 is still 404 (pattern matched, no such account)")
  void task1c_unknown() {
    assertThat(status(mvc(new AccountController()), get("/accounts/ACC-9"))).isEqualTo(404);
  }

  // ---------- Task 2: produces ----------

  @Test
  @DisplayName("Task 2: Accept text/csv answers the CSV text")
  void task2a_csv() {
    MvcResult r = call(mvc(new StatementController()), get("/accounts/ACC-1/statement").accept("text/csv"));
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 2: csv] expected 200 but got %d",
        r.getResponse().getStatus()).isEqualTo(200);
    assertThat(r.getResponse().getContentType()).startsWith("text/csv");
    assertThat(body(r)).isEqualTo("date,amount\n2026-11-01,50000\n2026-11-03,-1200\n");
  }

  @Test
  @DisplayName("Task 2: Accept application/json answers the JSON list")
  void task2b_json() {
    MvcResult r = call(mvc(new StatementController()), get("/accounts/ACC-1/statement").accept("application/json"));
    assertThat(r.getResponse().getStatus()).isEqualTo(200);
    assertThat(r.getResponse().getContentType()).startsWith("application/json");
    assertThat(body(r)).startsWith("[{\"date\":\"2026-11-01\"");
  }

  @Test
  @DisplayName("Task 2: Accept application/xml answers 406 Not Acceptable")
  void task2c_406() {
    int s = status(mvc(new StatementController()), get("/accounts/ACC-1/statement").accept("application/xml"));
    assertThat(s).withFailMessage("[task 2: 406] nothing produces XML, expected 406 but got %d", s).isEqualTo(406);
  }

  // ---------- Task 3: consumes ----------

  @Test
  @DisplayName("Task 3: POST /transfers with a JSON body answers 200 'accepted 500 from ACC-1 to ACC-2'")
  void task3a_json() {
    MvcResult r = call(mvc(new TransferController()), post("/transfers").contentType("application/json").content(JSON));
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 3: POST /transfers] expected 200 but got %d",
        r.getResponse().getStatus()).isEqualTo(200);
    assertThat(body(r)).isEqualTo("accepted 500 from ACC-1 to ACC-2");
  }

  @Test
  @DisplayName("Task 3: a text/plain body answers 415 Unsupported Media Type")
  void task3b_415() {
    int s = status(mvc(new TransferController()), post("/transfers").contentType("text/plain").content(JSON));
    assertThat(s).withFailMessage("[task 3: 415] expected 415 for text/plain but got %d. Add consumes", s).isEqualTo(415);
  }

  @Test
  @DisplayName("Task 3: GET /transfers answers 405 (the path exists, only POST is mapped)")
  void task3c_405() {
    int s = status(mvc(new TransferController()), get("/transfers"));
    assertThat(s).withFailMessage("[task 3: 405] expected 405 Method Not Allowed but got %d", s).isEqualTo(405);
  }

  @Test
  @DisplayName("Task 3: a body that is not JSON answers 400, not 500")
  void task3d_badJson() {
    int s = status(mvc(new TransferController()), post("/transfers").contentType("application/json").content("{amount:"));
    assertThat(s).withFailMessage("[task 3: bad json] expected 400 but got %d", s).isEqualTo(400);
  }

  // ---------- Task 4: params and headers ----------

  @Test
  @DisplayName("Task 4: GET /rates?currency=USD answers 83.25")
  void task4a_one() {
    MvcResult r = call(mvc(new RateController()), get("/rates").param("currency", "USD"));
    assertThat(body(r)).withFailMessage("[task 4: params] expected 83.25 but got %s", body(r)).isEqualTo("83.25");
  }

  @Test
  @DisplayName("Task 4: GET /rates (no currency) answers all three rates")
  void task4b_all() {
    MvcResult r = call(mvc(new RateController()), get("/rates"));
    assertThat(r.getResponse().getStatus()).isEqualTo(200);
    assertThat(body(r)).isEqualTo("{\"EUR\":90.1,\"GBP\":105.4,\"USD\":83.25}");
  }

  @Test
  @DisplayName("Task 4: X-Api-Version: 2 picks branchesV2; no header picks V1")
  void task4c_header() {
    MockMvc mvc = mvc(new RateController());
    String v2 = body(call(mvc, get("/branches").header("X-Api-Version", "2")));
    assertThat(v2).withFailMessage("[task 4: headers] with X-Api-Version: 2 expected '[BLR01] [DEL01]' but got %s", v2)
        .isEqualTo("[BLR01] [DEL01]");
    assertThat(body(call(mvc, get("/branches")))).isEqualTo("BLR01,DEL01");
    assertThat(body(call(mvc, get("/branches").header("X-Api-Version", "3")))).isEqualTo("BLR01,DEL01");
  }

  // ---------- Task 5: the built-in answers ----------

  @Test
  @DisplayName("Task 5: statusFor gives 405, 415, 406, 400, 404 and 500 for the right exceptions")
  void task5a_table() throws Exception {
    assertStatus(new org.springframework.web.HttpRequestMethodNotSupportedException("POST"), 405);
    assertStatus(new org.springframework.web.HttpMediaTypeNotSupportedException("text/plain"), 415);
    assertStatus(new org.springframework.web.HttpMediaTypeNotAcceptableException("xml"), 406);
    assertStatus(new org.springframework.web.bind.MissingServletRequestParameterException("amount", "long"), 400);
    assertStatus(new org.springframework.web.servlet.NoHandlerFoundException("GET", "/nope",
        new org.springframework.http.HttpHeaders()), 404);
    assertStatus(new IllegalStateException("boom"), 500);
  }

  @Test
  @DisplayName("Task 5: your table agrees with what Spring really answered for 6 real requests")
  void task5b_matchesReality() {
    MockMvc all = MockMvcBuilders.standaloneSetup(new AccountController(), new StatementController(),
        new TransferController(), new RateController()).build();
    RequestBuilder[] requests = {
        get("/fee"),                                            // missing param
        get("/fee").param("amount", "abc"),                     // type mismatch
        post("/fee"),                                           // wrong method
        get("/no-such-path"),                                   // nothing mapped
        get("/accounts/ACC-1/statement").accept("application/xml"),
        post("/transfers").contentType("text/plain").content("x")};
    for (RequestBuilder rb : requests) {
      MvcResult r = call(all, rb);
      Exception ex = r.getResolvedException();
      if (ex == null) {
        fail("[task 5: reality] request " + r.getRequest().getMethod() + " " + r.getRequest().getRequestURI()
            + " answered " + r.getResponse().getStatus() + " with no exception. Finish tasks 2 and 3 first");
      }
      assertThat(HttpAnswers.statusFor(ex))
          .withFailMessage("[task 5: reality] %s %s threw %s and Spring answered %d, but statusFor says %d",
              r.getRequest().getMethod(), r.getRequest().getRequestURI(), ex.getClass().getSimpleName(),
              r.getResponse().getStatus(), HttpAnswers.statusFor(ex))
          .isEqualTo(r.getResponse().getStatus());
    }
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
  @DisplayName("Task 6: the answer covers the key ideas (dispatcherservlet, 404, 405, 415, produces)")
  void task6c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"dispatcherservlet", "404", "405", "415|406", "produces|consumes"})
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

  private static void assertStatus(Exception e, int expected) {
    int got = HttpAnswers.statusFor(e);
    assertThat(got).withFailMessage("[task 5: table] %s should be %d but statusFor says %d",
        e.getClass().getSimpleName(), expected, got).isEqualTo(expected);
  }

  private static MockMvc mvc(Object controller) {
    try {
      return MockMvcBuilders.standaloneSetup(controller).build();
    } catch (Exception e) {
      return fail("[mapping] the controller could not be mapped: " + e.getMessage());
    }
  }

  private static MvcResult call(MockMvc mvc, RequestBuilder rb) {
    try {
      return mvc.perform(rb).andReturn();
    } catch (Exception e) {
      return fail("[request] the request threw " + e);
    }
  }

  private static int status(MockMvc mvc, RequestBuilder rb) {
    return call(mvc, rb).getResponse().getStatus();
  }

  private static String body(MvcResult r) {
    try {
      return r.getResponse().getContentAsString();
    } catch (Exception e) {
      return fail("cannot read body: " + e);
    }
  }
}
