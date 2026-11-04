package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Checks for chapter 3. Each test builds a fresh application context from
 * package ledgerly and looks inside it. Read the failure message.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter03CheckTest {

  // ---------- Task 1: FeeCalculator is a bean ----------

  @Test
  @DisplayName("Task 1: the context holds a FeeCalculator bean")
  void task1a_isBean() {
    try (var ctx = context()) {
      assertThat(ctx.getBeanNamesForType(FeeCalculator.class))
          .withFailMessage("[task 1: FeeCalculator] the context has no bean of type FeeCalculator."
              + " Mark the class so component scanning finds it")
          .isNotEmpty();
    }
  }

  @Test
  @DisplayName("Task 1: its default bean name is feeCalculator")
  void task1b_defaultName() {
    try (var ctx = context()) {
      assertThat(ctx.containsBean("feeCalculator"))
          .withFailMessage("[task 1: bean name] expected a bean named feeCalculator."
              + " The default name is the class name with a lower-case first letter."
              + " Beans found of that type: %s", String.join(",", ctx.getBeanNamesForType(FeeCalculator.class)))
          .isTrue();
    }
  }

  // ---------- Task 2: bankClock ----------

  @Test
  @DisplayName("Task 2: a bean named bankClock of type Clock exists")
  void task2a_clockBean() {
    try (var ctx = context()) {
      assertThat(ctx.containsBean("bankClock"))
          .withFailMessage("[task 2: bankClock] there is no bean named bankClock."
              + " Is the method annotated so it becomes a bean?")
          .isTrue();
      assertThat(ctx.getBeanNamesForType(Clock.class))
          .withFailMessage("[task 2: bankClock] the bean exists but it is not a Clock (is the method body still return null?)")
          .contains("bankClock");
    }
  }

  @Test
  @DisplayName("Task 2: the clock is set to Asia/Kolkata")
  void task2b_clockZone() {
    try (var ctx = context()) {
      Clock clock = bean(ctx, "task 2: bankClock", "bankClock", Clock.class);
      assertThat(clock.getZone().getId())
          .withFailMessage("[task 2: clock zone] expected Asia/Kolkata but got %s", clock.getZone().getId())
          .isEqualTo("Asia/Kolkata");
    }
  }

  // ---------- Task 3: two named policies ----------

  @Test
  @DisplayName("Task 3: savingsPolicy is 'savings' at 3.5 percent")
  void task3a_savings() {
    try (var ctx = context()) {
      InterestPolicy p = bean(ctx, "task 3: savingsPolicy", "savingsPolicy", InterestPolicy.class);
      assertThat(p)
          .withFailMessage("[task 3: savingsPolicy] expected InterestPolicy[name=savings, annualRatePercent=3.5] but got %s", p)
          .isEqualTo(new InterestPolicy("savings", 3.5));
    }
  }

  @Test
  @DisplayName("Task 3: currentPolicy is 'current' at 0.0 percent")
  void task3b_current() {
    try (var ctx = context()) {
      InterestPolicy p = bean(ctx, "task 3: currentPolicy", "currentPolicy", InterestPolicy.class);
      assertThat(p)
          .withFailMessage("[task 3: currentPolicy] expected InterestPolicy[name=current, annualRatePercent=0.0] but got %s", p)
          .isEqualTo(new InterestPolicy("current", 0.0));
    }
  }

  @Test
  @DisplayName("Task 3: there are exactly two InterestPolicy beans")
  void task3c_exactlyTwo() {
    try (var ctx = context()) {
      Map<String, InterestPolicy> all = ctx.getBeansOfType(InterestPolicy.class);
      assertThat(all)
          .withFailMessage("[task 3: how many] expected 2 InterestPolicy beans but found %d: %s", all.size(), all.keySet())
          .hasSize(2);
    }
  }

  // ---------- Task 4: Receipt is a prototype ----------

  @Test
  @DisplayName("Task 4: Receipt is prototype scoped")
  void task4a_prototype() {
    try (var ctx = context()) {
      String[] names = ctx.getBeanNamesForType(Receipt.class);
      assertThat(names).withFailMessage("[task 4: Receipt] the Receipt bean disappeared. Keep @Component").isNotEmpty();
      assertThat(ctx.isPrototype(names[0]))
          .withFailMessage("[task 4: scope] Receipt is still a singleton. Spring's default is ONE shared instance."
              + " Change its scope to prototype")
          .isTrue();
    }
  }

  @Test
  @DisplayName("Task 4: two receipts have two different ids")
  void task4b_differentReceipts() {
    try (var ctx = context()) {
      Receipt a = ctx.getBean(Receipt.class);
      Receipt b = ctx.getBean(Receipt.class);
      assertThat(a)
          .withFailMessage("[task 4: receipts] asked twice, got the SAME Receipt (id %s). Every transfer needs its own", a.id())
          .isNotSameAs(b);
      assertThat(a.id()).isNotEqualTo(b.id());
    }
  }

  @Test
  @DisplayName("Task 4: FeeCalculator stays a singleton (only Receipt changes)")
  void task4c_othersStaySingleton() {
    try (var ctx = context()) {
      FeeCalculator a = bean(ctx, "task 1: FeeCalculator", "feeCalculator", FeeCalculator.class);
      FeeCalculator b = ctx.getBean(FeeCalculator.class);
      assertThat(a)
          .withFailMessage("[task 4: trap] FeeCalculator should stay a singleton. Only Receipt needs to be a prototype")
          .isSameAs(b);
    }
  }

  // ---------- Task 5: primary ----------

  @Test
  @DisplayName("Task 5: asking for 'the' InterestPolicy by type returns the savings policy")
  void task5a_primaryWins() {
    try (var ctx = context()) {
      InterestPolicy p;
      try {
        p = ctx.getBean(InterestPolicy.class);
      } catch (NoUniqueBeanDefinitionException e) {
        throw new AssertionError("[task 5: primary] Spring found " + e.getNumberOfBeansFound()
            + " InterestPolicy beans " + e.getBeanNamesFound() + " and will not guess."
            + " Mark savingsPolicy as the primary bean", e);
      } catch (NoSuchBeanDefinitionException e) {
        throw new AssertionError("[task 5: primary] there is no InterestPolicy bean yet. Do task 3 first", e);
      }
      assertThat(p.name())
          .withFailMessage("[task 5: primary] expected the savings policy but got %s", p)
          .isEqualTo("savings");
    }
  }

  @Test
  @DisplayName("Task 5: only savingsPolicy is primary")
  void task5b_onlyOnePrimary() {
    try (var ctx = context()) {
      bean(ctx, "task 3: savingsPolicy", "savingsPolicy", InterestPolicy.class);
      bean(ctx, "task 3: currentPolicy", "currentPolicy", InterestPolicy.class);
      assertThat(ctx.getBeanFactory().getBeanDefinition("savingsPolicy").isPrimary())
          .withFailMessage("[task 5: primary] savingsPolicy is not marked primary").isTrue();
      assertThat(ctx.getBeanFactory().getBeanDefinition("currentPolicy").isPrimary())
          .withFailMessage("[task 5: trap] currentPolicy must NOT be primary too. Two primaries is an error again")
          .isFalse();
    }
  }

  // ---------- Task 6 (Interview): your answer ----------

  @Test
  @DisplayName("Task 6: interview-answer.md has your answer, not the TODO line")
  void task6a_answered() {
    String text = answer();
    assertThat(text)
        .withFailMessage("[task 6: interview answer] interview-answer.md still has the 'TODO: write your answer here' line."
            + " Write your answer and delete that line")
        .doesNotContain("TODO: write your answer here");
  }

  @Test
  @DisplayName("Task 6: the answer is at least 40 words")
  void task6b_longEnough() {
    int words = answerBody().isBlank() ? 0 : answerBody().trim().split("\\s+").length;
    assertThat(words)
        .withFailMessage("[task 6: length] your answer has %d words. Write at least 40, as you would say it aloud", words)
        .isGreaterThanOrEqualTo(40);
  }

  @Test
  @DisplayName("Task 6: the answer talks about singleton, thread safety and @Bean")
  void task6c_keyIdeas() {
    String body = answerBody().toLowerCase();
    assertThat(body)
        .withFailMessage("[task 6: key ideas] your answer should mention: singleton, thread (safety) and @Bean."
            + " Missing: %s", missing(body))
        .contains("singleton")
        .contains("thread")
        .contains("@bean");
  }

  // ---------- helpers ----------

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

  private static <T> T bean(AnnotationConfigApplicationContext ctx, String label, String name, Class<T> type) {
    if (!ctx.containsBean(name)) {
      fail("[" + label + "] there is no bean named " + name + " yet");
    }
    Object bean = ctx.getBean(name);
    if (!type.isInstance(bean)) {
      fail("[" + label + "] the bean " + name + " is not a " + type.getSimpleName() + " (is the method body still 'return null'?)");
    }
    return type.cast(bean);
  }

  private static String answer() {
    Path file = Path.of(System.getProperty("user.dir")).resolve("interview-answer.md");
    try {
      return Files.readString(file);
    } catch (IOException e) {
      return fail("[task 6] cannot read " + file.toAbsolutePath());
    }
  }

  /** The answer without the question block above the first blank-line-separated "Write" line. */
  private static String answerBody() {
    String text = answer();
    int marker = text.indexOf("Delete the line that says TODO.");
    String body = marker >= 0 ? text.substring(marker + "Delete the line that says TODO.".length()) : text;
    return body.replace("TODO: write your answer here", "").trim();
  }

  private static String missing(String body) {
    StringBuilder sb = new StringBuilder();
    for (String word : new String[] {"singleton", "thread", "@bean"}) {
      if (!body.contains(word)) {
        sb.append(word).append(' ');
      }
    }
    return sb.toString().trim();
  }
}
