package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.jdbc.core.simple.JdbcClient;
import tools.jackson.databind.json.JsonMapper;

/** Checks for chapter 20. Asha Rao owns ACC-1 (52000) and ACC-2 (1000). */
@DataJpaTest
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter20CheckTest {

  @Autowired TestEntityManager tem;
  @Autowired JdbcClient jdbc;

  /** Saves Asha with two accounts, setting the owning side directly (task 1 only). */
  private Customer saveAsha() {
    Customer asha = tem.persist(new Customer("Asha Rao"));
    Account a1 = new Account("ACC-1", 52_000);
    Account a2 = new Account("ACC-2", 1_000);
    a1.setCustomer(asha);
    a2.setCustomer(asha);
    tem.persist(a1);
    tem.persist(a2);
    tem.flush();
    tem.clear();
    return asha;
  }

  private List<String> columns(String table) {
    return jdbc.sql("select lower(column_name) from information_schema.columns where lower(table_name) = ?")
        .param(table).query(String.class).list();
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: the account table has a customer_id column")
  void task1a_column() {
    saveAsha();
    assertThat(columns("account")).withFailMessage("[task 1: column] account columns are %s; expected customer_id"
        + " (@ManyToOne + @JoinColumn(name = \"customer_id\"))", columns("account")).contains("customer_id");
  }

  @Test
  @DisplayName("Task 1: ACC-2 loaded on its own knows its owner: Asha Rao")
  void task1b_owner() {
    saveAsha();
    Account acc = tem.getEntityManager().createQuery("select a from Account a where a.number = 'ACC-2'", Account.class)
        .getSingleResult();
    assertThat(acc.getCustomer()).withFailMessage("[task 1: owner] ACC-2's customer is null after reload").isNotNull();
    assertThat(acc.getCustomer().getName()).isEqualTo("Asha Rao");
  }

  @Test
  @DisplayName("Task 1: an account without a customer is rejected (optional = false)")
  void task1c_required() {
    Throwable t = catchThrowable(() -> tem.persistAndFlush(new Account("ACC-9", 5)));
    assertThat(t).withFailMessage("[task 1: optional] an account with no customer was saved").isNotNull();
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: Asha reloaded has 2 accounts: ACC-1 and ACC-2")
  void task2a_inverse() {
    Customer asha = saveAsha();
    Customer back = tem.find(Customer.class, asha.getId());
    assertThat(back.getAccounts().stream().map(Account::getNumber).sorted().toList())
        .withFailMessage("[task 2: inverse] the reloaded customer has accounts %s",
            back.getAccounts().stream().map(Account::getNumber).toList())
        .containsExactly("ACC-1", "ACC-2");
  }

  @Test
  @DisplayName("Task 2: there is NO extra join table (customer_accounts): mappedBy reuses customer_id")
  void task2b_noJoinTable() {
    saveAsha();
    List<String> tables = jdbc.sql("select lower(table_name) from information_schema.tables where table_schema = 'PUBLIC'")
        .query(String.class).list();
    assertThat(tables).withFailMessage("[task 2: join table] tables are %s. Use mappedBy = \"customer\"", tables)
        .containsExactlyInAnyOrder("customer", "account");
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: addAccount sets both sides in memory")
  void task3a_bothSides() {
    Customer c = new Customer("Vikram Shah");
    Account a = new Account("ACC-3", 700);
    c.addAccount(a);
    assertThat(c.getAccounts()).containsExactly(a);
    assertThat(a.getCustomer()).withFailMessage("[task 3: both sides] addAccount did not set account.customer").isSameAs(c);
  }

  @Test
  @DisplayName("Task 3: after addAccount + persist, the row's customer_id points at Vikram")
  void task3b_saved() {
    Customer c = tem.persist(new Customer("Vikram Shah"));
    Account a = new Account("ACC-3", 700);
    c.addAccount(a);
    Throwable t = catchThrowable(() -> tem.persistAndFlush(a));
    assertThat(t).withFailMessage("[task 3: saved] saving the account failed: %s. JPA saves the OWNING side", t).isNull();
    Long fk = jdbc.sql("select customer_id from account where number = 'ACC-3'").query(Long.class).single();
    assertThat(fk).isEqualTo(c.getId());
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: a customer with accounts serialises to JSON without an endless loop")
  void task4a_json() {
    Customer c = new Customer("Asha Rao");
    Account a = new Account("ACC-1", 52_000);
    c.getAccounts().add(a);
    a.setCustomer(c);
    Throwable t = catchThrowable(() -> JsonMapper.builder().build().writeValueAsString(c));
    assertThat(t).withFailMessage("[task 4: loop] serialising failed with %s. Put @JsonIgnore on Account.customer",
        t == null ? null : t.getClass().getSimpleName()).isNull();
  }

  @Test
  @DisplayName("Task 4: the JSON has the accounts once and no 'customer' inside them")
  void task4b_shape() throws Exception {
    Customer c = new Customer("Asha Rao");
    Account a = new Account("ACC-1", 52_000);
    c.getAccounts().add(a);
    a.setCustomer(c);
    String json;
    try {
      json = JsonMapper.builder().build().writeValueAsString(c);
    } catch (Exception e) {
      json = "failed: " + e.getClass().getSimpleName();
    }
    assertThat(json).withFailMessage("[task 4: shape] got %s", json)
        .contains("\"number\":\"ACC-1\"").doesNotContain("\"customer\"");
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
  @DisplayName("Task 5: the answer covers the key ideas (owning, mappedby, join table, lazy, @jsonignore)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"owning|owner", "mappedby", "join table", "lazy", "@jsonignore|loop|recursion"})
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
