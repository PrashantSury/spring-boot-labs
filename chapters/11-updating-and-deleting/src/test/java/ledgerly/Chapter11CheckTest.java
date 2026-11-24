package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.jayway.jsonpath.JsonPath;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Checks for chapter 11. Every test starts with payee 1 = Vikram Shah, nickname Landlord, version 1. */
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter11CheckTest {

  private static final String FULL = "{\"name\":\"Vikram S\",\"accountNumber\":\"50100012345678\",\"ifsc\":\"HDFC0001234\"}";

  private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new PayeeController()).build();

  // ---------- Task 1: PUT ----------

  @Test
  @DisplayName("Task 1: PUT replaces the payee: new name, nickname null (not sent), version 2")
  void task1a_replace() {
    MvcResult r = call(put("/payees/1").contentType("application/json").content(FULL));
    expect(r, 200, "task 1: PUT");
    assertThat((String) json(r, "$.name")).isEqualTo("Vikram S");
    assertThat((Object) json(r, "$.nickname")).withFailMessage("[task 1: replace] nickname was not sent, so PUT must"
        + " make it null, but it is %s", (Object) json(r, "$.nickname")).isNull();
    assertThat((Integer) json(r, "$.version")).isEqualTo(2);
    assertThat(r.getResponse().getHeader("ETag")).isEqualTo("\"2\"");
  }

  @Test
  @DisplayName("Task 1: PUT on an unknown id is 404 and creates nothing")
  void task1b_unknown() {
    expect(call(put("/payees/9").contentType("application/json").content(FULL)), 404, "task 1: unknown id");
    expect(call(get("/payees/9")), 404, "task 1: PUT must not create");
  }

  @Test
  @DisplayName("Task 1: PUT without ifsc is 400 and the payee is unchanged")
  void task1c_incomplete() {
    expect(call(put("/payees/1").contentType("application/json").content("{\"name\":\"X\",\"accountNumber\":\"1\"}")),
        400, "task 1: incomplete");
    assertThat((Integer) json(call(get("/payees/1")), "$.version")).isEqualTo(1);
  }

  @Test
  @DisplayName("Task 1: the same PUT twice gives the same payee (only the version moves)")
  void task1d_idempotent() {
    MvcResult a = call(put("/payees/1").contentType("application/json").content(FULL));
    MvcResult b = call(put("/payees/1").contentType("application/json").content(FULL));
    expect(b, 200, "task 1: second PUT");
    assertThat((String) json(b, "$.name")).isEqualTo((String) json(a, "$.name"));
    assertThat((Object) json(b, "$.nickname")).isNull();
  }

  // ---------- Task 2: PATCH ----------

  @Test
  @DisplayName("Task 2: PATCH {nickname: Rent} changes only the nickname")
  void task2a_onlyOne() {
    MvcResult r = call(patch("/payees/1").contentType("application/json").content("{\"nickname\":\"Rent\"}"));
    expect(r, 200, "task 2: PATCH");
    assertThat((String) json(r, "$.nickname")).isEqualTo("Rent");
    assertThat((String) json(r, "$.name")).withFailMessage("[task 2: merge] name must stay Vikram Shah").isEqualTo("Vikram Shah");
    assertThat((String) json(r, "$.ifsc")).isEqualTo("HDFC0001234");
    assertThat((Integer) json(r, "$.version")).isEqualTo(2);
  }

  @Test
  @DisplayName("Task 2: PATCH {nickname: null} clears it (present-with-null is not absent)")
  void task2b_explicitNull() {
    MvcResult r = call(patch("/payees/1").contentType("application/json").content("{\"nickname\":null}"));
    expect(r, 200, "task 2: null");
    assertThat((Object) json(r, "$.nickname")).withFailMessage("[task 2: null] the key was sent with null: clear it").isNull();
  }

  @Test
  @DisplayName("Task 2: PATCH {} is 200 and changes nothing, not even the version")
  void task2c_empty() {
    MvcResult r = call(patch("/payees/1").contentType("application/json").content("{}"));
    expect(r, 200, "task 2: empty");
    assertThat((String) json(r, "$.nickname")).isEqualTo("Landlord");
    assertThat((Integer) json(r, "$.version")).isEqualTo(1);
  }

  @Test
  @DisplayName("Task 2: PATCH accountNumber, an unknown key, or a blank name is 400")
  void task2d_refused() {
    expect(call(patch("/payees/1").contentType("application/json").content("{\"accountNumber\":\"999\"}")), 400,
        "task 2: accountNumber cannot change");
    expect(call(patch("/payees/1").contentType("application/json").content("{\"colour\":\"red\"}")), 400,
        "task 2: unknown key");
    expect(call(patch("/payees/1").contentType("application/json").content("{\"name\":\" \"}")), 400,
        "task 2: blank name");
    assertThat((Integer) json(call(get("/payees/1")), "$.version")).isEqualTo(1);
  }

  @Test
  @DisplayName("Task 2: PATCH on an unknown id is 404")
  void task2e_unknown() {
    expect(call(patch("/payees/9").contentType("application/json").content("{\"nickname\":\"x\"}")), 404, "task 2: unknown");
  }

  // ---------- Task 3: DELETE ----------

  @Test
  @DisplayName("Task 3: DELETE twice answers 204 both times, then GET is 404")
  void task3a_twice() {
    expect(call(delete("/payees/1")), 204, "task 3: first DELETE");
    expect(call(delete("/payees/1")), 204, "task 3: second DELETE (idempotent)");
    expect(call(get("/payees/1")), 404, "task 3: gone");
  }

  @Test
  @DisplayName("Task 3: DELETE of an id that never existed is 204 too")
  void task3b_never() {
    expect(call(delete("/payees/777")), 204, "task 3: never existed");
  }

  // ---------- Task 4: If-Match ----------

  @Test
  @DisplayName("Task 4: PUT with a stale If-Match is 412 and nothing changes")
  void task4a_putStale() {
    expect(call(put("/payees/1").contentType("application/json").header("If-Match", "\"7\"").content(FULL)), 412,
        "task 4: stale PUT");
    assertThat((String) json(call(get("/payees/1")), "$.name")).isEqualTo("Vikram Shah");
  }

  @Test
  @DisplayName("Task 4: PATCH with the current If-Match works; replaying the same If-Match is 412")
  void task4b_patchLostUpdate() {
    MvcResult first = call(patch("/payees/1").contentType("application/json").header("If-Match", "\"1\"")
        .content("{\"nickname\":\"Rent\"}"));
    expect(first, 200, "task 4: current If-Match");
    expect(call(patch("/payees/1").contentType("application/json").header("If-Match", "\"1\"")
        .content("{\"nickname\":\"Old\"}")), 412, "task 4: lost update");
    assertThat((String) json(call(get("/payees/1")), "$.nickname")).isEqualTo("Rent");
  }

  @Test
  @DisplayName("Task 4: stale() is false without If-Match and compares quoted versions")
  void task4c_stale() {
    Payee p = new Payee(1, "A", "1", "I", null, 3);
    assertThat(PayeeController.stale(p, null)).isFalse();
    assertThat(PayeeController.stale(p, "\"3\"")).isFalse();
    assertThat(PayeeController.stale(p, "\"2\"")).isTrue();
  }

  // ---------- Task 5: your own CRUD tests ----------

  @Test
  @DisplayName("Task 5: PayeeCrudTest has at least 4 @Test methods using put, patch and delete")
  void task5a_ownTests() throws Exception {
    Path f = Path.of(System.getProperty("user.dir"), "src/test/java/ledgerly/PayeeCrudTest.java");
    String src = Files.readString(f);
    int tests = src.split("@Test\\b", -1).length - 1;
    assertThat(tests).withFailMessage("[task 5: your tests] PayeeCrudTest has %d @Test methods; write at least 4", tests)
        .isGreaterThanOrEqualTo(4);
    assertThat(src).withFailMessage("[task 5: coverage] your tests should call put(, patch( and delete(")
        .contains("put(").contains("patch(").contains("delete(");
    int names = src.split("@DisplayName", -1).length - 1;
    assertThat(names).withFailMessage("[task 5: names] give each test a @DisplayName").isGreaterThanOrEqualTo(tests);
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
  @DisplayName("Task 6: the answer covers the key ideas (put, patch, idempoten, 404, if-match)")
  void task6c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"put", "patch", "idempoten", "404|204", "if-match|version"})
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

  private MvcResult call(RequestBuilder rb) {
    try {
      return mvc.perform(rb).andReturn();
    } catch (Exception e) {
      return fail("[request] the request threw " + e);
    }
  }

  private static void expect(MvcResult r, int status, String label) {
    assertThat(r.getResponse().getStatus()).withFailMessage("[%s] expected %d but got %d", label, status,
        r.getResponse().getStatus()).isEqualTo(status);
  }

  @SuppressWarnings("unchecked")
  private static <T> T json(MvcResult r, String path) {
    try {
      return (T) JsonPath.parse(r.getResponse().getContentAsString()).read(path);
    } catch (com.jayway.jsonpath.PathNotFoundException e) {
      return null;
    } catch (Exception e) {
      return fail("[json] cannot read " + path + " (status " + r.getResponse().getStatus() + "): " + e.getMessage());
    }
  }
}
