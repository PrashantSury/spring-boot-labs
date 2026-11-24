package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;

/** Checks for chapter 37. Notifier is the slow SMS gateway (300 ms per message). */
@SpringBootTest(properties = "ledgerly.tour=false")
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter37CheckTest {

  @Autowired TransferService transfers;
  @Autowired Notifier notifier;
  @Autowired EventLog eventLog;
  @Autowired ObjectProvider<ScheduledTaskHolder> scheduled;

  @BeforeEach
  void reset() throws InterruptedException {
    Thread.sleep(400); // let any SMS from the previous test land first
    notifier.clear();
    eventLog.clear();
  }

  private static BigDecimal amt(String s) {
    return new BigDecimal(s);
  }

  private void awaitSms(int n) {
    try {
      await().atMost(Duration.ofSeconds(2)).until(() -> notifier.sent().size() >= n);
    } catch (Exception e) {
      // the assertion below explains
    }
  }

  private static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  // ---------- Example (green on the stub, stays green) ----------

  @Test
  @DisplayName("Example: a transfer of 100 is saved; one of 250000 is rejected and rolled back")
  void task0_transfers() {
    int before = transfers.count();
    transfers.transfer("ACC-1", "ACC-2", amt("100"));
    assertThatThrownBy(() -> transfers.transfer("ACC-1", "ACC-3", amt("250000"))).isInstanceOf(LedgerRejected.class);
    assertThat(transfers.count()).isEqualTo(before + 1);
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: a transfer publishes ONE TransferCompleted with its id, accounts and amount")
  void task1a_published() {
    long id = transfers.transfer("ACC-1", "ACC-2", amt("100"));
    assertThat(eventLog.events()).withFailMessage("[task 1: publish] events seen: %s", eventLog.events())
        .containsExactly(new TransferCompleted(id, "ACC-1", "ACC-2", amt("100")));
  }

  @Test
  @DisplayName("Task 1: the customer gets 'ACC-2 received 100 from ACC-1'")
  void task1b_smsSent() {
    transfers.transfer("ACC-1", "ACC-2", amt("100"));
    awaitSms(1);
    assertThat(notifier.sent()).withFailMessage("[task 1: publish] SMS sent: %s", notifier.sent())
        .extracting(Notifier.Sms::text).containsExactly("ACC-2 received 100 from ACC-1");
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: one good + one rejected transfer -> exactly ONE SMS (none for the rolled-back one)")
  void task2a_onlyCommitted() {
    transfers.transfer("ACC-1", "ACC-2", amt("100"));
    assertThatThrownBy(() -> transfers.transfer("ACC-1", "ACC-3", amt("250000"))).isInstanceOf(LedgerRejected.class);
    awaitSms(1);
    sleep(700);
    assertThat(notifier.sent()).withFailMessage("[task 2: after commit] SMS sent: %s", notifier.sent())
        .extracting(Notifier.Sms::text).containsExactly("ACC-2 received 100 from ACC-1");
  }

  @Test
  @DisplayName("Task 2: a rolled-back transfer DID publish its event, but no SMS goes out")
  void task2b_publishedButNotSent() {
    assertThatThrownBy(() -> transfers.transfer("ACC-1", "ACC-3", amt("250000"))).isInstanceOf(LedgerRejected.class);
    sleep(700);
    assertThat(eventLog.events()).withFailMessage("[task 1: publish] the event was never published").hasSize(1);
    assertThat(notifier.sent()).withFailMessage("[task 2: after commit] SMS for money that never moved: %s",
        notifier.sent()).isEmpty();
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: transfer() returns in under 250 ms although the SMS takes 300 ms")
  void task3a_doesNotWait() {
    transfers.transfer("ACC-1", "ACC-2", amt("1")); // warm-up
    awaitSms(1);
    long t = System.nanoTime();
    transfers.transfer("ACC-1", "ACC-2", amt("100"));
    long ms = (System.nanoTime() - t) / 1_000_000;
    awaitSms(2);
    assertThat(notifier.sent()).withFailMessage("[task 1/2] no SMS was sent").hasSize(2);
    assertThat(ms).withFailMessage("[task 3: async] transfer took %s ms; the caller waited for the SMS", ms)
        .isLessThan(250);
  }

  @Test
  @DisplayName("Task 3: the SMS is sent on a thread named notify-...")
  void task3b_threadName() {
    transfers.transfer("ACC-1", "ACC-2", amt("100"));
    awaitSms(1);
    List<String> threads = notifier.sent().stream().map(Notifier.Sms::thread).toList();
    assertThat(threads).withFailMessage("[task 3: thread name] SMS threads: %s", threads).hasSize(1)
        .allMatch(n -> n.startsWith("notify-"));
  }

  // ---------- Task 4 ----------

  private List<ScheduledTask> tasks() {
    ScheduledTaskHolder h = scheduled.getIfAvailable();
    return h == null ? List.of() : List.copyOf(h.getScheduledTasks());
  }

  @Test
  @DisplayName("Task 4: postInterest runs on cron '0 0 1 * * *'")
  void task4a_cron() {
    var crons = tasks().stream().filter(t -> t.getTask() instanceof CronTask)
        .filter(t -> t.getTask().getRunnable().toString().contains("postInterest"))
        .map(t -> ((CronTask) t.getTask()).getExpression()).toList();
    assertThat(crons).withFailMessage("[task 4: cron] scheduled: %s; add @EnableScheduling and @Scheduled(cron = ...)",
        tasks()).containsExactly("0 0 1 * * *");
  }

  @Test
  @DisplayName("Task 4: sweepOutbox runs with fixedDelay 30 s (not fixedRate)")
  void task4b_fixedDelay() {
    var delays = tasks().stream().filter(t -> t.getTask() instanceof FixedDelayTask)
        .filter(t -> t.getTask().getRunnable().toString().contains("sweepOutbox"))
        .map(t -> ((FixedDelayTask) t.getTask()).getIntervalDuration()).toList();
    assertThat(delays).withFailMessage("[task 4: fixedDelay] scheduled: %s", tasks())
        .containsExactly(Duration.ofSeconds(30));
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
  @DisplayName("Task 5: the answer covers the key ideas (after_commit, async, thread, schedul, instance)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"after_commit|after commit", "async", "thread", "schedul", "instance|shedlock"})
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
