package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.ledgerly.fx.FixedFxRates;
import dev.ledgerly.fx.FxAutoConfiguration;
import dev.ledgerly.fx.FxRates;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.transaction.support.TransactionTemplate;

/** Checks for chapter 40: four small proofs of the Spring core answers you give in interviews. */
@SpringBootTest(properties = "ledgerly.tour=false")
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter40CheckTest {

  @Autowired StatementPrinter printer;
  @Autowired TransferService transfers;
  @Autowired AuditWriter auditWriter;
  @Autowired ObjectProvider<LimitsProperties> limits;
  @Autowired TransactionTemplate tx;

  @BeforeEach
  void clear() {
    transfers.clear();
  }

  // ---------- Example (green on the stub, stays green) ----------

  @Test
  @DisplayName("Example: a transfer of 100 is saved (and the transaction commits)")
  void task0_transferSaved() {
    transfers.transfer("ACC-1", "ACC-2", new BigDecimal("100"));
    assertThat(transfers.count("transfers")).isEqualTo(1);
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: the second statement contains ONLY its own lines")
  void task1a_freshBuffer() {
    printer.print("ACC-1", List.of("rent 15000", "salary 90000"));
    String second = printer.print("ACC-2", List.of("fd interest 1200"));
    assertThat(second).withFailMessage("[task 1: prototype in singleton] second statement: %s", second)
        .endsWith(": fd interest 1200");
  }

  @Test
  @DisplayName("Task 1: every statement uses a different StatementBuffer")
  void task1b_differentBuffers() {
    String a = printer.print("ACC-1", List.of("x"));
    String b = printer.print("ACC-1", List.of("y"));
    String na = a.substring(a.indexOf('#'), a.indexOf(':'));
    String nb = b.substring(b.indexOf('#'), b.indexOf(':'));
    assertThat(nb).withFailMessage("[task 1: ObjectProvider] both statements used buffer %s", na).isNotEqualTo(na);
  }

  // ---------- Task 2 ----------

  private final ApplicationContextRunner runner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(FxAutoConfiguration.class));

  @Test
  @DisplayName("Task 2: with no FxRates of its own, the app gets the library's FixedFxRates")
  void task2a_default() {
    runner.run(ctx -> assertThat(ctx).withFailMessage("[task 2: @Bean] no FxRates bean").hasSingleBean(FxRates.class)
        .getBean(FxRates.class).isInstanceOf(FixedFxRates.class));
  }

  @Test
  @DisplayName("Task 2: when the app defines its own FxRates, the default backs off")
  void task2b_backsOff() {
    runner.withBean("liveRates", FxRates.class, () -> currency -> new BigDecimal("84.00")).run(ctx -> {
      assertThat(ctx).withFailMessage("[task 2: @ConditionalOnMissingBean] FxRates beans: %s",
          List.of(ctx.getBeanNamesForType(FxRates.class))).hasSingleBean(FxRates.class);
      assertThat(ctx.getBean(FxRates.class).rupeesPer("USD")).isEqualByComparingTo("84.00");
    });
  }

  @Test
  @DisplayName("Task 2: FxAutoConfiguration is listed in META-INF/spring/...AutoConfiguration.imports")
  void task2c_registered() {
    List<String> names = ImportCandidates.load(AutoConfiguration.class, getClass().getClassLoader()).getCandidates();
    assertThat(names).withFailMessage("[task 2: imports file] dev.ledgerly.fx.FxAutoConfiguration is not listed")
        .contains("dev.ledgerly.fx.FxAutoConfiguration");
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: default profile -> perTransfer 50000 (default value), transfersPerDay 20 (from properties)")
  void task3a_defaults() {
    LimitsProperties l = limits.getIfAvailable();
    assertThat(l).withFailMessage("[task 3: @ConfigurationProperties] no LimitsProperties bean").isNotNull();
    assertThat(l.perTransfer()).isEqualByComparingTo("50000");
    assertThat(l.transfersPerDay()).isEqualTo(20);
  }

  @Test
  @DisplayName("Task 3: profile prod -> perTransfer 200000, transfersPerDay still 20")
  void task3b_prodProfile() {
    try (var ctx = new SpringApplicationBuilder(LedgerlyApplication.class).web(WebApplicationType.NONE)
        .profiles("prod").properties("ledgerly.tour=false", "spring.datasource.url=jdbc:h2:mem:prodcheck")
        .run()) {
      LimitsProperties l = ctx.getBeanProvider(LimitsProperties.class).getIfAvailable();
      assertThat(l).withFailMessage("[task 3: @ConfigurationProperties] no LimitsProperties bean").isNotNull();
      assertThat(l.perTransfer()).withFailMessage("[task 3: application-prod.properties] perTransfer %s",
          l.perTransfer()).isEqualByComparingTo("200000");
      assertThat(l.transfersPerDay()).isEqualTo(20);
    }
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: a transfer of 75000 rolls back, but its ATTEMPT audit line survives")
  void task4a_auditSurvives() {
    assertThatThrownBy(() -> transfers.transfer("ACC-1", "ACC-2", new BigDecimal("75000")))
        .isInstanceOf(IllegalStateException.class);
    assertThat(transfers.count("transfers")).isZero();
    assertThat(transfers.count("audit")).withFailMessage("[task 4: REQUIRES_NEW] the audit line was rolled back"
        + " with the transfer").isEqualTo(1);
  }

  @Test
  @DisplayName("Task 4: AuditWriter.write commits on its own even when the caller's transaction rolls back")
  void task4b_ownTransaction() {
    tx.executeWithoutResult(status -> {
      auditWriter.write("inside a transaction that will roll back");
      status.setRollbackOnly();
    });
    assertThat(transfers.count("audit")).withFailMessage("[task 4: REQUIRES_NEW] write() joined the caller's"
        + " transaction").isEqualTo(1);
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
  @DisplayName("Task 5: the answer covers the key ideas (prototype, conditional, profile, proxy, requires_new, self-invocation)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"prototype|scope", "conditional", "profile", "proxy", "requires_new|propagation", "self-invocation|this."})
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
