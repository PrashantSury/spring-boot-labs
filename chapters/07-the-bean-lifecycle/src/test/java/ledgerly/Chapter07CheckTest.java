package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Checks for chapter 7. Each test starts a fresh context from package
 * ledgerly, often closes it, and reads LifecycleLog to see what Spring called.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter07CheckTest {

  @BeforeEach
  void clearLog() {
    LifecycleLog.clear();
  }

  // ---------- Task 1: @PostConstruct ----------

  @Test
  @DisplayName("Task 1: RateCache has today's USD rate as soon as the context is ready")
  void task1a_loaded() {
    try (var ctx = context()) {
      Double usd = ctx.getBean(RateCache.class).rate("USD");
      assertThat(usd).withFailMessage("[task 1: @PostConstruct] rate(USD) is %s. Nobody called load():"
          + " mark it so Spring calls it after injection", usd).isEqualTo(83.25);
    }
  }

  @Test
  @DisplayName("Task 1: load() runs exactly once")
  void task1b_once() {
    try (var ctx = context()) {
      int loads = ctx.getBean(RateCache.class).loads();
      assertThat(loads).withFailMessage("[task 1: once] load() ran %d times, expected 1", loads).isEqualTo(1);
    }
  }

  @Test
  @DisplayName("Task 1: an unknown currency is null (no made-up rates)")
  void task1c_unknown() {
    try (var ctx = context()) {
      assertThat(ctx.getBean(RateCache.class).rate("JPY")).isNull();
      assertThat(ctx.getBean(RateCache.class).rate("GBP")).isEqualTo(105.40);
    }
  }

  // ---------- Task 2: @PreDestroy ----------

  @Test
  @DisplayName("Task 2: closing the context closes the 2 open sessions")
  void task2a_closedOnShutdown() {
    var ctx = context();
    ctx.getBean(SessionRegistry.class).open("ACC-1");
    ctx.getBean(SessionRegistry.class).open("ACC-2");
    ctx.close();
    assertThat(LifecycleLog.lines()).withFailMessage("[task 2: @PreDestroy] after close() expected the log line"
        + " 'sessions closed: 2' but the log is %s", LifecycleLog.lines()).contains("sessions closed: 2");
  }

  @Test
  @DisplayName("Task 2: closeAll() does NOT run while the application is still running")
  void task2b_notEarly() {
    try (var ctx = context()) {
      ctx.getBean(SessionRegistry.class).open("ACC-1");
      assertThat(LifecycleLog.lines()).noneMatch(l -> l.startsWith("sessions closed"));
      assertThat(ctx.getBean(SessionRegistry.class).openCount()).isEqualTo(1);
    }
  }

  @Test
  @DisplayName("Task 2: closeAll() runs once even with no open sessions")
  void task2c_onceWhenEmpty() {
    context().close();
    long n = LifecycleLog.lines().stream().filter(l -> l.equals("sessions closed: 0")).count();
    assertThat(n).withFailMessage("[task 2: once] expected 'sessions closed: 0' exactly once, log is %s", LifecycleLog.lines())
        .isEqualTo(1);
  }

  // ---------- Task 3: @Bean(initMethod, destroyMethod) ----------

  @Test
  @DisplayName("Task 3: the payment gateway is connected when the context is ready")
  void task3a_connected() {
    try (var ctx = context()) {
      assertThat(ctx.getBean(PaymentGatewayClient.class).isConnected())
          .withFailMessage("[task 3: initMethod] the gateway is not connected. Name connect as the @Bean's initMethod")
          .isTrue();
    }
  }

  @Test
  @DisplayName("Task 3: closing the context disconnects the gateway")
  void task3b_disconnected() {
    var ctx = context();
    PaymentGatewayClient gw = ctx.getBean(PaymentGatewayClient.class);
    ctx.close();
    assertThat(gw.isConnected()).withFailMessage("[task 3: destroyMethod] still connected after close(). Name disconnect"
        + " as the @Bean's destroyMethod").isFalse();
    assertThat(LifecycleLog.lines()).contains("gateway disconnected");
  }

  // ---------- Task 4: BeanPostProcessor + proxy ----------

  @Test
  @DisplayName("Task 4: the @Metered StatementPrinter you get from the context is a proxy")
  void task4a_proxy() {
    try (var ctx = context()) {
      Object printer = ctx.getBean(StatementPrinter.class);
      assertThat(AopUtils.isAopProxy(printer)).withFailMessage("[task 4: proxy] StatementPrinter is a plain %s."
          + " Return a ProxyFactory proxy from postProcessAfterInitialization", printer.getClass().getSimpleName()).isTrue();
    }
  }

  @Test
  @DisplayName("Task 4: a direct call to line() is counted once and still returns the real result")
  void task4b_counted() {
    try (var ctx = context()) {
      String r = ctx.getBean(StatementPrinter.class).line("ACC-1", 100);
      assertThat(r).isEqualTo("ACC-1 100");
      int n = ctx.getBean(CallCounter.class).count("line");
      assertThat(n).withFailMessage("[task 4: counter] expected line=1 but got %d", n).isEqualTo(1);
    }
  }

  @Test
  @DisplayName("Task 4: statement() is counted, but its two inner this.line() calls are NOT (self-invocation)")
  void task4c_selfInvocation() {
    try (var ctx = context()) {
      String r = ctx.getBean(StatementPrinter.class).statement("ACC-1");
      assertThat(r).isEqualTo("ACC-1 500 / ACC-1 -200");
      CallCounter c = ctx.getBean(CallCounter.class);
      assertThat(c.count("statement")).withFailMessage("[task 4: counter] expected statement=1 but got %d",
          c.count("statement")).isEqualTo(1);
      assertThat(c.count("line")).withFailMessage("[task 4: self-invocation] line was counted %d times."
          + " this.line() inside the bean never goes through the proxy, so it should be 0", c.count("line")).isEqualTo(0);
    }
  }

  @Test
  @DisplayName("Task 4: FeeTable (not @Metered) stays a plain object")
  void task4d_onlyMetered() {
    try (var ctx = context()) {
      Object fees = ctx.getBean(FeeTable.class);
      assertThat(AopUtils.isAopProxy(fees)).withFailMessage("[task 4: trap] FeeTable is not @Metered and must not be proxied")
          .isFalse();
      assertThat(ctx.getBean(FeeTable.class).feeFor(5000)).isEqualTo(50);
    }
  }

  // ---------- Task 5: the full order ----------

  @Test
  @DisplayName("Task 5: start-up order is constructor, @PostConstruct, afterPropertiesSet, initMethod")
  void task5a_startOrder() {
    try (var ctx = context()) {
      assertThat(probe()).withFailMessage("[task 5: start order] the probe logged %s", probe())
          .startsWith("probe: constructor", "probe: @PostConstruct", "probe: afterPropertiesSet", "probe: initMethod");
    }
  }

  @Test
  @DisplayName("Task 5: shutdown order is @PreDestroy, destroy, destroyMethod")
  void task5b_stopOrder() {
    context().close();
    assertThat(probe()).withFailMessage("[task 5: full order] the probe logged %s", probe())
        .containsExactly("probe: constructor", "probe: @PostConstruct", "probe: afterPropertiesSet", "probe: initMethod",
            "probe: @PreDestroy", "probe: destroy", "probe: destroyMethod");
  }

  @Test
  @DisplayName("Task 5: every callback runs exactly once")
  void task5c_once() {
    context().close();
    assertThat(probe()).withFailMessage("[task 5: once] the probe logged %s (7 lines expected)", probe())
        .doesNotHaveDuplicates().hasSize(7);
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
  @DisplayName("Task 6: the answer covers the key ideas (@postconstruct, @predestroy, beanpostprocessor, proxy, prototype)")
  void task6c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"@postconstruct", "@predestroy", "beanpostprocessor", "proxy", "prototype"})
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

  // ---------- helpers ----------

  private static List<String> probe() {
    return LifecycleLog.lines().stream().filter(l -> l.startsWith("probe:")).toList();
  }

  private static AnnotationConfigApplicationContext context() {
    try {
      return new AnnotationConfigApplicationContext("ledgerly");
    } catch (Exception e) {
      Throwable root = e;
      while (root.getCause() != null) {
        root = root.getCause();
      }
      return fail("[context] the application context did not start: " + root.getClass().getSimpleName()
          + ": " + root.getMessage());
    }
  }
}
