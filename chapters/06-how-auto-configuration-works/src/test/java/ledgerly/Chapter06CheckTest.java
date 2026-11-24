package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import ledgerly.app.ConditionsReader;
import ledgerly.app.LedgerlyApplication;
import ledgerly.audit.AuditSink;
import ledgerly.audit.ConsoleAuditSink;
import ledgerly.audit.SiemForwarder;
import ledgerly.autoconfigure.LedgerlyAuditAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Checks for chapter 6. Tasks 2-4 use ApplicationContextRunner, Spring
 * Boot's own tool for testing auto-configurations: it starts a tiny context
 * with just your auto-configuration plus whatever the test adds.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter06CheckTest {

  static final String CONFIG = "ledgerly.autoconfigure.LedgerlyAuditAutoConfiguration";

  private final ApplicationContextRunner runner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(LedgerlyAuditAutoConfiguration.class));

  /** An app that brings its own sink. */
  @Configuration(proxyBeanMethods = false)
  static class OwnSinkConfig {
    @Bean
    AuditSink bankAuditSink() {
      return event -> "bank: " + event;
    }
  }

  // ---------- Task 1: an auto-configuration, found through the imports file ----------

  @Test
  @DisplayName("Task 1: the class is annotated @AutoConfiguration")
  void task1a_annotated() {
    assertThat(LedgerlyAuditAutoConfiguration.class.isAnnotationPresent(AutoConfiguration.class))
        .withFailMessage("[task 1: @AutoConfiguration] LedgerlyAuditAutoConfiguration is not annotated @AutoConfiguration")
        .isTrue();
  }

  @Test
  @DisplayName("Task 1: the AutoConfiguration.imports file lists the class")
  void task1b_importsFile() throws IOException {
    String file = "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(file)) {
      assertThat(in).withFailMessage("[task 1: imports file] %s is missing", file).isNotNull();
      List<String> lines = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines()
          .map(String::trim).filter(l -> !l.isEmpty() && !l.startsWith("#")).toList();
      assertThat(lines).withFailMessage("[task 1: imports file] expected the line %s but the file lists %s", CONFIG, lines)
          .contains(CONFIG);
    }
  }

  @Test
  @DisplayName("Task 1: the real application gets a ConsoleAuditSink without defining one")
  void task1c_appGetsDefault() {
    try (var ctx = start()) {
      assertThat(ctx.getBeanNamesForType(AuditSink.class))
          .withFailMessage("[task 1: app] the application has no AuditSink. Boot finds auto-configurations only"
              + " through the imports file").hasSize(1);
      assertThat(ctx.getBean(AuditSink.class).write("login")).isEqualTo("console: login");
    }
  }

  @Test
  @DisplayName("Task 1: LedgerlyApplication does not cheat with @Import or @ComponentScan")
  void task1d_noCheat() {
    assertThat(LedgerlyApplication.class.isAnnotationPresent(Import.class)
        || LedgerlyApplication.class.isAnnotationPresent(ComponentScan.class))
        .withFailMessage("[task 1: trap] remove @Import/@ComponentScan from LedgerlyApplication. A starter must work"
            + " through the imports file alone")
        .isFalse();
  }

  // ---------- Task 2: back off ----------

  @Test
  @DisplayName("Task 2: with no AuditSink of its own, the app gets the default auditSink")
  void task2a_default() {
    runner.run(ctx -> {
      assertThat(ctx).hasSingleBean(AuditSink.class);
      assertThat(ctx.getBean(AuditSink.class)).isInstanceOf(ConsoleAuditSink.class);
    });
  }

  @Test
  @DisplayName("Task 2: an app with its own AuditSink gets ONLY its own (the default backs off)")
  void task2b_backsOff() {
    runner.withUserConfiguration(OwnSinkConfig.class).run(ctx -> {
      String[] names = ctx.getBeanNamesForType(AuditSink.class);
      assertThat(names).withFailMessage("[task 2: back off] expected only bankAuditSink but found %s."
          + " The default must not be created when the app has its own", Arrays.toString(names))
          .containsExactly("bankAuditSink");
      assertThat(ctx.getBean(AuditSink.class).write("login")).isEqualTo("bank: login");
    });
  }

  @Test
  @DisplayName("Task 2: backing off the sink does not remove the SIEM forwarder")
  void task2c_onlySinkBacksOff() {
    runner.withUserConfiguration(OwnSinkConfig.class).run(ctx ->
        assertThat(ctx.getBeanNamesForType(SiemForwarder.class))
            .withFailMessage("[task 2: trap] put @ConditionalOnMissingBean on the auditSink METHOD, not on the class")
            .hasSize(1));
  }

  // ---------- Task 3: on/off switch ----------

  @Test
  @DisplayName("Task 3: ledgerly.audit.enabled=false removes every bean of the starter")
  void task3a_off() {
    runner.withPropertyValues("ledgerly.audit.enabled=false").run(ctx -> {
      assertThat(ctx.getBeanNamesForType(AuditSink.class))
          .withFailMessage("[task 3: off] with ledgerly.audit.enabled=false there should be no AuditSink").isEmpty();
      assertThat(ctx.getBeanNamesForType(SiemForwarder.class))
          .withFailMessage("[task 3: off] with ledgerly.audit.enabled=false there should be no SiemForwarder").isEmpty();
    });
  }

  @Test
  @DisplayName("Task 3: enabled=true keeps the starter on")
  void task3b_on() {
    runner.withPropertyValues("ledgerly.audit.enabled=true").run(ctx -> assertThat(ctx).hasSingleBean(AuditSink.class));
  }

  @Test
  @DisplayName("Task 3: a MISSING key means on (matchIfMissing)")
  void task3c_missingMeansOn() {
    runner.run(ctx -> assertThat(ctx.getBeanNamesForType(AuditSink.class))
        .withFailMessage("[task 3: missing key] with no ledgerly.audit.enabled key the starter must stay ON").hasSize(1));
  }

  @Test
  @DisplayName("Task 3: a value like 'no' also switches it off (only 'true' turns it on)")
  void task3d_otherValue() {
    runner.withPropertyValues("ledgerly.audit.enabled=no").run(ctx ->
        assertThat(ctx.getBeanNamesForType(AuditSink.class))
            .withFailMessage("[task 3: havingValue] only the value true should switch it on").isEmpty());
  }

  // ---------- Task 4: on the classpath ----------

  @Test
  @DisplayName("Task 4: with the SIEM library on the classpath, a SiemForwarder exists")
  void task4a_present() {
    runner.run(ctx -> {
      assertThat(ctx).hasSingleBean(SiemForwarder.class);
      assertThat(ctx.getBean(SiemForwarder.class).forward("login")).isEqualTo("siem: login");
    });
  }

  @Test
  @DisplayName("Task 4: with the SIEM library hidden, there is no SiemForwarder and the context still starts")
  void task4b_hidden() {
    runner.withClassLoader(new FilteredClassLoader("ledgerly.siem")).run(ctx -> {
      assertThat(ctx).hasNotFailed();
      assertThat(ctx.getBeanNamesForType(SiemForwarder.class))
          .withFailMessage("[task 4: missing class] the SIEM client is not on the classpath, so no SiemForwarder")
          .isEmpty();
      assertThat(ctx).hasSingleBean(AuditSink.class);
    });
  }

  // ---------- Task 5: the conditions report ----------

  @Test
  @DisplayName("Task 5: by default the config class 'matched'")
  void task5a_matched() {
    try (var ctx = start()) {
      String o = ConditionsReader.outcome(ctx, CONFIG);
      assertThat(o).withFailMessage("[task 5: outcome] expected 'matched' for %s but got %s", CONFIG, o).isEqualTo("matched");
    }
  }

  @Test
  @DisplayName("Task 5: switched off, the config class 'did not match'")
  void task5b_didNotMatch() {
    try (var ctx = start("--ledgerly.audit.enabled=false")) {
      String o = ConditionsReader.outcome(ctx, CONFIG);
      assertThat(o).withFailMessage("[task 5: outcome] expected 'did not match' but got %s", o).isEqualTo("did not match");
    }
  }

  @Test
  @DisplayName("Task 5: with the app's own sink, #auditSink 'did not match'")
  void task5c_methodBackedOff() {
    try (var ctx = new SpringApplicationBuilder(LedgerlyApplication.class, OwnSinkConfig.class)
        .web(WebApplicationType.NONE).run()) {
      String o = ConditionsReader.outcome(ctx, CONFIG + "#auditSink");
      assertThat(o).withFailMessage("[task 5: method] expected 'did not match' for #auditSink but got %s", o)
          .isEqualTo("did not match");
    }
  }

  @Test
  @DisplayName("Task 5: a source that was never evaluated is 'not evaluated'")
  void task5d_notEvaluated() {
    try (var ctx = start()) {
      String o = ConditionsReader.outcome(ctx, "ledgerly.NoSuchConfiguration");
      assertThat(o).withFailMessage("[task 5: unknown] expected 'not evaluated' but got %s", o).isEqualTo("not evaluated");
    }
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
  @DisplayName("Task 6: the answer talks about the imports file, conditions, backing off and --debug")
  void task6c_keyIdeas() {
    String body = answerBody().toLowerCase();
    List<String> missing = Arrays.stream(new String[] {"imports", "@conditionalonclass", "@conditionalonmissingbean",
        "back off|backs off|backoff", "--debug|conditions evaluation report"})
        .filter(w -> Arrays.stream(w.split("\\|")).noneMatch(body::contains)).toList();
    assertThat(missing).withFailMessage("[task 6: key ideas] your answer should also mention: %s", missing).isEmpty();
  }

  // ---------- helpers ----------

  private static ConfigurableApplicationContext start(String... args) {
    try {
      return new SpringApplicationBuilder(LedgerlyApplication.class).web(WebApplicationType.NONE).run(args);
    } catch (Exception e) {
      Throwable root = e;
      while (root.getCause() != null) {
        root = root.getCause();
      }
      return fail("[context] the application did not start: " + root.getMessage());
    }
  }

  private static String answer() {
    Path file = Path.of(System.getProperty("user.dir")).resolve("interview-answer.md");
    try {
      return Files.readString(file);
    } catch (IOException e) {
      return fail("[task 6] cannot read " + file.toAbsolutePath());
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
