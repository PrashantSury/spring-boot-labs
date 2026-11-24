package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Checks for chapter 5. Each test starts the application with a given set of
 * profiles and arguments, then reads what Spring bound. Read the message in
 * square brackets.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter05CheckTest {

  // ---------- Task 1: @Value ----------

  @Test
  @DisplayName("Task 1: bankName comes from ledgerly.bank-name in application.properties")
  void task1a_bankName() {
    try (var ctx = start(List.of())) {
      String name = ctx.getBean(BranchInfo.class).bankName();
      assertThat(name).withFailMessage("[task 1: bank name] expected 'Ledgerly Bank' but got %s."
          + " Add the key to application.properties and read it with @Value", name).isEqualTo("Ledgerly Bank");
    }
  }

  @Test
  @DisplayName("Task 1: supportPhone falls back to 1800-123-4567 when the key is missing")
  void task1b_default() {
    try (var ctx = start(List.of())) {
      String phone = ctx.getBean(BranchInfo.class).supportPhone();
      assertThat(phone).withFailMessage("[task 1: default] expected the default 1800-123-4567 but got %s."
          + " Write the default after a colon: ${key:default}", phone).isEqualTo("1800-123-4567");
    }
  }

  @Test
  @DisplayName("Task 1: supportPhone uses ledgerly.support-phone when it is set")
  void task1c_overrideDefault() {
    try (var ctx = start(List.of(), "--ledgerly.support-phone=022-5550-1234")) {
      String phone = ctx.getBean(BranchInfo.class).supportPhone();
      assertThat(phone).withFailMessage("[task 1: set value] the key was set to 022-5550-1234 but you returned %s", phone)
          .isEqualTo("022-5550-1234");
    }
  }

  @Test
  @DisplayName("Task 1: without ledgerly.bank-name the application refuses to start (no silent default)")
  void task1d_requiredKey() {
    var ctx = new AnnotationConfigApplicationContext();
    ctx.registerBean(PropertySourcesPlaceholderConfigurer.class);
    ctx.register(BranchInfo.class);
    String error;
    try (ctx) {
      ctx.refresh();
      error = "it started, and bankName() is " + ctx.getBean(BranchInfo.class).bankName();
    } catch (Exception e) {
      error = root(e);
    }
    assertThat(error).withFailMessage("[task 1: required] with no bank-name key the app should fail with"
        + " 'Could not resolve placeholder ledgerly.bank-name', but %s", error).contains("ledgerly.bank-name");
  }

  // ---------- Task 2: @ConfigurationProperties ----------

  @Test
  @DisplayName("Task 2: TransferProperties is a bean bound from ledgerly.transfer.*")
  void task2a_bound() {
    try (var ctx = start(List.of())) {
      TransferProperties p = transfer(ctx);
      assertThat(p.dailyLimit()).withFailMessage("[task 2: daily-limit] expected 200000 but got %s", p.dailyLimit())
          .isEqualTo(200_000L);
      assertThat(p.feePercent()).withFailMessage("[task 2: fee-percent] expected 1.0 but got %s", p.feePercent())
          .isEqualByComparingTo(new BigDecimal("1.0"));
    }
  }

  @Test
  @DisplayName("Task 2: otp-validity=5m binds to a Duration of 5 minutes")
  void task2b_duration() {
    try (var ctx = start(List.of())) {
      Duration d = transfer(ctx).otpValidity();
      assertThat(d).withFailMessage("[task 2: otp-validity] expected PT5M (5 minutes) but got %s", d)
          .isEqualTo(Duration.ofMinutes(5));
    }
  }

  @Test
  @DisplayName("Task 2: a daily limit of 0 stops the application at startup")
  void task2c_zeroLimitRejected() {
    String error = startFails(List.of(), "--ledgerly.transfer.daily-limit=0");
    assertThat(error).withFailMessage("[task 2: validation] expected a binding error about dailyLimit but got: %s", error)
        .contains("dailyLimit");
  }

  @Test
  @DisplayName("Task 2: a fee of 7.5 percent stops the application (max is 5.0)")
  void task2d_feeTooHigh() {
    String error = startFails(List.of(), "--ledgerly.transfer.fee-percent=7.5");
    assertThat(error).withFailMessage("[task 2: validation] expected a binding error about feePercent but got: %s", error)
        .contains("feePercent");
  }

  @Test
  @DisplayName("Task 2: a fee of exactly 5.0 and of 0.0 are both accepted (edges)")
  void task2e_feeEdges() {
    try (var ctx = start(List.of(), "--ledgerly.transfer.fee-percent=5.0")) {
      assertThat(transfer(ctx).feePercent()).isEqualByComparingTo("5.0");
    }
    try (var ctx = start(List.of(), "--ledgerly.transfer.fee-percent=0.0")) {
      assertThat(transfer(ctx).feePercent()).isEqualByComparingTo("0.0");
    }
  }

  @Test
  @DisplayName("Task 2: a missing otp-validity stops the application")
  void task2f_missingRejected() {
    String error = startFails(List.of(), "--ledgerly.transfer.otp-validity=");
    assertThat(error).withFailMessage("[task 2: validation] expected a binding error about otpValidity but got: %s", error)
        .contains("otpValidity");
  }

  // ---------- Task 3: profile files ----------

  @Test
  @DisplayName("Task 3: prod uses the postgres url and a daily limit of 100000")
  void task3a_prod() {
    try (var ctx = start(List.of("prod"))) {
      assertThat(prop(ctx, "ledgerly.database.url"))
          .withFailMessage("[task 3: prod url] expected jdbc:postgresql://db.ledgerly.internal:5432/ledgerly but got %s",
              prop(ctx, "ledgerly.database.url"))
          .isEqualTo("jdbc:postgresql://db.ledgerly.internal:5432/ledgerly");
      assertThat(transfer(ctx).dailyLimit())
          .withFailMessage("[task 3: prod limit] expected 100000 in prod but got %s", transfer(ctx).dailyLimit())
          .isEqualTo(100_000L);
    }
  }

  @Test
  @DisplayName("Task 3: prod still inherits fee-percent and bank-name from application.properties")
  void task3b_prodInherits() {
    try (var ctx = start(List.of("prod"))) {
      assertThat(transfer(ctx).feePercent())
          .withFailMessage("[task 3: inherit] prod should keep fee-percent 1.0 from the base file. Don't copy every key")
          .isEqualByComparingTo("1.0");
      assertThat(ctx.getBean(BranchInfo.class).bankName()).isEqualTo("Ledgerly Bank");
    }
  }

  @Test
  @DisplayName("Task 3: dev renames the bank but keeps the base h2 url and limit")
  void task3c_dev() {
    try (var ctx = start(List.of("dev"))) {
      assertThat(ctx.getBean(BranchInfo.class).bankName())
          .withFailMessage("[task 3: dev name] expected 'Ledgerly Bank (DEV)' but got %s", ctx.getBean(BranchInfo.class).bankName())
          .isEqualTo("Ledgerly Bank (DEV)");
      assertThat(prop(ctx, "ledgerly.database.url")).isEqualTo("jdbc:h2:mem:ledgerly");
      assertThat(transfer(ctx).dailyLimit()).isEqualTo(200_000L);
    }
  }

  @Test
  @DisplayName("Task 3: with no profile, the base url and limit are used")
  void task3d_noProfile() {
    try (var ctx = start(List.of())) {
      assertThat(prop(ctx, "ledgerly.database.url")).isEqualTo("jdbc:h2:mem:ledgerly");
      assertThat(transfer(ctx).dailyLimit()).isEqualTo(200_000L);
    }
  }

  // ---------- Task 4: @Profile beans ----------

  @Test
  @DisplayName("Task 4: with no profile there is exactly one SmsGateway, the fake one")
  void task4a_defaultFake() {
    try (var ctx = start(List.of())) {
      assertGateway(ctx, FakeSmsGateway.class, "no profile");
    }
  }

  @Test
  @DisplayName("Task 4: dev gets the fake gateway")
  void task4b_devFake() {
    try (var ctx = start(List.of("dev"))) {
      assertGateway(ctx, FakeSmsGateway.class, "dev");
    }
  }

  @Test
  @DisplayName("Task 4: prod gets the real gateway, and the fake one does not exist")
  void task4c_prodReal() {
    try (var ctx = start(List.of("prod"))) {
      assertGateway(ctx, RealSmsGateway.class, "prod");
    }
  }

  @Test
  @DisplayName("Task 4: a uat profile (no file of its own) still gets the fake gateway")
  void task4d_uatFake() {
    try (var ctx = start(List.of("uat"))) {
      assertGateway(ctx, FakeSmsGateway.class, "uat");
    }
  }

  // ---------- Task 5: precedence ----------

  @Test
  @DisplayName("Task 5: a command-line argument beats every file")
  void task5a_commandLine() {
    try (var ctx = start(List.of("prod"), "--ledgerly.transfer.daily-limit=150000")) {
      assertThat(transfer(ctx).dailyLimit()).isEqualTo(150_000L);
      String from = report(ctx).whereFrom("ledgerly.transfer.daily-limit");
      assertThat(from).withFailMessage("[task 5: whereFrom] a command-line value comes from 'commandLineArgs' but you said %s", from)
          .isEqualTo("commandLineArgs");
    }
  }

  @Test
  @DisplayName("Task 5: in prod, daily-limit comes from application-prod.properties")
  void task5b_profileFile() {
    try (var ctx = start(List.of("prod"))) {
      String from = report(ctx).whereFrom("ledgerly.transfer.daily-limit");
      assertThat(from).withFailMessage("[task 5: whereFrom] expected a source naming application-prod.properties but got %s", from)
          .isNotNull().contains("application-prod.properties");
    }
  }

  @Test
  @DisplayName("Task 5: in prod, fee-percent still comes from the base application.properties")
  void task5c_baseFile() {
    try (var ctx = start(List.of("prod"))) {
      String from = report(ctx).whereFrom("ledgerly.transfer.fee-percent");
      assertThat(from).withFailMessage("[task 5: whereFrom] expected a source naming application.properties but got %s", from)
          .isNotNull().contains("application.properties").doesNotContain("application-prod");
    }
  }

  @Test
  @DisplayName("Task 5: an unknown key answers 'not set'")
  void task5d_notSet() {
    try (var ctx = start(List.of())) {
      String from = report(ctx).whereFrom("ledgerly.no-such-key");
      assertThat(from).withFailMessage("[task 5: not set] expected 'not set' but got %s", from).isEqualTo("not set");
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
  @DisplayName("Task 6: the answer talks about @Value, @ConfigurationProperties, profile and precedence")
  void task6c_keyIdeas() {
    String body = answerBody().toLowerCase();
    List<String> missing = Arrays.stream(new String[] {"@value", "@configurationproperties", "profile",
        "environment variable|command line|command-line|precedence"})
        .filter(w -> Arrays.stream(w.split("\\|")).noneMatch(body::contains)).toList();
    assertThat(missing).withFailMessage("[task 6: key ideas] your answer should also mention: %s", missing).isEmpty();
  }

  // ---------- helpers ----------

  private static ConfigurableApplicationContext start(List<String> profiles, String... args) {
    try {
      return builder(profiles).run(args);
    } catch (Exception e) {
      return fail("[context] the application did not start: " + root(e));
    }
  }

  private static String startFails(List<String> profiles, String... args) {
    try (var ctx = builder(profiles).run(args)) {
      return fail("[startup] expected the application to REFUSE to start with " + Arrays.toString(args)
          + ", but it started");
    } catch (Exception e) {
      StringBuilder all = new StringBuilder();
      for (Throwable t = e; t != null; t = t.getCause()) {
        all.append(t.getMessage()).append(" | ");
      }
      return all.toString();
    }
  }

  private static SpringApplicationBuilder builder(List<String> profiles) {
    return new SpringApplicationBuilder(LedgerlyApplication.class)
        .web(WebApplicationType.NONE)
        .profiles(profiles.toArray(String[]::new))
        .logStartupInfo(false);
  }

  private static TransferProperties transfer(ConfigurableApplicationContext ctx) {
    if (ctx.getBeanNamesForType(TransferProperties.class).length == 0) {
      return fail("[task 2: TransferProperties] there is no TransferProperties bean. Bind the record with"
          + " @ConfigurationProperties and add @ConfigurationPropertiesScan to LedgerlyApplication");
    }
    return ctx.getBean(TransferProperties.class);
  }

  private static EnvironmentReport report(ConfigurableApplicationContext ctx) {
    return ctx.getBean(EnvironmentReport.class);
  }

  private static String prop(ConfigurableApplicationContext ctx, String key) {
    return ctx.getEnvironment().getProperty(key);
  }

  private static void assertGateway(ConfigurableApplicationContext ctx, Class<?> expected, String profile) {
    String[] names = ctx.getBeanNamesForType(SmsGateway.class);
    assertThat(names).withFailMessage("[task 4: %s] expected exactly ONE SmsGateway bean (%s) but found %d: %s",
        profile, expected.getSimpleName(), names.length, Arrays.toString(names)).hasSize(1);
    assertThat(ctx.getBean(SmsGateway.class)).withFailMessage("[task 4: %s] expected %s", profile, expected.getSimpleName())
        .isInstanceOf(expected);
  }

  private static String root(Throwable e) {
    while (e.getCause() != null) {
      e = e.getCause();
    }
    return e.getClass().getSimpleName() + ": " + e.getMessage();
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
