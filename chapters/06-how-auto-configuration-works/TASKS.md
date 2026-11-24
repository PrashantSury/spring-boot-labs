# Chapter 6 · How auto-configuration works

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter06CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter06CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@Conditional` annotations: `OnClass`, `OnMissingBean`, `OnProperty` | not written yet | — | checks not written yet | ⬜ |
| 2 | Reading the `--debug` conditions report | not written yet | — | checks not written yet | ⬜ |
| 3 | Writing your own tiny auto-configuration and starter-style bean | not written yet | — | checks not written yet | ⬜ |
| 4 | How to back off a default by defining your own bean | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- How does Spring Boot know to create a `DataSource`? Answer from the mechanism: the imports file, the conditions, and backing off.
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Your own bean of the same type makes the default back off, which can surprise you
- Auto-configuration classes are not found by component scan

## ❓ Checks (letters only)

1. You define your own `DataSource` bean. Boot's default `DataSource`: A. is created too, and startup fails · B. backs off · C. replaces yours
2. Fastest way to see why an auto-configuration did not apply: A. read the Boot source · B. run with `--debug` and read the conditions report
3. How are auto-configuration classes found? A. component scan · B. the `AutoConfiguration.imports` file in each jar

<details><summary>📖 Teaching</summary>

**Why it matters.** Interviewers ask "how does Spring Boot know to create a DataSource?" and you should answer from the mechanism, not from memory of a slide.

**The concept.** Spring Boot ships auto-configuration classes listed in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Each one applies only if its `@Conditional...` checks pass, and most of its beans back off when you define your own.

**Small example.**

```java
@AutoConfiguration
@ConditionalOnClass(name = "com.ledgerly.sms.SmsClient")
public class SmsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ledgerly.sms", name = "enabled", havingValue = "true")
    SmsClient smsClient() { return new SmsClient("default-gateway"); }
}
```

| Case | Result | In or out, and why |
|---|---|---|
| Class on the classpath, `ledgerly.sms.enabled=true`, no bean of yours | the default `SmsClient` | all three conditions pass |
| You declare your own `@Bean SmsClient` | your bean only | `@ConditionalOnMissingBean` backs off |
| `ledgerly.sms.enabled` not set | no `SmsClient` bean | `@ConditionalOnProperty` fails |
| `SmsClient` class not on the classpath | the whole class is skipped | `@ConditionalOnClass` fails first |
| Run with `--debug` | `CONDITIONS EVALUATION REPORT` with `Positive matches` and `Negative matches` | the report says which condition passed or failed |

**Gotchas**
- Your own bean of the same type makes the default back off, sometimes with settings you didn't expect.
- Auto-configuration classes are not found by component scan; they must be listed in the imports file.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** It is guarded by a missing-bean condition.
2. **B.** Negative matches name the failed condition.
3. **B.** Scan only covers your packages.

</details>
