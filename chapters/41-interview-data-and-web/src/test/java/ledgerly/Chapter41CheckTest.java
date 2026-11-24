package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
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

/** Checks for chapter 41. Users: asha, ravi (customers 1, 2), ops (ADMIN); password = name + "-pass". */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter41CheckTest {

  @Autowired MockMvc mvc;
  @Autowired DemoData demo;
  @Autowired EntityManagerFactory emf;

  @BeforeEach
  void reset() {
    demo.reset();
  }

  /** Returns the response, or status -1 when the request blew up with an exception (e.g. LazyInitialization). */
  private MockHttpServletResponse call(MockHttpServletRequestBuilder rb) {
    try {
      return mvc.perform(rb).andReturn().getResponse();
    } catch (Exception e) {
      MockHttpServletResponse r = new MockHttpServletResponse();
      r.setStatus(-1);
      Throwable c = e;
      while (c.getCause() != null) c = c.getCause();
      try {
        r.getWriter().write("EXCEPTION " + c.getClass().getSimpleName() + ": " + c.getMessage());
      } catch (Exception ignored) {
        // body is only for the message
      }
      return r;
    }
  }

  private static String body(MockHttpServletResponse r) {
    try {
      return r.getContentAsString();
    } catch (Exception e) {
      return "";
    }
  }

  private MockHttpServletResponse as(String user, MockHttpServletRequestBuilder rb) {
    return call(rb.with(httpBasic(user, user + "-pass")));
  }

  private MockHttpServletResponse addPayee(String json) {
    return as("asha", post("/payees").contentType(MediaType.APPLICATION_JSON).content(json));
  }

  private static final String KIRAN = "{\"name\":\"Kiran Rao\",\"ifsc\":\"HDFC0001234\",\"accountNumber\":\"50100012345678\"}";

  private static void status(String task, MockHttpServletResponse r, int expected) {
    assertThat(r.getStatus()).withFailMessage("[%s] status %s, body %s", task, r.getStatus(), body(r))
        .isEqualTo(expected);
  }

  // ---------- Example (green on the stub, stays green) ----------

  @Test
  @DisplayName("Example: no login -> 401; asha can add a payee (2xx)")
  void task0_examples() {
    status("example: no login", call(get("/payees")), 401);
    assertThat(addPayee(KIRAN).getStatus()).isBetween(200, 299);
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: GET /customers (as ops) -> Asha 2 accounts, Ravi 1, Meera 3")
  void task1a_list() {
    var r = as("ops", get("/customers"));
    status("task 1: lazy loading needs a transaction", r, 200);
    assertThat(body(r)).isEqualTo("[{\"id\":1,\"name\":\"Asha\",\"accounts\":2},{\"id\":2,\"name\":\"Ravi\",\"accounts\":1},"
        + "{\"id\":3,\"name\":\"Meera\",\"accounts\":3}]");
  }

  @Test
  @DisplayName("Task 1: GET /customers runs ONE SQL statement (no N+1)")
  void task1b_noNPlusOne() {
    var stats = emf.unwrap(SessionFactory.class).getStatistics();
    stats.setStatisticsEnabled(true);
    stats.clear();
    var r = as("ops", get("/customers"));
    long statements = stats.getPrepareStatementCount();
    status("task 1: list", r, 200);
    assertThat(statements).withFailMessage("[task 1: N+1] GET /customers ran %s SQL statements (1 for customers + 1"
        + " per customer); use a join fetch", statements).isEqualTo(1);
  }

  @Test
  @DisplayName("Task 1: GET /customers/1 (as asha) -> her two accounts")
  void task1c_detail() {
    var r = as("asha", get("/customers/1"));
    status("task 1: detail", r, 200);
    assertThat(body(r)).contains("\"name\":\"Asha\"", "\"number\":\"ACC-1\"", "\"number\":\"ACC-2\"");
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: POST /payees -> 201 + Location /payees/{id}")
  void task2a_created() {
    var r = addPayee(KIRAN);
    status("task 2: 201", r, 201);
    Object id = JsonPath.read(body(r), "$.id");
    assertThat(r.getHeader("Location")).isEqualTo("/payees/" + id);
  }

  @Test
  @DisplayName("Task 2: the same account number again -> 409 problem 'Duplicate payee'")
  void task2b_conflict() {
    addPayee(KIRAN);
    var r = addPayee(KIRAN);
    status("task 2: 409", r, 409);
    assertThat(body(r)).contains("\"title\":\"Duplicate payee\"", "already exists");
  }

  @Test
  @DisplayName("Task 2: GET /payees/999 -> 404 problem 'no payee 999'")
  void task2c_notFound() {
    var r = as("asha", get("/payees/999"));
    status("task 2: 404", r, 404);
    assertThat(body(r)).contains("\"title\":\"Not found\"", "no payee 999");
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: blank name, bad IFSC, short account number -> 400 naming all three fields")
  void task3a_body() {
    var r = addPayee("{\"name\":\" \",\"ifsc\":\"HDFC1234\",\"accountNumber\":\"123\"}");
    status("task 3: @Valid", r, 400);
    assertThat(body(r)).contains("name", "ifsc", "accountNumber");
  }

  @Test
  @DisplayName("Task 3: GET /payees?size=500 -> 400; size=100 -> 200")
  void task3b_param() {
    status("task 3: @Max(100)", as("asha", get("/payees").param("size", "500")), 400);
    status("task 3: size 100 is fine", as("asha", get("/payees").param("size", "100")), 200);
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: GET /customers -> ops 200, asha 403")
  void task4a_adminList() {
    status("task 4: admin", as("ops", get("/customers")), 200);
    status("task 4: URL rule", as("asha", get("/customers")), 403);
  }

  @Test
  @DisplayName("Task 4: GET /customers/2 -> ravi 200, asha 403, ops 200")
  void task4b_ownDetail() {
    status("task 4: self", as("ravi", get("/customers/2")), 200);
    status("task 4: @PreAuthorize", as("asha", get("/customers/2")), 403);
    status("task 4: admin", as("ops", get("/customers/2")), 200);
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
  @DisplayName("Task 5: the answer covers the key ideas (dispatcher, n+1, transaction, valid, 401, 403)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"dispatcher", "n+1|join fetch", "transaction", "valid", "401", "403"})
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
