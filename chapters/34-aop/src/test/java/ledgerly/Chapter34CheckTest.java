package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Checks for chapter 34. Timings and AuditTrail are what your aspects write to. */
@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter34CheckTest {

  @Autowired TransferService transfers;
  @Autowired Timings timings;
  @Autowired AuditTrail trail;

  @BeforeEach
  void reset() {
    transfers.reset();
    timings.clear();
    trail.clear();
  }

  private static BigDecimal amt(String s) {
    return new BigDecimal(s);
  }

  // ---------- Example (green on the stub, stays green) ----------

  @Test
  @DisplayName("Example: feeFor is final, so no aspect can ever time it")
  void task0_finalMethod() {
    assertThat(transfers.feeFor(amt("100"))).isEqualByComparingTo("5");
    assertThat(transfers.feeFor(amt("20000"))).isEqualByComparingTo("25");
    assertThat(timings.count("feeFor")).isZero();
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: the TransferService bean is a CGLIB proxy (an aspect applies to it)")
  void task1a_proxy() {
    assertThat(AopUtils.isCglibProxy(transfers)).withFailMessage("[task 1: aspect] TransferService is a plain %s;"
        + " no aspect applies. Add @Aspect and @Component to TimingAspect", transfers.getClass().getSimpleName()).isTrue();
  }

  @Test
  @DisplayName("Task 1: one transfer is timed once, and its result is unchanged")
  void task1b_timedOnce() {
    String tx = transfers.transfer("ACC-1", "ACC-2", amt("100"));
    assertThat(tx).isEqualTo("TX-1");
    assertThat(transfers.balance("ACC-2")).isEqualByComparingTo("600");
    assertThat(timings.count("transfer")).withFailMessage("[task 1: around] transfer timed %s times, expected 1",
        timings.count("transfer")).isEqualTo(1);
  }

  @Test
  @DisplayName("Task 1: every TransferService method is timed (balance too)")
  void task1c_everyMethod() {
    transfers.balance("ACC-1");
    transfers.balance("ACC-2");
    assertThat(timings.count("balance")).withFailMessage("[task 1: pointcut] balance timed %s times, expected 2",
        timings.count("balance")).isEqualTo(2);
  }

  @Test
  @DisplayName("Task 1: a failing call is timed too, and the caller still gets the exception")
  void task1d_timedWhenThrows() {
    assertThatThrownBy(() -> transfers.transfer("ACC-1", "ACC-2", amt("-5")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(timings.count("transfer")).withFailMessage("[task 1: finally] a failed transfer was not timed;"
        + " record in a finally block").isEqualTo(1);
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: a successful @Audited call writes 'OK transfer [ACC-1, ACC-2, 100] -> TX-1'")
  void task2a_okLine() {
    transfers.transfer("ACC-1", "ACC-2", amt("100"));
    assertThat(trail.lines()).withFailMessage("[task 2: after returning] trail is %s", trail.lines())
        .containsExactly("OK transfer [ACC-1, ACC-2, 100] -> TX-1");
  }

  @Test
  @DisplayName("Task 2: only @Audited methods are audited (balance is not)")
  void task2b_onlyAnnotated() {
    transfers.balance("ACC-1");
    transfers.transfer("ACC-2", "ACC-3", amt("50"));
    transfers.balance("ACC-3");
    assertThat(trail.lines()).withFailMessage("[task 2: pointcut] trail is %s; use @annotation(audited)", trail.lines())
        .containsExactly("OK transfer [ACC-2, ACC-3, 50] -> TX-1");
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: a failed @Audited call writes 'FAILED transfer [ACC-1, ACC-2, 5000]: insufficient funds in ACC-1'")
  void task3a_failedLine() {
    try {
      transfers.transfer("ACC-1", "ACC-2", amt("5000"));
    } catch (InsufficientFunds expected) {
      // the line is what we check
    }
    assertThat(trail.lines()).withFailMessage("[task 3: after throwing] trail is %s", trail.lines())
        .containsExactly("FAILED transfer [ACC-1, ACC-2, 5000]: insufficient funds in ACC-1");
  }

  @Test
  @DisplayName("Task 3: the exception still reaches the caller and balances don't move")
  void task3b_notSwallowed() {
    assertThatThrownBy(() -> transfers.transfer("ACC-3", "ACC-1", amt("1")))
        .isInstanceOf(InsufficientFunds.class).hasMessage("insufficient funds in ACC-3");
    assertThat(transfers.balance("ACC-1")).isEqualByComparingTo("1000");
    assertThat(trail.lines()).withFailMessage("[task 3: after throwing] no FAILED line: %s", trail.lines())
        .hasSize(1).allMatch(l -> l.startsWith("FAILED transfer"));
  }

  // ---------- Task 4 ----------

  private Map<String, BigDecimal> twoLines() {
    Map<String, BigDecimal> lines = new LinkedHashMap<>();
    lines.put("ACC-2", amt("10"));
    lines.put("ACC-3", amt("20"));
    return lines;
  }

  @Test
  @DisplayName("Task 4: payroll of 2 lines writes 2 audit lines (no self-invocation)")
  void task4a_payrollAudited() {
    assertThat(transfers.payroll("ACC-1", twoLines())).containsExactly("TX-1", "TX-2");
    assertThat(trail.lines()).withFailMessage("[task 4: self-invocation] payroll wrote %s; this.transfer() skips the"
        + " proxy, call it through self.getObject()", trail.lines())
        .containsExactly("OK transfer [ACC-1, ACC-2, 10] -> TX-1", "OK transfer [ACC-1, ACC-3, 20] -> TX-2");
  }

  @Test
  @DisplayName("Task 4: payroll of 2 lines times transfer twice")
  void task4b_payrollTimed() {
    transfers.payroll("ACC-1", twoLines());
    assertThat(timings.count("transfer")).withFailMessage("[task 4: self-invocation] transfer timed %s times, expected 2",
        timings.count("transfer")).isEqualTo(2);
    assertThat(transfers.balance("ACC-1")).isEqualByComparingTo("970");
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
  @DisplayName("Task 5: the answer covers the key ideas (proxy, pointcut, around, self-invocation, final)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"proxy", "pointcut", "around", "self-invocation|self invocation|this.", "final"})
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
