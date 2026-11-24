package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
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
 * Checks for chapter 17. @DataJpaTest starts only JPA + H2 + your repository; every test starts
 * from the same 4 accounts and is rolled back.
 */
@DataJpaTest
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter17CheckTest {

  @Autowired AccountRepository repo;

  @BeforeEach
  void seed() {
    repo.saveAll(List.of(
        new Account("ACC-1", "Asha Rao", 7, new BigDecimal("52000.00")),
        new Account("ACC-2", "Asha Rao", 7, new BigDecimal("900.00")),
        new Account("ACC-3", "Asha Rao", 7, new BigDecimal("15000.00")),
        new Account("ACC-4", "Vikram Shah", 8, new BigDecimal("80000.00"))));
  }

  @Test
  @DisplayName("Free from JpaRepository: count() is 4 (stays green)")
  void task0_free() {
    assertThat(repo.count()).isEqualTo(4);
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: findByNumber(\"ACC-3\") finds the ₹15,000 account")
  void task1a_found() {
    var a = run("1", () -> repo.findByNumber("ACC-3"));
    assertThat(a).hasValueSatisfying(x -> assertThat(x.getBalance()).isEqualByComparingTo("15000"));
  }

  @Test
  @DisplayName("Task 1: findByNumber(\"ACC-9\") is Optional.empty(), not null")
  void task1b_empty() {
    assertThat(run("1", () -> repo.findByNumber("ACC-9"))).isNotNull().isEmpty();
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: customer 7 with balance > 1000 is ACC-1, ACC-3 (biggest first); ACC-2 and ACC-4 are out")
  void task2a_derived() {
    List<String> got = run("2", () -> repo.findByCustomerIdAndBalanceGreaterThanOrderByBalanceDesc(7, new BigDecimal("1000")))
        .stream().map(Account::getNumber).toList();
    assertThat(got).withFailMessage("[task 2] expected [ACC-1, ACC-3] but got %s", got).containsExactly("ACC-1", "ACC-3");
  }

  @Test
  @DisplayName("Task 2: GreaterThan is strict: balance > 15000 for customer 7 is only ACC-1")
  void task2b_strict() {
    List<String> got = run("2", () -> repo.findByCustomerIdAndBalanceGreaterThanOrderByBalanceDesc(7, new BigDecimal("15000")))
        .stream().map(Account::getNumber).toList();
    assertThat(got).containsExactly("ACC-1");
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: countByStatus(ACTIVE) is 4, countByStatus(FROZEN) is 0")
  void task3a_count() {
    assertThat(run("3", () -> repo.countByStatus(AccountStatus.ACTIVE))).isEqualTo(4);
    assertThat(run("3", () -> repo.countByStatus(AccountStatus.FROZEN))).isZero();
  }

  @Test
  @DisplayName("Task 3: existsByNumber is true for ACC-4 and false for ACC-9")
  void task3b_exists() {
    assertThat(run("3", () -> repo.existsByNumber("ACC-4"))).isTrue();
    assertThat(run("3", () -> repo.existsByNumber("ACC-9"))).isFalse();
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4a: searchOwner(\"asha\") finds ACC-1, ACC-2, ACC-3 (case-insensitive, ordered)")
  void task4a_query() {
    assertThat(run("4a", () -> repo.searchOwner("asha")).stream().map(Account::getNumber).toList())
        .containsExactly("ACC-1", "ACC-2", "ACC-3");
  }

  @Test
  @DisplayName("Task 4a: searchOwner(\"SHAH\") finds only ACC-4")
  void task4a2_query() {
    assertThat(run("4a", () -> repo.searchOwner("SHAH")).stream().map(Account::getNumber).toList()).containsExactly("ACC-4");
  }

  @Test
  @DisplayName("Task 4b: freezeAllOfCustomer(7) returns 3")
  void task4b_count() {
    assertThat(run("4b", () -> repo.freezeAllOfCustomer(7))).isEqualTo(3);
  }

  @Test
  @DisplayName("Task 4b: after the freeze, findByNumber(\"ACC-1\") says FROZEN and ACC-4 is still ACTIVE")
  void task4c_fresh() {
    run("4b", () -> repo.freezeAllOfCustomer(7));
    var acc1 = run("1", () -> repo.findByNumber("ACC-1")).orElseThrow();
    assertThat(acc1.getStatus()).withFailMessage("[task 4b: stale] ACC-1 still says %s: the bulk update skipped the"
        + " objects already loaded. Use @Modifying(clearAutomatically = true)", acc1.getStatus())
        .isEqualTo(AccountStatus.FROZEN);
    assertThat(run("1", () -> repo.findByNumber("ACC-4")).orElseThrow().getStatus()).isEqualTo(AccountStatus.ACTIVE);
  }

  @Test
  @DisplayName("Task 4b: freezeAllOfCustomer is @Transactional (a write outside a test transaction needs one)")
  void task4d_transactional() throws Exception {
    var m = AccountRepository.class.getMethod("freezeAllOfCustomer", long.class);
    assertThat(m.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
        .withFailMessage("[task 4b: transaction] add @Transactional to freezeAllOfCustomer").isTrue();
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
  @DisplayName("Task 5: the answer covers the key ideas (derived, @query, jpql, @modifying, startup)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"derived|method name", "@query", "jpql", "@modifying", "startup"})
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
