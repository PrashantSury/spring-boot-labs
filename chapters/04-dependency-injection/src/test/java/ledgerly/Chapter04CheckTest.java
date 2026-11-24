package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import ledgerly.cycle.AccountService;
import ledgerly.cycle.StatementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Checks for chapter 4. Most tests build a small application context with
 * only the classes a task needs, so one unfinished task does not hide the
 * others. Read the message in square brackets.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter04CheckTest {

  // ---------- Task 1: constructor injection in TransferService ----------

  @Test
  @DisplayName("Task 1: TransferService has a constructor (FeePolicy, AuditTrail)")
  void task1a_constructor() {
    constructor(TransferService.class, "task 1: constructor", FeePolicy.class, AuditTrail.class);
  }

  @Test
  @DisplayName("Task 1: TransferService has no @Autowired fields")
  void task1b_noFieldInjection() {
    assertNoAutowiredFields(TransferService.class, "task 1: field injection");
  }

  @Test
  @DisplayName("Task 1: every TransferService field is private final")
  void task1c_finalFields() {
    assertFinalFields(TransferService.class, "task 1: final fields", 2);
  }

  @Test
  @DisplayName("Task 1: a plain unit test can build it with new and a fake fee (no Spring)")
  void task1d_plainUnitTest() throws Exception {
    Constructor<TransferService> c =
        constructor(TransferService.class, "task 1: constructor", FeePolicy.class, AuditTrail.class);
    AuditTrail audit = new AuditTrail();
    FeePolicy fake = amount -> 7;
    Transfer t = c.newInstance(fake, audit).transfer("ACC-1", "ACC-2", 50_000);
    assertThat(t.fee())
        .withFailMessage("[task 1: fake fee] built with a fake FeePolicy that always charges 7, but the fee was %d."
            + " Use the FeePolicy passed to the constructor", t.fee())
        .isEqualTo(7);
    assertThat(audit.lines())
        .withFailMessage("[task 1: audit] expected one audit line in the AuditTrail passed in, got %s", audit.lines())
        .containsExactly("ACC-1 -> ACC-2 50000 fee 7");
  }

  @Test
  @DisplayName("Task 1: the class does not use @Autowired on its constructor (one constructor needs none)")
  void task1e_noAutowiredConstructor() {
    for (Constructor<?> c : TransferService.class.getDeclaredConstructors()) {
      assertThat(c.isAnnotationPresent(Autowired.class))
          .withFailMessage("[task 1: @Autowired] a class with ONE constructor does not need @Autowired. Remove it")
          .isFalse();
    }
  }

  // ---------- Task 2: @Primary and @Qualifier ----------

  @Test
  @DisplayName("Task 2: standardFees is the primary FeePolicy, zeroFees is not")
  void task2a_primary() {
    try (var ctx = context()) {
      assertThat(ctx.getBeanFactory().getBeanDefinition("standardFees").isPrimary())
          .withFailMessage("[task 2: primary] standardFees is not marked primary").isTrue();
      assertThat(ctx.getBeanFactory().getBeanDefinition("zeroFees").isPrimary())
          .withFailMessage("[task 2: trap] zeroFees must NOT be primary too").isFalse();
    }
  }

  @Test
  @DisplayName("Task 2: PremiumTransferService takes a FeePolicy with @Qualifier(\"zeroFees\")")
  void task2b_qualifier() {
    Constructor<PremiumTransferService> c =
        constructor(PremiumTransferService.class, "task 2: constructor", FeePolicy.class);
    Parameter p = c.getParameters()[0];
    Qualifier q = p.getAnnotation(Qualifier.class);
    assertThat(q)
        .withFailMessage("[task 2: qualifier] the FeePolicy parameter has no @Qualifier. Without it, it gets the primary")
        .isNotNull();
    assertThat(q.value())
        .withFailMessage("[task 2: qualifier] expected @Qualifier(\"zeroFees\") but got @Qualifier(\"%s\")", q.value())
        .isEqualTo("zeroFees");
  }

  @Test
  @DisplayName("Task 2: PremiumTransferService has no @Autowired fields and final fields only")
  void task2c_premiumFields() {
    assertNoAutowiredFields(PremiumTransferService.class, "task 2: field injection");
    assertFinalFields(PremiumTransferService.class, "task 2: final fields", 1);
  }

  @Test
  @DisplayName("Task 2: in the context, a standard transfer of 50,000 costs 500")
  void task2d_standardInContext() {
    try (var ctx = context()) {
      Transfer t = ctx.getBean(TransferService.class).transfer("ACC-1", "ACC-2", 50_000);
      assertThat(t.fee()).withFailMessage("[task 2: standard] expected fee 500 but got %d", t.fee()).isEqualTo(500);
    }
  }

  @Test
  @DisplayName("Task 2: in the context, a premium transfer of 50,000 costs 0")
  void task2e_premiumInContext() {
    try (var ctx = context()) {
      Transfer t = ctx.getBean(PremiumTransferService.class).transfer("ACC-1", "ACC-2", 50_000);
      assertThat(t.fee())
          .withFailMessage("[task 2: premium] expected fee 0 (zeroFees) but got %d. Did it get the primary?", t.fee())
          .isEqualTo(0);
    }
  }

  @Test
  @DisplayName("Task 2: a small standard transfer still pays the minimum fee of 10")
  void task2f_minimumFee() {
    try (var ctx = context()) {
      Transfer t = ctx.getBean(TransferService.class).transfer("ACC-1", "ACC-2", 300);
      assertThat(t.fee()).withFailMessage("[task 2: minimum] expected fee 10 on 300 but got %d", t.fee()).isEqualTo(10);
    }
  }

  // ---------- Task 3: List and Map injection ----------

  @Test
  @DisplayName("Task 3: notifyAll uses every Notifier, in @Order order")
  void task3a_notifyAllInOrder() {
    try (var ctx = hub()) {
      List<String> sent = ctx.getBean(NotificationHub.class).notifyAll("Salary credited");
      assertThat(sent)
          .withFailMessage("[task 3: notifyAll] expected [SMS: Salary credited, EMAIL: Salary credited,"
              + " PUSH: Salary credited] but got %s. Inject List<Notifier>", sent)
          .containsExactly("SMS: Salary credited", "EMAIL: Salary credited", "PUSH: Salary credited");
    }
  }

  @Test
  @DisplayName("Task 3: notifyVia picks one channel by bean name")
  void task3b_notifyVia() {
    try (var ctx = hub()) {
      String r = ctx.getBean(NotificationHub.class).notifyVia("emailNotifier", "OTP 4821");
      assertThat(r).withFailMessage("[task 3: notifyVia] expected 'EMAIL: OTP 4821' but got %s. Inject Map<String, Notifier>", r)
          .isEqualTo("EMAIL: OTP 4821");
    }
  }

  @Test
  @DisplayName("Task 3: notifyVia with an unknown name throws IllegalArgumentException")
  void task3c_unknownChannel() {
    try (var ctx = hub()) {
      NotificationHub hub = ctx.getBean(NotificationHub.class);
      try {
        String r = hub.notifyVia("fax", "hi");
        fail("[task 3: unknown channel] expected IllegalArgumentException(\"no notifier named fax\") but got " + r);
      } catch (IllegalArgumentException e) {
        assertThat(e.getMessage()).withFailMessage("[task 3: message] expected 'no notifier named fax' but got '%s'", e.getMessage())
            .isEqualTo("no notifier named fax");
      }
    }
  }

  @Test
  @DisplayName("Task 3: channels() is the sorted set of bean names")
  void task3d_channels() {
    try (var ctx = hub()) {
      assertThat(ctx.getBean(NotificationHub.class).channels())
          .withFailMessage("[task 3: channels] expected [emailNotifier, pushNotifier, smsNotifier] but got %s",
              ctx.getBean(NotificationHub.class).channels())
          .containsExactly("emailNotifier", "pushNotifier", "smsNotifier");
    }
  }

  @Test
  @DisplayName("Task 3: a fourth Notifier bean is picked up with no change to NotificationHub")
  void task3e_newChannel() {
    var ctx = new AnnotationConfigApplicationContext();
    ctx.register(PushNotifier.class, EmailNotifier.class, SmsNotifier.class, NotificationHub.class);
    ctx.registerBean("whatsappNotifier", Notifier.class, () -> m -> "WHATSAPP: " + m);
    refresh(ctx);
    try (ctx) {
      List<String> sent = ctx.getBean(NotificationHub.class).notifyAll("hi");
      assertThat(sent).withFailMessage("[task 3: open for extension] with 4 Notifier beans expected 4 messages but got %s", sent)
          .hasSize(4).contains("WHATSAPP: hi");
      assertThat(ctx.getBean(NotificationHub.class).notifyVia("whatsappNotifier", "hi")).isEqualTo("WHATSAPP: hi");
    }
  }

  // ---------- Task 4: optional dependency ----------

  @Test
  @DisplayName("Task 4: with NO FraudScreen bean, the context starts and 50,000 is allowed")
  void task4a_noScreen() {
    try (var ctx = limits(false)) {
      assertThat(ctx.getBean(LimitChecker.class).allowed("ACC-1", 50_000))
          .withFailMessage("[task 4: allowed] 50,000 should be allowed when there is no FraudScreen").isTrue();
    }
  }

  @Test
  @DisplayName("Task 4: 2,00,000 is allowed, 2,00,001 is not (the limit is inclusive)")
  void task4b_limitEdge() {
    try (var ctx = limits(false)) {
      LimitChecker c = ctx.getBean(LimitChecker.class);
      assertThat(c.allowed("ACC-1", 200_000)).withFailMessage("[task 4: edge] exactly 200000 must be allowed").isTrue();
      assertThat(c.allowed("ACC-1", 200_001)).withFailMessage("[task 4: edge] 200001 is over the limit").isFalse();
    }
  }

  @Test
  @DisplayName("Task 4: zero and negative amounts are never allowed")
  void task4c_nonPositive() {
    try (var ctx = limits(false)) {
      LimitChecker c = ctx.getBean(LimitChecker.class);
      assertThat(c.allowed("ACC-1", 0)).withFailMessage("[task 4: zero] an amount of 0 must not be allowed").isFalse();
      assertThat(c.allowed("ACC-1", -500)).withFailMessage("[task 4: negative] -500 must not be allowed").isFalse();
    }
  }

  @Test
  @DisplayName("Task 4: when a FraudScreen bean exists, a blocked account is refused")
  void task4d_withScreen() {
    try (var ctx = limits(true)) {
      LimitChecker c = ctx.getBean(LimitChecker.class);
      assertThat(c.allowed("ACC-666", 100))
          .withFailMessage("[task 4: fraud] ACC-666 is blocked by the FraudScreen bean but was allowed. Use the screen when it exists")
          .isFalse();
      assertThat(c.allowed("ACC-1", 100))
          .withFailMessage("[task 4: fraud] ACC-1 is not blocked and 100 is fine, but it was refused").isTrue();
    }
  }

  // ---------- Task 5: the circular dependency ----------

  @Test
  @DisplayName("Task 5: AccountService and StatementService start together (no cycle error)")
  void task5a_starts() {
    try (var ctx = cycle()) {
      assertThat(ctx.getBean(AccountService.class)).isNotNull();
    }
  }

  @Test
  @DisplayName("Task 5: closing ACC-1 prints its final balance 52000")
  void task5b_close() {
    try (var ctx = cycle()) {
      String r = ctx.getBean(AccountService.class).close("ACC-1");
      assertThat(r).withFailMessage("[task 5: close] expected 'closed ACC-1 | final balance 52000' but got '%s'", r)
          .isEqualTo("closed ACC-1 | final balance 52000");
    }
  }

  @Test
  @DisplayName("Task 5: an unknown account closes with balance 0")
  void task5c_unknown() {
    try (var ctx = cycle()) {
      assertThat(ctx.getBean(AccountService.class).close("ACC-9")).isEqualTo("closed ACC-9 | final balance 0");
    }
  }

  @Test
  @DisplayName("Task 5: StatementService still uses a final field and its constructor (no field injection)")
  void task5d_stillConstructor() {
    assertNoAutowiredFields(StatementService.class, "task 5: field injection");
    assertFinalFields(StatementService.class, "task 5: final field", 1);
    assertThat(StatementService.class.getDeclaredConstructors()[0].getParameterCount())
        .withFailMessage("[task 5: constructor] keep the one-parameter constructor").isEqualTo(1);
  }

  // ---------- Task 6 (Interview) ----------

  @Test
  @DisplayName("Task 6: interview-answer.md has your answer, not the TODO line")
  void task6a_answered() {
    assertThat(answer())
        .withFailMessage("[task 6: interview answer] interview-answer.md still has the 'TODO: write your answer here' line")
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
  @DisplayName("Task 6: the answer talks about constructor, final, @Qualifier and the cycle")
  void task6c_keyIdeas() {
    String body = answerBody().toLowerCase();
    List<String> missing = Arrays.stream(new String[] {"constructor", "final", "@qualifier", "circular|cycle"})
        .filter(w -> Arrays.stream(w.split("\\|")).noneMatch(body::contains)).toList();
    assertThat(missing).withFailMessage("[task 6: key ideas] your answer should also mention: %s", missing).isEmpty();
  }

  // ---------- helpers ----------

  private static AnnotationConfigApplicationContext context() {
    var ctx = new AnnotationConfigApplicationContext();
    ctx.scan("ledgerly");
    return refresh(ctx);
  }

  private static AnnotationConfigApplicationContext hub() {
    var ctx = new AnnotationConfigApplicationContext();
    // registered in reverse on purpose: only @Order decides the list order
    ctx.register(PushNotifier.class, EmailNotifier.class, SmsNotifier.class, NotificationHub.class);
    return refresh(ctx);
  }

  private static AnnotationConfigApplicationContext limits(boolean withScreen) {
    var ctx = new AnnotationConfigApplicationContext();
    if (withScreen) {
      ctx.registerBean("fraudScreen", FraudScreen.class, () -> account -> account.equals("ACC-666"));
    }
    ctx.register(LimitChecker.class);
    return refresh(ctx);
  }

  private static AnnotationConfigApplicationContext cycle() {
    var ctx = new AnnotationConfigApplicationContext();
    ctx.register(AccountService.class, StatementService.class);
    return refresh(ctx);
  }

  private static AnnotationConfigApplicationContext refresh(AnnotationConfigApplicationContext ctx) {
    try {
      ctx.refresh();
      return ctx;
    } catch (Exception e) {
      Throwable root = e;
      while (root.getCause() != null) {
        root = root.getCause();
      }
      return fail("[context] the application context did not start: " + root.getClass().getSimpleName()
          + ": " + root.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> Constructor<T> constructor(Class<T> type, String label, Class<?>... params) {
    try {
      return type.getDeclaredConstructor(params);
    } catch (NoSuchMethodException e) {
      return fail("[" + label + "] " + type.getSimpleName() + " has no constructor ("
          + String.join(", ", Arrays.stream(params).map(Class::getSimpleName).toList()) + "). Found: "
          + Arrays.toString(type.getDeclaredConstructors()));
    }
  }

  private static void assertNoAutowiredFields(Class<?> type, String label) {
    for (Field f : type.getDeclaredFields()) {
      assertThat(f.isAnnotationPresent(Autowired.class))
          .withFailMessage("[%s] field '%s' in %s still uses @Autowired. Take it through the constructor",
              label, f.getName(), type.getSimpleName())
          .isFalse();
    }
  }

  private static void assertFinalFields(Class<?> type, String label, int expected) {
    List<Field> fields = Arrays.stream(type.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers())).toList();
    assertThat(fields).withFailMessage("[%s] expected %d instance field(s) in %s but found %d",
        label, expected, type.getSimpleName(), fields.size()).hasSize(expected);
    for (Field f : fields) {
      assertThat(Modifier.isFinal(f.getModifiers()) && Modifier.isPrivate(f.getModifiers()))
          .withFailMessage("[%s] field '%s' in %s must be private final", label, f.getName(), type.getSimpleName())
          .isTrue();
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
