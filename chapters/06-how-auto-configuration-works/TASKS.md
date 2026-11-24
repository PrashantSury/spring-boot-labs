# Chapter 6 · How auto-configuration works

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter06CheckTest` 20/20 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/06-how-auto-configuration-works test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/06-how-auto-configuration-works compile exec:java "-Dexec.mainClass=ledgerly.app.AutoConfigTour"`.
- Fresh stub: 6/20 green (guards and defaults that must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@AutoConfiguration` + one line in the imports file | `src/main/java/ledgerly/autoconfigure/LedgerlyAuditAutoConfiguration.java`, `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` | `mvn -pl chapters/06-how-auto-configuration-works test -Popen` | task 1 checks green | ⬜ |
| 2 | `@ConditionalOnMissingBean(AuditSink.class)` on `auditSink()` | `LedgerlyAuditAutoConfiguration.java` | same | task 2 checks green | ⬜ |
| 3 | `@ConditionalOnProperty` `ledgerly.audit.enabled`, on if missing | `LedgerlyAuditAutoConfiguration.java` | same | task 3 checks green | ⬜ |
| 4 | `@ConditionalOnClass(name = "ledgerly.siem.SiemClient")` on `siemForwarder()` | `LedgerlyAuditAutoConfiguration.java` | same | task 4 checks green | ⬜ |
| 5 | `outcome(ctx, source)` from `ConditionEvaluationReport` | `src/main/java/ledgerly/app/ConditionsReader.java` | same | task 5 checks green | ⬜ |
| 6 | **(Interview)** how Boot creates a `DataSource` | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 6)

- Where does Boot find auto-configuration classes? (not component scan)
- Which conditions decide the `DataSource`? Name two.
- Your own `DataSource` bean: what happens, and how do you check why a default did or didn't apply?

## ⚠️ Traps

- No `@Import`/`@ComponentScan` on the app: the imports file only.
- `@ConditionalOnMissingBean` on the **method**, not the class.
- `matchIfMissing = true`.
- `@ConditionalOnClass(name = "...")` as a String.

## ❓ Checks (letters only)

1. You add your own `AuditSink` bean. The starter's `auditSink()`: A. is created too, and startup fails with "found 2" · B. backs off · C. replaces yours
2. Auto-configuration classes are found by: A. component scanning from the main class · B. the `AutoConfiguration.imports` files · C. `application.properties`
3. The config class "did not match". Its `@Bean` methods are: A. matched · B. did not match · C. not evaluated

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "How does Spring Boot auto-configure a DataSource?" separates people who used Boot from people who understand it. The same mechanism explains every surprise like "why did my Jackson settings stop working?" (you defined an `ObjectMapper` and the default backed off).

**The concept.** `@EnableAutoConfiguration` loads every class listed in the `AutoConfiguration.imports` files. Each class and `@Bean` method has conditions; a bean is created only when all of them match. User beans are registered first, so `@ConditionalOnMissingBean` sees them and backs off.

**Small example.** Sample setups for this chapter's starter:

| Setup | `AuditSink` | `SiemForwarder` | Why |
|---|---|---|---|
| nothing set | `ConsoleAuditSink` | yes | key missing → on; SIEM class present |
| `ledgerly.audit.enabled=false` | none | none | the class did not match, nothing inside runs |
| app defines `bankAuditSink` | `bankAuditSink` only | yes | only `auditSink()` backs off |
| `ledgerly.siem` jar missing | `ConsoleAuditSink` | none | `@ConditionalOnClass` failed |

Real output, switched off:

```
config class         did not match
auditSink method     not evaluated
```

**Gotchas**
- Backing off is all or nothing: your own `DataSource` means none of Boot's pool settings apply.
- `@ConditionalOnMissingBean` in your **own** `@Configuration` is unreliable: ordering between user configs is not guaranteed. It is meant for auto-configurations.
- `--debug` prints "Positive matches" and "Negative matches". Search the report for the class name.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** `@ConditionalOnMissingBean` sees your bean first and the default is skipped.
2. **B.** Boot reads the imports files; scanning does not find them.
3. **C.** When the class fails, Boot never looks at its methods.

</details>
