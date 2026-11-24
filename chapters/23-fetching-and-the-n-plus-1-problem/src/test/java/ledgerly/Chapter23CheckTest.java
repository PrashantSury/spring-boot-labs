package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

/** Checks for chapter 23. 5 customers x 2 accounts. Hibernate statistics count the SQL statements. */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter23CheckTest {

  @Autowired CustomerRepository customers;
  @Autowired AccountRepository accounts;
  @Autowired StatementService service;
  @Autowired TransactionTemplate tx;
  @Autowired EntityManagerFactory emf;

  @BeforeEach
  void reset() {
    accounts.deleteAll();
    customers.deleteAll();
    SampleData.load(customers, accounts);
  }

  private Statistics stats() {
    Statistics s = emf.unwrap(SessionFactory.class).getStatistics();
    s.clear();
    return s;
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: findAllWithAccounts returns 5 customers (no duplicates), each with 2 accounts loaded")
  void task1a_result() {
    List<Customer> all = load();
    assertThat(all).extracting(Customer::getName).containsExactly("Asha Rao", "Meera Iyer", "Rohan Das", "Vikram Shah", "Zoya Khan");
    Throwable t = catchThrowable(() -> all.forEach(c -> c.getAccounts().size()));
    assertThat(t).withFailMessage("[task 1: fetched] the accounts were not loaded by the query: %s", t).isNull();
    assertThat(all).allSatisfy(c -> assertThat(c.getAccounts()).hasSize(2));
  }

  @Test
  @DisplayName("Task 1: ...in exactly ONE select")
  void task1b_oneSelect() {
    Statistics s = stats();
    try {
      customers.findAllWithAccounts().forEach(c -> c.getAccounts().size());
    } catch (RuntimeException e) {
      fail("[task 1] " + e);
    }
    assertThat(s.getPrepareStatementCount()).withFailMessage("[task 1: one select] %d statements", s.getPrepareStatementCount())
        .isEqualTo(1);
  }

  private List<Customer> load() {
    try {
      return customers.findAllWithAccounts();
    } catch (UnsupportedOperationException e) {
      return fail("[task 1] still the stub: " + e.getMessage());
    }
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: accounts over 20000 with owner names = 5 rows, ACC-12 Asha Rao first")
  void task2a_result() {
    List<String> rows = tx.execute(s -> accounts.findByBalanceGreaterThanOrderByNumber(20_000).stream()
        .map(a -> a.getNumber() + " " + a.getCustomer().getName()).toList());
    assertThat(rows).hasSize(5).first().isEqualTo("ACC-12 Asha Rao");
  }

  @Test
  @DisplayName("Task 2: reading those 5 owners costs ONE select, not 1 + 5")
  void task2b_oneSelect() {
    Statistics s = stats();
    tx.executeWithoutResult(st -> accounts.findByBalanceGreaterThanOrderByNumber(20_000)
        .forEach(a -> a.getCustomer().getName()));
    assertThat(s.getPrepareStatementCount()).withFailMessage("[task 2: N+1] %d statements: 1 for the accounts + 1 per"
        + " customer. Add @EntityGraph(attributePaths = \"customer\")", s.getPrepareStatementCount()).isEqualTo(1);
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: findAll() + touching every customer's accounts = 2 selects (batch), not 1 + 5")
  void task3a_batch() {
    Statistics s = stats();
    tx.executeWithoutResult(st -> customers.findAll().forEach(c -> c.getAccounts().size()));
    assertThat(s.getPrepareStatementCount()).withFailMessage("[task 3: batch] %d statements. Add @BatchSize(size = 50)"
        + " to Customer.accounts", s.getPrepareStatementCount()).isEqualTo(2);
  }

  @Test
  @DisplayName("Task 3: Customer.accounts is still LAZY (no EAGER shortcut)")
  void task3b_lazy() throws Exception {
    var ann = Customer.class.getDeclaredField("accounts").getAnnotation(jakarta.persistence.OneToMany.class);
    assertThat(ann.fetch()).withFailMessage("[task 3: lazy] EAGER on a collection loads it everywhere, even when"
        + " nobody needs it").isEqualTo(jakarta.persistence.FetchType.LAZY);
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: ownerSummaries works outside any transaction (no LazyInitializationException)")
  void task4a_noLazyError() {
    Throwable t = catchThrowable(() -> service.ownerSummaries());
    assertThat(t).withFailMessage("[task 4: lazy] %s. Make ownerSummaries @Transactional(readOnly = true)",
        t == null ? null : t.getClass().getSimpleName()).isNull();
  }

  @Test
  @DisplayName("Task 4: ownerSummaries lists 'Asha Rao: 2 accounts' ... sorted")
  void task4b_content() {
    List<String> got;
    try {
      got = service.ownerSummaries();
    } catch (RuntimeException e) {
      got = fail("[task 4] " + e.getClass().getSimpleName());
    }
    assertThat(got).first().isEqualTo("Asha Rao: 2 accounts");
    assertThat(got).hasSize(5);
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
  @DisplayName("Task 5: the answer covers the key ideas (n+1, lazy, join fetch, entitygraph, batch, lazyinitializationexception)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"n+1", "lazy", "join fetch", "entitygraph", "batch", "lazyinitializationexception"})
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
