package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.YearMonth;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

/** Checks for chapter 32. FxUpstream counts real (slow, paid) calls. */
@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter32CheckTest {

  @Autowired FxRateService fx;
  @Autowired FxUpstream upstream;
  @Autowired StatementService statements;
  @Autowired ObjectProvider<CacheManager> cacheManager;

  @BeforeEach
  void reset() {
    CacheManager cm = cacheManager.getIfAvailable();
    if (cm != null) cm.getCacheNames().forEach(n -> cm.getCache(n).clear());
    upstream.reset();
    statements.resetBuilds();
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: rate(USD) three times calls the upstream ONCE")
  void task1a_hit() {
    fx.rate("USD");
    fx.rate("USD");
    assertThat(fx.rate("USD")).isEqualByComparingTo("83.12");
    assertThat(upstream.calls()).withFailMessage("[task 1: cacheable] upstream called %s times; enable caching and add"
        + " @Cacheable(\"fxRates\")", upstream.calls()).isEqualTo(1);
  }

  @Test
  @DisplayName("Task 1: different currencies are different keys (USD, EUR, USD -> 2 calls)")
  void task1b_keys() {
    fx.rate("USD");
    fx.rate("EUR");
    fx.rate("USD");
    assertThat(upstream.calls()).withFailMessage("[task 1: keys] %s calls", upstream.calls()).isEqualTo(2);
  }

  @Test
  @DisplayName("Task 1: the value is really in the cache 'fxRates'")
  void task1c_inCache() {
    fx.rate("GBP");
    CacheManager cm = cacheManager.getIfAvailable();
    assertThat(cm).withFailMessage("[task 1: enable] no CacheManager: add @EnableCaching").isNotNull();
    assertThat(cm.getCache("fxRates").get("GBP", BigDecimal.class)).isEqualByComparingTo("105.30");
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: a closed month is built once, whatever the format flag")
  void task2a_key() {
    YearMonth last = YearMonth.now().minusMonths(1);
    statements.statement(1, last, "pdf");
    statements.statement(1, last, "csv");
    statements.statement(1, last, "pdf");
    assertThat(statements.builds()).withFailMessage("[task 2: key] built %s times", statements.builds())
        .isEqualTo(1);
  }

  @Test
  @DisplayName("Task 2: the CURRENT month is never cached (condition)")
  void task2b_condition() {
    YearMonth last = YearMonth.now().minusMonths(1);
    statements.statement(1, last, "pdf");
    statements.statement(1, last, "pdf");
    statements.statement(1, YearMonth.now(), "pdf");
    statements.statement(1, YearMonth.now(), "pdf");
    assertThat(statements.builds()).withFailMessage("[task 2: condition] built %s times (expected 1 + 2)",
        statements.builds()).isEqualTo(3);
  }

  @Test
  @DisplayName("Task 2: different accounts do not share a statement")
  void task2c_accounts() {
    YearMonth last = YearMonth.now().minusMonths(1);
    assertThat(statements.statement(1, last, "pdf").get(0)).startsWith("statement 1");
    assertThat(statements.statement(2, last, "pdf").get(0)).startsWith("statement 2");
    statements.statement(2, last, "pdf");
    assertThat(statements.builds()).withFailMessage("[task 2: key] built %s times", statements.builds())
        .isEqualTo(2);
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: override(USD, 84.00) replaces the cached value with no upstream call")
  void task3a_put() {
    fx.rate("USD");
    fx.override("USD", new BigDecimal("84.00"));
    assertThat(fx.rate("USD")).withFailMessage("[task 3: put] the cache still has the old rate")
        .isEqualByComparingTo("84.00");
    assertThat(upstream.calls()).isEqualTo(1);
  }

  @Test
  @DisplayName("Task 3: endOfDay() empties fxRates: the next USD and EUR go upstream again")
  void task3b_evict() {
    fx.rate("USD");
    fx.rate("EUR");
    fx.rate("USD");
    assertThat(upstream.calls()).withFailMessage("[task 3: needs task 1] caching is not on yet").isEqualTo(2);
    fx.endOfDay();
    fx.rate("USD");
    fx.rate("EUR");
    assertThat(upstream.calls()).withFailMessage("[task 3: evict] %s calls, expected 4", upstream.calls()).isEqualTo(4);
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: fxRates is a Caffeine cache that expires 10 minutes after write, max 500 entries")
  void task4a_caffeine() {
    fx.rate("USD");
    CacheManager cm = cacheManager.getIfAvailable();
    assertThat(cm).withFailMessage("[task 1: enable] no CacheManager").isNotNull();
    Object nativeCache = cm.getCache("fxRates").getNativeCache();
    assertThat(nativeCache).isInstanceOf(com.github.benmanes.caffeine.cache.Cache.class);
    var policy = ((com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache).policy();
    assertThat(policy.expireAfterWrite()).withFailMessage("[task 4: expiry] no expireAfterWrite: set"
        + " spring.cache.caffeine.spec").isPresent();
    assertThat(Duration.ofNanos(policy.expireAfterWrite().get().getExpiresAfter(TimeUnit.NANOSECONDS)))
        .isEqualTo(Duration.ofMinutes(10));
    assertThat(policy.eviction().orElseThrow().getMaximum()).isEqualTo(500);
  }

  // ---------- Example (green on the stub, stays green) ----------

  @Test
  @DisplayName("Example: self-invocation skips the cache: usdTwiceInternally() calls upstream twice")
  void task0_selfInvocation() {
    fx.usdTwiceInternally();
    assertThat(upstream.calls()).isEqualTo(2);
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
  @DisplayName("Task 5: the answer covers the key ideas (proxy, key, evict, expir, stale)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"proxy", "key", "evict", "expir|ttl", "stale"})
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
