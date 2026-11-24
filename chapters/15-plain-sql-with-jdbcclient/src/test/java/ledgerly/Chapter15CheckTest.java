package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

/**
 * Checks for chapter 15, against the real H2 database from schema.sql + data.sql.
 * Every test runs in a transaction that is rolled back, so each one starts from the same 3 accounts.
 */
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter15CheckTest {

  @Autowired AccountDao dao;
  @Autowired JdbcClient jdbc;

  private long balanceOf(String number) {
    return jdbc.sql("select balance from account where number = ?").param(number).query(Long.class).single();
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: findAll returns the 3 accounts ordered ACC-1, ACC-2, ACC-3")
  void task1a_all() {
    List<String> numbers = dao.findAll().stream().map(Account::number).toList();
    assertThat(numbers).withFailMessage("[task 1: findAll] expected [ACC-1, ACC-2, ACC-3] but got %s", numbers)
        .containsExactly("ACC-1", "ACC-2", "ACC-3");
  }

  @Test
  @DisplayName("Task 1: the rows are mapped completely (ACC-1 = Asha Rao, 52000, id 1)")
  void task1b_mapped() {
    List<Account> all = dao.findAll();
    assertThat(all).isNotEmpty();
    assertThat(all.get(0)).isEqualTo(new Account(1, "ACC-1", "Asha Rao", 52000));
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: findByNumber(\"ACC-2\") is Vikram Shah with 1000")
  void task2a_found() {
    assertThat(dao.findByNumber("ACC-2")).withFailMessage("[task 2: found] ACC-2 should be found")
        .hasValueSatisfying(a -> assertThat(a.owner() + " " + a.balance()).isEqualTo("Vikram Shah 1000"));
  }

  @Test
  @DisplayName("Task 2: findByNumber(\"ACC-9\") is empty (not an exception)")
  void task2b_missing() {
    assertThat(dao.findByNumber("ACC-9")).isEmpty();
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: searchByOwner(\"Asha\") finds ACC-1 and ACC-3")
  void task3a_like() {
    assertThat(dao.searchByOwner("Asha").stream().map(Account::number).toList()).containsExactly("ACC-1", "ACC-3");
  }

  @Test
  @DisplayName("Task 3: searchByOwner(\"' OR 1=1 --\") finds NOTHING (no SQL injection)")
  void task3b_injection() {
    List<String> got = dao.searchByOwner("' OR 1=1 --").stream().map(Account::number).toList();
    assertThat(got).withFailMessage("[task 3: injection] the input changed the SQL and returned %s. Use a"
        + " named parameter, not string concatenation", got).isEmpty();
  }

  @Test
  @DisplayName("Task 3: searchByOwner(\"O'Brien\") does not crash (a quote is just a character)")
  void task3c_quote() {
    Throwable t = catchThrowable(() -> dao.searchByOwner("O'Brien"));
    assertThat(t).withFailMessage("[task 3: quote] a name with an apostrophe broke the SQL: %s", t).isNull();
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: insert returns the generated id (> 3) and the row can be read back")
  void task4a_insert() {
    long id = dao.insert("ACC-4", "Meera Iyer", 0);
    assertThat(id).withFailMessage("[task 4: id] expected the generated id (> 3) but got %d", id).isGreaterThan(3);
    assertThat(jdbc.sql("select owner from account where id = :id").param("id", id).query(String.class).optional())
        .hasValue("Meera Iyer");
  }

  @Test
  @DisplayName("Task 4: inserting ACC-1 again throws DuplicateKeyException")
  void task4b_duplicate() {
    Throwable t = catchThrowable(() -> dao.insert("ACC-1", "Someone", 10));
    assertThat(t).withFailMessage("[task 4: duplicate] expected DuplicateKeyException but got %s", t)
        .isInstanceOf(DuplicateKeyException.class);
  }

  // ---------- Task 5 ----------

  @Test
  @DisplayName("Task 5: deposit 500 to ACC-2 returns true and the balance becomes 1500")
  void task5a_deposit() {
    assertThat(dao.deposit("ACC-2", 500)).isTrue();
    assertThat(balanceOf("ACC-2")).isEqualTo(1500);
  }

  @Test
  @DisplayName("Task 5: deposit to ACC-9 returns false (0 rows updated)")
  void task5b_depositUnknown() {
    assertThat(dao.deposit("ACC-9", 500)).withFailMessage("[task 5: unknown] no row changed, so return false").isFalse();
  }

  @Test
  @DisplayName("Task 5: withdraw 600 from ACC-2 is true (1000 -> 400); withdraw 2000 is false and leaves 1000")
  void task5c_withdraw() {
    assertThat(dao.withdraw("ACC-2", 2000)).withFailMessage("[task 5: withdraw] 2000 from 1000 must be refused").isFalse();
    assertThat(balanceOf("ACC-2")).isEqualTo(1000);
    assertThat(dao.withdraw("ACC-2", 600)).withFailMessage("[task 5: withdraw] 600 from 1000 must succeed").isTrue();
    assertThat(balanceOf("ACC-2")).isEqualTo(400);
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
  @DisplayName("Task 6: the answer covers the key ideas (parameter, injection, update count, keyholder, optional)")
  void task6c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"parameter|placeholder", "injection", "update count|row count|rows", "keyholder|generated key", "optional"})
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
}
