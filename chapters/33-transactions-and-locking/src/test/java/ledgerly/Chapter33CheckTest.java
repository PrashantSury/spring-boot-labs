package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/** Checks for chapter 33. Real transactions (no @Transactional on the test), H2 in memory. */
@SpringBootTest(properties = "spring.main.web-application-type=none")
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter33CheckTest {

  @Autowired AccountRepository accounts;
  @Autowired AuditEntryRepository audit;
  @Autowired WithdrawWorker worker;
  @Autowired WithdrawService service;
  @Autowired Interest interest;

  long id;

  @BeforeEach
  void setUp() {
    audit.deleteAll();
    id = accounts.save(new Account("asha", new BigDecimal("1000.00"))).getId();
  }

  @AfterEach
  void clearHook() {
    worker.setAfterRead(() -> {});
  }

  private BigDecimal balance() {
    return accounts.findById(id).orElseThrow().getBalance();
  }

  private static BigDecimal amt(String s) {
    return new BigDecimal(s);
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: a new account has version 0; each update adds 1")
  void task1a_version() {
    assertThat(accounts.findById(id).orElseThrow().getVersion()).withFailMessage("[task 1: version] no @Version field")
        .isEqualTo(0L);
    interest.credit(id, amt("10"));
    assertThat(accounts.findById(id).orElseThrow().getVersion()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Task 1: saving a stale copy throws ObjectOptimisticLockingFailureException (no lost update)")
  void task1b_stale() {
    Account stale = accounts.findById(id).orElseThrow();
    interest.credit(id, amt("50"));
    stale.setBalance(stale.getBalance().subtract(amt("100")));
    assertThatThrownBy(() -> accounts.save(stale)).withFailMessage("[task 1: version] the stale copy overwrote the"
        + " interest: a lost update").isInstanceOf(ObjectOptimisticLockingFailureException.class);
    assertThat(balance()).isEqualByComparingTo("1050.00");
  }

  @Test
  @DisplayName("Task 1: interest lands between read and write: withdrawOnce fails instead of losing 50")
  void task1c_conflict() {
    worker.setAfterRead(() -> interest.credit(id, amt("50")));
    assertThatThrownBy(() -> worker.withdrawOnce(id, amt("100"))).withFailMessage("[task 1: version] withdrawal"
        + " silently overwrote the interest").isInstanceOf(ObjectOptimisticLockingFailureException.class);
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: 10 parallel locked withdrawals of 100 from 1,000: all succeed, balance 0")
  void task2a_pessimistic() throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(10);
    List<Future<BigDecimal>> fs = new ArrayList<>();
    for (int i = 0; i < 10; i++) fs.add(pool.submit(() -> worker.withdrawLocked(id, amt("100"))));
    for (Future<BigDecimal> f : fs) {
      try {
        f.get();
      } catch (java.util.concurrent.ExecutionException e) {
        fail("[task 2: lock] " + e.getCause());
      }
    }
    pool.shutdown();
    assertThat(balance()).withFailMessage("[task 2: lock] balance %s", balance()).isEqualByComparingTo("0.00");
  }

  @Test
  @DisplayName("Task 2: an 11th locked withdrawal fails with InsufficientFunds")
  void task2b_overdraw() {
    try {
      worker.withdrawLocked(id, amt("1000"));
    } catch (UnsupportedOperationException e) {
      fail("[task 2: lock] " + e.getMessage());
    }
    assertThatThrownBy(() -> worker.withdrawLocked(id, amt("100"))).isInstanceOf(InsufficientFunds.class);
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: one conflict, then success: balance 1000 + 50 - 100 = 950")
  void task3a_retry() {
    AtomicInteger reads = new AtomicInteger();
    worker.setAfterRead(() -> {
      if (reads.incrementAndGet() == 1) interest.credit(id, amt("50"));
    });
    BigDecimal result;
    try {
      result = service.withdrawWithRetry(id, amt("100"));
    } catch (ObjectOptimisticLockingFailureException e) {
      result = fail("[task 3: retry] gave up after the first conflict");
    }
    assertThat(result).isEqualByComparingTo("950.00");
    assertThat(reads.get()).isEqualTo(2);
  }

  @Test
  @DisplayName("Task 3: a conflict EVERY time: gives up after exactly 3 attempts")
  void task3b_giveUp() {
    AtomicInteger reads = new AtomicInteger();
    worker.setAfterRead(() -> {
      reads.incrementAndGet();
      interest.credit(id, amt("1"));
    });
    assertThatThrownBy(() -> service.withdrawWithRetry(id, amt("100")))
        .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    assertThat(reads.get()).withFailMessage("[task 3: retry] %s attempts, expected 3", reads.get()).isEqualTo(3);
  }

  @Test
  @DisplayName("Task 3: InsufficientFunds is NOT retried (1 attempt)")
  void task3c_noRetry() {
    AtomicInteger reads = new AtomicInteger();
    worker.setAfterRead(reads::incrementAndGet);
    assertThatThrownBy(() -> service.withdrawWithRetry(id, amt("5000"))).isInstanceOf(InsufficientFunds.class);
    assertThat(reads.get()).isEqualTo(1);
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: a failed withdrawal rolls back, but its audit entry stays (REQUIRES_NEW)")
  void task4a_requiresNew() {
    assertThatThrownBy(() -> worker.withdrawOnce(id, amt("5000"))).isInstanceOf(InsufficientFunds.class);
    assertThat(balance()).isEqualByComparingTo("1000.00");
    assertThat(audit.findAll()).withFailMessage("[task 4: requires_new] the audit entry was rolled back with the"
        + " withdrawal").extracting(AuditEntry::getMessage).containsExactly("withdraw 5000 from " + id);
  }

  @Test
  @DisplayName("Task 4: a successful withdrawal is audited once")
  void task4b_success() {
    worker.withdrawOnce(id, amt("100"));
    assertThat(audit.count()).isEqualTo(1);
    assertThat(balance()).isEqualByComparingTo("900.00");
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
  @DisplayName("Task 5: the answer covers the key ideas (optimistic, pessimistic, version, retry, requires_new)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"optimistic", "pessimistic", "version", "retry", "requires_new|requires new"})
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
