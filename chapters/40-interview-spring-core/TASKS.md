# Chapter 40 · Interview: Spring core

**Time:** ~60 min + 20 min answers · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter40CheckTest` 13/13 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/40-interview-spring-core test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/40-interview-spring-core spring-boot:run` (add `-Dspring-boot.run.profiles=prod` for task 3). H2, no Docker.
- Fresh stub: 2/13 green: the transfer example and "the default backs off" (true on the stub because there is no default yet). Both must stay green.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | fresh prototype per call (`ObjectProvider`) | `src/main/java/ledgerly/StatementPrinter.java` | `mvn -pl chapters/40-interview-spring-core test -Popen` | task 1 checks green | ⬜ |
| 2 | auto-configuration with `@ConditionalOnMissingBean` + imports file | `src/main/java/dev/ledgerly/fx/FxAutoConfiguration.java`, `src/main/resources/META-INF/spring/...AutoConfiguration.imports` | same | task 2 checks green | ⬜ |
| 3 | `@ConfigurationProperties` + defaults + prod profile | `LimitsProperties.java`, `LedgerlyApplication.java`, `application-prod.properties` | same | task 3 checks green | ⬜ |
| 4 | audit survives rollback (`REQUIRES_NEW` in its own bean) | `AuditWriter.java`, `TransferService.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** four spoken answers, bank example first | `interview-answer.md` | graded by Lumi, then aloud 60–90 s each | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Scopes; a prototype in a singleton.
- Auto-configuration: how it decides; overriding.
- Profiles + `@ConfigurationProperties`.
- 90 s: `@Transactional` in a transfer + two silent failures.

## ⚠️ Traps

- Example first, then what goes wrong, then the fix.
- Self-invocation skips the proxy.
- Checked exceptions commit by default.

## ❓ Checks (letters only)

1. A strong answer starts with: A. a textbook definition · B. the default behaviour plus a concrete example
2. Two ways `@Transactional` silently doesn't apply: A. self-invocation; a checked exception doesn't roll back · B. a public method; an unchecked exception
3. The app defines its own `FxRates` bean and the starter has `@ConditionalOnMissingBean FxRates`. Beans of type `FxRates`: A. 1 · B. 2

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** The Spring core round is where "I've used Spring" turns into "I understand Spring". Every question below has a follow-up that asks what goes wrong.

**The concept.** Spring is a factory with a parts list. Scopes say how many of each part it makes (one shared, or one per request for it). Auto-configuration is the factory's "default fittings" sheet: each default is fitted only if its conditions hold, for example "only if you didn't bring your own". Properties and profiles are the settings dial, with a different preset for prod. Proxies are the wrappers the factory puts around parts to add transactions, and a part talking to itself never goes through its wrapper.

**Small example.** Real results (tour and tests):

| Topic | Stub (bug) | Finished | The sentence for the interview |
|---|---|---|---|
| scopes | 2nd statement `#1: rent … \| salary … \| fd interest 1200` | `#2: fd interest 1200` | a prototype in a singleton's constructor is created once |
| auto-config | `FxRates = none` | `FixedFxRates`; with your own bean, yours wins | defaults back off with `@ConditionalOnMissingBean` |
| properties | `limits null` | 50000 / 20; prod 200000 / 20 | the profile file overrides only the keys it sets |
| transactions | 75000 fails → audit rows 0 | audit rows 1 | `REQUIRES_NEW` needs a call through the proxy |

**Gotchas**
- `@SpringBootApplication` = `@SpringBootConfiguration` + `@EnableAutoConfiguration` + `@ComponentScan` (of its own package and below).
- `--debug` (or `debug=true`) prints the condition evaluation report: why each auto-configuration did or didn't apply.
- Property precedence, highest first: command line, environment variables, `application-{profile}.properties`, `application.properties`, `@DefaultValue`.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** Behaviour plus an example beats a definition.
2. **A.** Both are silent: no error, just no transaction behaviour.
3. **A.** The default backs off.

</details>
