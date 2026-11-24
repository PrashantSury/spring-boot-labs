package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;

/**
 * Checks for chapter 18. The app seeds 45 transactions for ACC-1 (UPI #1 .. UPI #45, newest = #45)
 * and 3 for ACC-2. Largest ACC-1 amount: UPI #36 (5032); smallest: UPI #37 (169).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter18CheckTest {

  @Autowired MockMvc mvc;
  @Autowired TxnRepository repo;

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: findByAccountNumber(ACC-1, page 0 size 20) has 20 rows, 45 total, 3 pages")
  void task1a_page() {
    var page = repo1(() -> repo.findByAccountNumber("ACC-1", PageRequest.of(0, 20)));
    assertThat(page.getContent()).hasSize(20);
    assertThat(page.getTotalElements()).isEqualTo(45);
    assertThat(page.getTotalPages()).isEqualTo(3);
  }

  @Test
  @DisplayName("Task 1: the last page (2) of size 20 has the remaining 5 rows; ACC-2 has 3 rows in total")
  void task1b_lastPage() {
    assertThat(repo1(() -> repo.findByAccountNumber("ACC-1", PageRequest.of(2, 20))).getContent()).hasSize(5);
    assertThat(repo1(() -> repo.findByAccountNumber("ACC-2", PageRequest.of(0, 20))).getTotalElements()).isEqualTo(3);
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: GET /accounts/ACC-1/transactions -> page 0, size 20, 45 total, 3 pages, newest first (UPI #45)")
  void task2a_defaults() {
    String b = body(get("/accounts/ACC-1/transactions"));
    assertThat(List.of(read(b, "$.page"), read(b, "$.size"), read(b, "$.totalElements"), read(b, "$.totalPages")))
        .withFailMessage("[task 2: defaults] page,size,total,pages = %s,%s,%s,%s", read(b, "$.page"), read(b, "$.size"),
            read(b, "$.totalElements"), read(b, "$.totalPages"))
        .containsExactly(0, 20, 45, 3);
    assertThat(read(b, "$.content[0].description")).withFailMessage("[task 2: default sort] first row is %s,"
        + " expected UPI #45 (bookedAt desc)", read(b, "$.content[0].description")).isEqualTo("UPI #45");
  }

  @Test
  @DisplayName("Task 2: ?page=2&size=20 has 5 rows; ?page=1&size=10 starts with UPI #35")
  void task2b_paging() {
    assertThat((List<?>) read(body(get("/accounts/ACC-1/transactions").param("page", "2")), "$.content")).hasSize(5);
    String b = body(get("/accounts/ACC-1/transactions").param("page", "1").param("size", "10"));
    assertThat(read(b, "$.content[0].description")).isEqualTo("UPI #35");
  }

  @Test
  @DisplayName("Task 2: ?size=500 is cut to 100 (size in the response is 100)")
  void task2c_cap() {
    String b = body(get("/accounts/ACC-1/transactions").param("size", "500"));
    assertThat(read(b, "$.size")).withFailMessage("[task 2: cap] size=500 should be served as 100, got %s",
        read(b, "$.size")).isEqualTo(100);
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: sortFrom(\"amount,desc\") is amount DESC then id DESC; \"amount\" alone is ASC")
  void task3a_parse() {
    Sort s = StatementController.sortFrom("amount,desc");
    assertThat(s.getOrderFor("amount")).withFailMessage("[task 3: parse] no order for amount in %s", s).isNotNull();
    assertThat(s.getOrderFor("amount").getDirection()).isEqualTo(Sort.Direction.DESC);
    assertThat(s.getOrderFor("id")).withFailMessage("[task 3: tie-breaker] add id as a second sort: %s", s).isNotNull();
    assertThat(StatementController.sortFrom("amount").getOrderFor("amount").getDirection()).isEqualTo(Sort.Direction.ASC);
  }

  @Test
  @DisplayName("Task 3: ?sort=amount,desc starts with UPI #36 (5032); ?sort=amount,asc with UPI #37 (169)")
  void task3b_http() {
    String desc = body(get("/accounts/ACC-1/transactions").param("sort", "amount,desc"));
    assertThat(read(desc, "$.content[0].amount")).isEqualTo(5032);
    String asc = body(get("/accounts/ACC-1/transactions").param("sort", "amount,asc"));
    assertThat(read(asc, "$.content[0].description")).isEqualTo("UPI #37");
  }

  @Test
  @DisplayName("Task 3: sorting by 'description' or 'id; drop table' is 400 (whitelist)")
  void task3c_whitelist() {
    for (String bad : List.of("description", "id; drop table txn")) {
      Throwable t = catchThrowable(() -> StatementController.sortFrom(bad));
      assertThat(t).withFailMessage("[task 3: whitelist] sort '%s' was accepted", bad)
          .isInstanceOf(ResponseStatusException.class);
      assertThat(((ResponseStatusException) t).getStatusCode().value()).isEqualTo(400);
    }
    assertThat(call(get("/accounts/ACC-1/transactions").param("sort", "description")).getResponse().getStatus()).isEqualTo(400);
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: GET /accounts/ACC-1/feed -> 10 rows, page 0, hasNext true, newest first")
  void task4a_first() {
    String b = body(get("/accounts/ACC-1/feed"));
    assertThat((List<?>) read(b, "$.content")).hasSize(10);
    assertThat(read(b, "$.hasNext")).isEqualTo(true);
    assertThat(read(b, "$.content[0].description")).isEqualTo("UPI #45");
  }

  @Test
  @DisplayName("Task 4: feed page 4 has 5 rows and hasNext false; the JSON has no totalElements")
  void task4b_last() {
    String b = body(get("/accounts/ACC-1/feed").param("page", "4"));
    assertThat((List<?>) read(b, "$.content")).hasSize(5);
    assertThat(read(b, "$.hasNext")).isEqualTo(false);
    assertThat(b).doesNotContain("totalElements");
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
  @DisplayName("Task 5: the answer covers the key ideas (pageable, page, slice, count, whitelist)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"pageable", "page", "slice", "count", "whitelist"})
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

  private static <T> T repo1(java.util.function.Supplier<T> s) {
    try {
      return s.get();
    } catch (UnsupportedOperationException e) {
      return fail("[stub] " + e.getMessage());
    }
  }

  private MvcResult call(org.springframework.test.web.servlet.RequestBuilder rb) {
    try {
      return mvc.perform(rb).andReturn();
    } catch (Exception e) {
      return fail("[request] the request threw " + (e.getCause() != null ? e.getCause() : e));
    }
  }

  private String body(org.springframework.test.web.servlet.RequestBuilder rb) {
    MvcResult r = call(rb);
    try {
      assertThat(r.getResponse().getStatus()).withFailMessage("[request] expected 200 but got %d",
          r.getResponse().getStatus()).isEqualTo(200);
      return r.getResponse().getContentAsString();
    } catch (java.io.UnsupportedEncodingException e) {
      return fail(e.toString());
    }
  }

  private static Object read(String json, String path) {
    try {
      return JsonPath.read(json, path);
    } catch (Exception e) {
      return null;
    }
  }
}
