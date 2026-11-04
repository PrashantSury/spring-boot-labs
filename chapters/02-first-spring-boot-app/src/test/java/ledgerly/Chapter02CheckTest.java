package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Checks for chapter 2. Read the failure message: it says what was expected.
 * Tasks 1 to 5 call AccountController directly, so they do not need task 6.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter02CheckTest {

  private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new AccountController()).build();

  // ---------- Task 1: GET /status ----------

  @Test
  @DisplayName("Task 1: GET /status answers 200 with the text 'Ledgerly is open'")
  void task1a_status() {
    MvcResult r = call("task 1: GET /status", get("/status"));
    expectStatus("task 1: GET /status", r, 200, "map the method with @GetMapping(\"/status\")");
    assertThat(body(r))
        .withFailMessage("[task 1: GET /status] expected the text 'Ledgerly is open' but got '%s'", body(r))
        .isEqualTo("Ledgerly is open");
  }

  @Test
  @DisplayName("Task 1: /status is plain text, not JSON")
  void task1b_plainText() {
    MvcResult r = call("task 1: GET /status", get("/status"));
    expectStatus("task 1: GET /status", r, 200, "map the method with @GetMapping(\"/status\")");
    assertThat(r.getResponse().getContentType())
        .withFailMessage("[task 1: content type] expected text/plain but got %s. Return a String",
            r.getResponse().getContentType())
        .startsWith("text/plain");
  }

  @Test
  @DisplayName("Task 1: POST /status is refused with 405 (only GET is mapped)")
  void task1c_onlyGet() {
    MvcResult r = call("task 1: POST /status", post("/status"));
    assertThat(r.getResponse().getStatus())
        .withFailMessage("[task 1: POST /status] expected 405 Method Not Allowed but got %d."
            + " Use @GetMapping, which only allows GET", r.getResponse().getStatus())
        .isEqualTo(405);
  }

  // ---------- Task 2: GET /accounts ----------

  @Test
  @DisplayName("Task 2: GET /accounts answers 200 with a JSON list of four accounts")
  void task2a_list() {
    MvcResult r = call("task 2: GET /accounts", get("/accounts"));
    expectStatus("task 2: GET /accounts", r, 200, "map the method with @GetMapping(\"/accounts\")");
    assertThat(r.getResponse().getContentType())
        .withFailMessage("[task 2: content type] expected application/json but got %s",
            r.getResponse().getContentType())
        .startsWith("application/json");
    int size = JsonPath.parse(body(r)).read("$.length()");
    assertThat(size)
        .withFailMessage("[task 2: GET /accounts] expected 4 accounts but got %d", size)
        .isEqualTo(4);
  }

  @Test
  @DisplayName("Task 2: the accounts keep their order and fields")
  void task2b_orderAndFields() {
    MvcResult r = call("task 2: GET /accounts", get("/accounts"));
    expectStatus("task 2: GET /accounts", r, 200, "map the method with @GetMapping(\"/accounts\")");
    var json = JsonPath.parse(body(r));
    assertThat((String) json.read("$[0].holder"))
        .withFailMessage("[task 2: first account] expected Asha Rao first (ACCOUNTS order)")
        .isEqualTo("Asha Rao");
    assertThat((String) json.read("$[3].holder"))
        .withFailMessage("[task 2: last account] expected Imran Khan last (ACCOUNTS order)")
        .isEqualTo("Imran Khan");
    assertThat((String) json.read("$[0].number"))
        .withFailMessage("[task 2: fields] each account needs a 'number' field")
        .isEqualTo("1000234567");
    assertThat((Integer) json.read("$[2].balance"))
        .withFailMessage("[task 2: fields] each account needs a numeric 'balance' field")
        .isEqualTo(125000);
  }

  // ---------- Task 3: GET /accounts/{number}/masked ----------

  @Test
  @DisplayName("Task 3: GET /accounts/1000234567/masked answers ******4567")
  void task3a_masked() {
    MvcResult r = call("task 3: GET /accounts/1000234567/masked", get("/accounts/1000234567/masked"));
    expectStatus("task 3: masked", r, 200,
        "map @GetMapping(\"/accounts/{number}/masked\") and read {number} with @PathVariable");
    assertThat(body(r))
        .withFailMessage("[task 3: masked] expected '******4567' but got '%s'", body(r))
        .isEqualTo("******4567");
  }

  @Test
  @DisplayName("Task 3: the number is a String, so leading zeros survive")
  void task3b_leadingZeros() {
    MvcResult r = call("task 3: GET /accounts/0000123456/masked", get("/accounts/0000123456/masked"));
    expectStatus("task 3: masked", r, 200,
        "map @GetMapping(\"/accounts/{number}/masked\") and read {number} with @PathVariable");
    assertThat(body(r))
        .withFailMessage("[task 3: leading zeros] expected '******3456' but got '%s'."
            + " Keep the path variable a String", body(r))
        .isEqualTo("******3456");
  }

  // ---------- Task 4: GET /accounts/above?balance=N ----------

  @Test
  @DisplayName("Task 4: balance=10000 answers Asha Rao and Neha Iyer, in order")
  void task4a_above() {
    MvcResult r = call("task 4: GET /accounts/above?balance=10000", get("/accounts/above").param("balance", "10000"));
    expectStatus("task 4: above", r, 200,
        "map @GetMapping(\"/accounts/above\") and read balance with @RequestParam");
    var json = JsonPath.parse(body(r));
    int size = json.read("$.length()");
    assertThat(size)
        .withFailMessage("[task 4: above 10000] expected 2 accounts (Asha Rao 52000, Neha Iyer 125000) but got %d", size)
        .isEqualTo(2);
    assertThat((String) json.read("$[0].holder")).isEqualTo("Asha Rao");
    assertThat((String) json.read("$[1].holder")).isEqualTo("Neha Iyer");
  }

  @Test
  @DisplayName("Task 4: MORE than, not 'at least': balance=52000 skips the 52000 account")
  void task4b_strict() {
    MvcResult r = call("task 4: GET /accounts/above?balance=52000", get("/accounts/above").param("balance", "52000"));
    expectStatus("task 4: above", r, 200,
        "map @GetMapping(\"/accounts/above\") and read balance with @RequestParam");
    int size = JsonPath.parse(body(r)).read("$.length()");
    assertThat(size)
        .withFailMessage("[task 4: strictly more] balance=52000 expected 1 account (Neha Iyer) but got %d."
            + " Asha Rao has exactly 52000, which is not MORE than 52000", size)
        .isEqualTo(1);
  }

  @Test
  @DisplayName("Task 4: nothing matches, answer an empty list")
  void task4c_empty() {
    MvcResult r = call("task 4: GET /accounts/above?balance=1000000", get("/accounts/above").param("balance", "1000000"));
    expectStatus("task 4: above", r, 200, "map @GetMapping(\"/accounts/above\")");
    assertThat(body(r)).withFailMessage("[task 4: empty] expected [] but got %s", body(r)).isEqualTo("[]");
  }

  @Test
  @DisplayName("Task 4: a balance that is not a number is a 400, and you write no code for it")
  void task4d_badBalance() {
    MvcResult r = call("task 4: GET /accounts/above?balance=lots", get("/accounts/above").param("balance", "lots"));
    assertThat(r.getResponse().getStatus())
        .withFailMessage("[task 4: balance=lots] expected 400 Bad Request but got %d."
            + " Declare the parameter as an int and Spring answers 400 for you", r.getResponse().getStatus())
        .isEqualTo(400);
  }

  // ---------- Task 5: GET /fee ----------

  @Test
  @DisplayName("Task 5: amount=5000 costs 50")
  void task5a_onePercent() {
    assertThat(fee("amount=5000"))
        .withFailMessage("[task 5: 1%% of 5000] expected 50")
        .isEqualTo("50");
  }

  @Test
  @DisplayName("Task 5: the fee is never below 10")
  void task5b_minimum() {
    assertThat(fee("amount=200"))
        .withFailMessage("[task 5: minimum] amount=200 expected 10 (1%% would be 2, the minimum is 10)")
        .isEqualTo("10");
  }

  @Test
  @DisplayName("Task 5: express=true doubles the fee")
  void task5c_express() {
    assertThat(fee("amount=5000&express=true"))
        .withFailMessage("[task 5: express] amount=5000&express=true expected 100")
        .isEqualTo("100");
    assertThat(fee("amount=200&express=true"))
        .withFailMessage("[task 5: express minimum] amount=200&express=true expected 20 (10 doubled)")
        .isEqualTo("20");
  }

  @Test
  @DisplayName("Task 5: express is optional, and amount is required")
  void task5d_optionalAndRequired() {
    MvcResult r = call("task 5: GET /fee", get("/fee"));
    assertThat(r.getResponse().getStatus())
        .withFailMessage("[task 5: missing amount] GET /fee with no amount expected 400 but got %d."
            + " A plain @RequestParam is required", r.getResponse().getStatus())
        .isEqualTo(400);
  }

  // ---------- Task 6 (Interview): @SpringBootApplication ----------

  @Test
  @DisplayName("Task 6: LedgerlyApplication is a Spring Boot configuration")
  void task6a_configuration() {
    assertThat(AnnotatedElementUtils.hasAnnotation(LedgerlyApplication.class, SpringBootConfiguration.class))
        .withFailMessage("[task 6: configuration] LedgerlyApplication is not a @SpringBootConfiguration."
            + " Which one annotation includes it?")
        .isTrue();
  }

  @Test
  @DisplayName("Task 6: LedgerlyApplication enables auto-configuration")
  void task6b_autoConfiguration() {
    assertThat(AnnotatedElementUtils.hasAnnotation(LedgerlyApplication.class, EnableAutoConfiguration.class))
        .withFailMessage("[task 6: auto-configuration] LedgerlyApplication does not enable auto-configuration")
        .isTrue();
  }

  @Test
  @DisplayName("Task 6: LedgerlyApplication scans for components")
  void task6c_componentScan() {
    assertThat(AnnotatedElementUtils.hasAnnotation(LedgerlyApplication.class, ComponentScan.class))
        .withFailMessage("[task 6: component scan] LedgerlyApplication does not scan for components")
        .isTrue();
  }

  @Test
  @DisplayName("Task 6: the app really starts, and finds AccountController")
  void task6d_starts() {
    try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(LedgerlyApplication.class)
        .web(WebApplicationType.SERVLET)
        .properties("server.port=0")
        .run()) {
      assertThat(ctx.getBeanNamesForType(AccountController.class))
          .withFailMessage("[task 6: component scan] the app started, but AccountController is not a bean")
          .isNotEmpty();
    } catch (AssertionError e) {
      throw e;
    } catch (Exception e) {
      Throwable root = e;
      while (root.getCause() != null) {
        root = root.getCause();
      }
      fail("[task 6: start] the app did not start: " + root.getMessage());
    }
  }

  // ---------- helpers ----------

  private String fee(String query) {
    MvcResult r = call("task 5: GET /fee?" + query, get("/fee?" + query));
    expectStatus("task 5: GET /fee?" + query, r, 200,
        "map @GetMapping(\"/fee\") and read amount and express with @RequestParam");
    return body(r);
  }

  private MvcResult call(String label, MockHttpServletRequestBuilder request) {
    try {
      return mvc.perform(request.accept(MediaType.ALL)).andReturn();
    } catch (Exception e) {
      Throwable root = e;
      while (root.getCause() != null) {
        root = root.getCause();
      }
      return fail("[" + label + "] your method threw " + root.getClass().getSimpleName() + ": " + root.getMessage());
    }
  }

  private static void expectStatus(String label, MvcResult r, int expected, String hint) {
    int actual = r.getResponse().getStatus();
    assertThat(actual)
        .withFailMessage("[%s] expected status %d but got %d. Hint: %s", label, expected, actual, hint)
        .isEqualTo(expected);
  }

  private static String body(MvcResult r) {
    try {
      return r.getResponse().getContentAsString();
    } catch (Exception e) {
      return fail("could not read the response body: " + e.getMessage());
    }
  }
}
