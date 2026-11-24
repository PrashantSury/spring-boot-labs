package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/**
 * Checks for chapter 19 on SampleData: ACC-1 = RENT 25000+25000, GROCERY 1200+800+450, TRAVEL 3200
 * (Oct total 27000, Nov total 28650); ACC-2 = GROCERY 300.
 */
@DataJpaTest
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter19CheckTest {

  @Autowired TxnRepository repo;

  @BeforeEach
  void seed() {
    repo.saveAll(SampleData.txns());
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: ACC-1 lines, biggest first: 25000, 25000, 3200, 1200, 800, 450")
  void task1a_lines() {
    List<Long> amounts = run("1", () -> repo.findByAccountNumberOrderByAmountDesc("ACC-1")).stream().map(TxnLine::getAmount).toList();
    assertThat(amounts).containsExactly(25000L, 25000L, 3200L, 1200L, 800L, 450L);
  }

  @Test
  @DisplayName("Task 1: ACC-2 has exactly one line: Blinkit")
  void task1b_projection() {
    List<TxnLine> lines = run("1", () -> repo.findByAccountNumberOrderByAmountDesc("ACC-2"));
    assertThat(lines).hasSize(1);
    assertThat(lines.get(0).getDescription()).isEqualTo("Blinkit");
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: ACC-1 by category = RENT 50000 (2), TRAVEL 3200 (1), GROCERY 2450 (3)")
  void task2a_groups() {
    assertThat(run("2", () -> repo.spendByCategory("ACC-1"))).containsExactly(
        new CategoryTotal("RENT", 50000, 2), new CategoryTotal("TRAVEL", 3200, 1), new CategoryTotal("GROCERY", 2450, 3));
  }

  @Test
  @DisplayName("Task 2: ACC-2's 300 grocery is not mixed into ACC-1, and ACC-2 alone is GROCERY 300 (1)")
  void task2b_filtered() {
    assertThat(run("2", () -> repo.spendByCategory("ACC-2"))).containsExactly(new CategoryTotal("GROCERY", 300, 1));
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: totalSpent(ACC-1) is 55650")
  void task3a_total() {
    assertThat(run("3", () -> repo.totalSpent("ACC-1"))).isEqualTo(55650L);
  }

  @Test
  @DisplayName("Task 3: totalSpent(ACC-9) is 0 (SUM over no rows is NULL: coalesce it)")
  void task3b_empty() {
    try {
      assertThat(run("3", () -> repo.totalSpent("ACC-9"))).isZero();
    } catch (org.springframework.aop.AopInvocationException e) {
      fail("[task 3: null sum] " + e.getMessage() + ". Wrap the sum in coalesce(..., 0)");
    }
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: monthlyTotals(ACC-1) = 2026-10 27000, 2026-11 28650")
  void task4a_monthly() {
    List<String> got = run("4", () -> repo.monthlyTotals("ACC-1")).stream().map(m -> m.getPeriod() + " " + m.getTotal()).toList();
    assertThat(got).withFailMessage("[task 4] expected [2026-10 27000, 2026-11 28650] but got %s", got)
        .containsExactly("2026-10 27000", "2026-11 28650");
  }

  @Test
  @DisplayName("Task 4: the monthly method is a native query (nativeQuery = true)")
  void task4b_native() throws Exception {
    var q = TxnRepository.class.getMethod("monthlyTotals", String.class)
        .getAnnotation(org.springframework.data.jpa.repository.Query.class);
    assertThat(q).withFailMessage("[task 4: native] add @Query(value = ..., nativeQuery = true)").isNotNull();
    assertThat(q.nativeQuery()).isTrue();
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
  @DisplayName("Task 5: the answer covers the key ideas (projection, group by, coalesce, native, constructor)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"projection", "group by", "coalesce|null", "native", "constructor|select new"})
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

  private static <T> T run(String task, Supplier<T> call) {
    try {
      return call.get();
    } catch (UnsupportedOperationException e) {
      return fail("[task " + task + "] still the stub: " + e.getMessage());
    }
  }
}
