package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Checks for chapter 16. Each test persists real rows in H2 and rolls back at the end. */
@DataJpaTest
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter16CheckTest {

  @Autowired TestEntityManager tem;
  @Autowired JdbcClient jdbc;

  private static Account asha() {
    return new Account("ACC-1", "Asha Rao", AccountType.SAVINGS, new BigDecimal("52000.00"));
  }

  private Account save(Account a) {
    try {
      return tem.persistFlushFind(a);
    } catch (IllegalArgumentException e) {
      return fail("[task 1: entity] JPA does not know Account: " + e.getMessage() + ". Add @Entity and @Id");
    }
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: Account is an @Entity with a protected no-arg constructor")
  void task1a_entity() throws Exception {
    assertThat(Account.class.isAnnotationPresent(Entity.class)).withFailMessage("[task 1: @Entity] missing").isTrue();
    var ctor = Account.class.getDeclaredConstructor();
    assertThat(java.lang.reflect.Modifier.isProtected(ctor.getModifiers()))
        .withFailMessage("[task 1: constructor] the no-arg constructor should be protected").isTrue();
  }

  @Test
  @DisplayName("Task 1: persist gives the account a database-generated id, and it reads back from table 'accounts'")
  void task1b_roundTrip() {
    Account a = asha();
    assertThat(a.getId()).isNull();
    Account back = save(a);
    assertThat(a.getId()).withFailMessage("[task 1: id] persist() should fill the id (@GeneratedValue)").isNotNull();
    assertThat(back.getNumber() + " " + back.getOwner()).isEqualTo("ACC-1 Asha Rao");
    assertThat(jdbc.sql("select count(*) from accounts").query(Long.class).single()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Task 1: a second ACC-1 is rejected by the unique constraint; a null owner by NOT NULL")
  void task1c_constraints() {
    save(asha());
    Throwable dup = catchThrowable(() -> tem.persistAndFlush(asha()));
    assertThat(dup).withFailMessage("[task 1: unique] a duplicate number was accepted").isNotNull();
    tem.clear();
    Throwable noOwner = catchThrowable(() ->
        tem.persistAndFlush(new Account("ACC-2", null, AccountType.CURRENT, BigDecimal.ONE)));
    assertThat(noOwner).withFailMessage("[task 1: not null] an account without owner was accepted").isNotNull();
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: the type column holds the text 'SAVINGS', not the number 0")
  void task2a_enumString() {
    save(asha());
    String stored = jdbc.sql("select cast(type as varchar) from accounts").query(String.class).single();
    assertThat(stored).withFailMessage("[task 2: enum] the column holds '%s'. Use @Enumerated(EnumType.STRING)", stored)
        .isEqualTo("SAVINGS");
  }

  @Test
  @DisplayName("Task 2: a CURRENT account reads back as CURRENT")
  void task2b_enumBack() {
    Account back = save(new Account("ACC-2", "Vikram Shah", AccountType.CURRENT, BigDecimal.TEN));
    assertThat(back.getType()).isEqualTo(AccountType.CURRENT);
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: the balance column is DECIMAL/NUMERIC(19, 2) and NOT NULL")
  void task3a_column() {
    save(asha());
    var col = jdbc.sql("select numeric_precision, numeric_scale, is_nullable from information_schema.columns"
        + " where lower(table_name) = 'accounts' and lower(column_name) = 'balance'").query().singleRow();
    assertThat(col.get("NUMERIC_PRECISION") + "," + col.get("NUMERIC_SCALE") + "," + col.get("IS_NULLABLE"))
        .withFailMessage("[task 3: column] precision,scale,nullable = %s,%s,%s; expected 19,2,NO",
            col.get("NUMERIC_PRECISION"), col.get("NUMERIC_SCALE"), col.get("IS_NULLABLE"))
        .isEqualTo("19,2,NO");
  }

  @Test
  @DisplayName("Task 3: 52000.00 + 0.10 + 0.20 is exactly 52000.30 after a round trip")
  void task3b_exact() {
    Account a = asha();
    a.deposit(new BigDecimal("0.10"));
    a.deposit(new BigDecimal("0.20"));
    Account back = save(a);
    assertThat(back.getBalance()).isEqualByComparingTo("52000.30");
    assertThat(back.getBalance().scale()).isEqualTo(2);
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: the same row loaded twice (after clear) gives two objects that are equal")
  void task4a_equalAfterReload() {
    Account first = save(asha());
    tem.clear();
    Account second = tem.find(Account.class, first.getId());
    assertThat(second).isNotSameAs(first);
    assertThat(second).withFailMessage("[task 4: equals] two copies of the same row should be equal (compare ids)")
        .isEqualTo(first);
    assertThat(second.hashCode()).isEqualTo(first.hashCode());
  }

  @Test
  @DisplayName("Task 4: two different unsaved accounts are NOT equal (null ids never match)")
  void task4b_unsaved() {
    Account a = asha();
    Account b = new Account("ACC-2", "Vikram Shah", AccountType.CURRENT, BigDecimal.TEN);
    assertThat(a).isNotEqualTo(b);
    assertThat(a).isEqualTo(a);
  }

  @Test
  @DisplayName("Task 4: an account put in a HashSet BEFORE persist is still found AFTER persist")
  void task4c_hashSet() {
    Account a = asha();
    Set<Account> set = new HashSet<>(List.of(a));
    tem.persistAndFlush(a);
    assertThat(set.contains(a)).withFailMessage("[task 4: hashCode] the set lost the account once the id was set."
        + " hashCode must not depend on the id").isTrue();
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
  @DisplayName("Task 5: the answer covers the key ideas (@entity, no-arg, enumtype.string, bigdecimal, hashcode)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"@entity", "no-arg|constructor", "enumtype.string|string", "bigdecimal", "hashcode"})
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
