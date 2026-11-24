package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Checks for chapter 21. NOT @Transactional on purpose: each call commits or rolls back for real.
 * Every test starts with ACC-1 = 10000, ACC-2 = 500 and no customers.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter21CheckTest {

  @Autowired AccountRepository accounts;
  @Autowired CustomerRepository customers;
  @Autowired PayeeRepository payees;
  @Autowired TransferService service;
  @Autowired TransactionTemplate tx;

  @BeforeEach
  void reset() {
    payees.deleteAll();
    customers.deleteAll();
    accounts.deleteAll();
    accounts.saveAll(List.of(new Account("ACC-1", 10_000), new Account("ACC-2", 500)));
  }

  private long balance(String n) {
    return accounts.findByNumber(n).orElseThrow().getBalance();
  }

  private Long saveAshaWithTwoPayees() {
    Customer asha = new Customer("Asha Rao");
    asha.addPayee(new Payee("Mom", "ACC-2"));
    asha.addPayee(new Payee("Landlord", "ACC-7"));
    Throwable t = catchThrowable(() -> customers.save(asha));
    assertThat(t).withFailMessage("[task 1: cascade] saving the customer failed: %s", t).isNull();
    return asha.getId();
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: saving a customer with 2 new payees saves the payees too (cascade)")
  void task1a_cascadePersist() {
    saveAshaWithTwoPayees();
    assertThat(payees.count()).withFailMessage("[task 1: cascade] %d payees in the table, expected 2", payees.count())
        .isEqualTo(2);
  }

  @Test
  @DisplayName("Task 1: removing 'Landlord' from the list and saving deletes that row (orphanRemoval)")
  void task1b_orphan() {
    Long id = saveAshaWithTwoPayees();
    tx.executeWithoutResult(s -> customers.findById(id).orElseThrow().removePayee("Landlord"));
    assertThat(payees.count()).withFailMessage("[task 1: orphanRemoval] %d payees left, expected 1", payees.count())
        .isEqualTo(1);
  }

  @Test
  @DisplayName("Task 1: deleting the customer deletes its payees")
  void task1c_cascadeRemove() {
    Long id = saveAshaWithTwoPayees();
    assertThat(payees.count()).withFailMessage("[task 1: remove] the payees were never saved (cascade)").isEqualTo(2);
    Throwable t = catchThrowable(() -> customers.deleteById(id));
    assertThat(t).withFailMessage("[task 1: remove] deleting the customer failed: %s", t).isNull();
    assertThat(payees.count()).isZero();
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: a good transfer of 3000 moves the money (7000 / 3500)")
  void task2a_ok() {
    service.transfer("ACC-1", "ACC-2", 3000);
    assertThat(balance("ACC-1") + "/" + balance("ACC-2")).isEqualTo("7000/3500");
  }

  @Test
  @DisplayName("Task 2: a transfer to ACC-9 fails AND ACC-1 still has 10000 (rolled back)")
  void task2b_rollback() {
    assertThat(catchThrowable(() -> service.transfer("ACC-1", "ACC-9", 3000))).isInstanceOf(AccountNotFoundException.class);
    assertThat(balance("ACC-1")).withFailMessage("[task 2: rollback] ACC-1 has %d: the debit was committed without"
        + " the credit. Make transfer @Transactional", balance("ACC-1")).isEqualTo(10_000);
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: transferWithLimit 6000 throws DailyLimitExceededException AND ACC-1 still has 10000")
  void task3a_checked() {
    assertThat(catchThrowable(() -> service.transferWithLimit("ACC-1", "ACC-2", 6000)))
        .isInstanceOf(DailyLimitExceededException.class);
    assertThat(balance("ACC-1")).withFailMessage("[task 3: checked] ACC-1 has %d: a checked exception commits by"
        + " default. Use rollbackFor", balance("ACC-1")).isEqualTo(10_000);
  }

  @Test
  @DisplayName("Task 3: transferWithLimit 4000 works (6000 / 4500)")
  void task3b_ok() throws Exception {
    service.transferWithLimit("ACC-1", "ACC-2", 4000);
    assertThat(balance("ACC-1") + "/" + balance("ACC-2")).isEqualTo("6000/4500");
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: batch [1000 to ACC-2, 2000 to ACC-9, 500 to ACC-2]: only the middle one fails")
  void task4a_failedList() {
    List<Payment> failed = service.payAll(List.of(
        new Payment("ACC-1", "ACC-2", 1000), new Payment("ACC-1", "ACC-9", 2000), new Payment("ACC-1", "ACC-2", 500)));
    assertThat(failed).containsExactly(new Payment("ACC-1", "ACC-9", 2000));
  }

  @Test
  @DisplayName("Task 4: after that batch ACC-1 = 8500 and ACC-2 = 2000 (the bad payment fully undone, the others kept)")
  void task4b_isolated() {
    service.payAll(List.of(
        new Payment("ACC-1", "ACC-2", 1000), new Payment("ACC-1", "ACC-9", 2000), new Payment("ACC-1", "ACC-2", 500)));
    assertThat(balance("ACC-1")).withFailMessage("[task 4: self-invocation] ACC-1 has %d, expected 8500. this.payOne()"
        + " skips the proxy, so its @Transactional does nothing. Call worker.pay(p)", balance("ACC-1")).isEqualTo(8500);
    assertThat(balance("ACC-2")).isEqualTo(2000);
  }

  @Test
  @DisplayName("Task 4: PaymentWorker.pay is @Transactional")
  void task4c_worker() throws Exception {
    var m = PaymentWorker.class.getMethod("pay", Payment.class);
    assertThat(m.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
        .withFailMessage("[task 4: worker] add a @Transactional pay(Payment) to PaymentWorker").isTrue();
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
  @DisplayName("Task 5: the answer covers the key ideas (@transactional, rollback, checked, rollbackfor, proxy, cascade)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"@transactional", "rollback", "checked", "rollbackfor", "proxy|self-invocation", "cascade"})
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
