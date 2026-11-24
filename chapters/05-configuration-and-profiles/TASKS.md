# Chapter 5 · Configuration and profiles

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter05CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter05CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `application.properties` and `application.yml`, and the order Spring reads them | not written yet | — | checks not written yet | ⬜ |
| 2 | `@Value` vs `@ConfigurationProperties` with a validated properties record | not written yet | — | checks not written yet | ⬜ |
| 3 | Spring profiles: `dev`, `uat`, `prod`, and `@Profile` on beans | not written yet | — | checks not written yet | ⬜ |
| 4 | Property precedence: environment variables, command line, files | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- When do you use `@Value` and when `@ConfigurationProperties`? How would the same jar use a different database in prod without a code change?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- A typo in a property name is silently ignored with `@Value` defaults
- An active profile replaces nothing by itself; it only adds files and beans

## ❓ Checks (letters only)

1. `@Value("${ledgerly.fee.minmum:10}")` has a typo in the key. What happens? A. startup fails · B. the value is silently 10 · C. it is `null`
2. `application-prod.yml` says 25, env `LEDGERLY_FEES_MINIMUM` says 50, profile `prod` is active. Result: A. 25 · B. 50 · C. startup fails
3. Activating profile `prod`: A. replaces `application.yml` · B. adds `application-prod.yml` and `@Profile("prod")` beans on top

<details><summary>📖 Teaching</summary>

**Why it matters.** The same Ledgerly jar must run with a test database on a laptop and the real one in production, with no code change.

**The concept.** Settings live outside the code; Spring reads them from files, environment variables and the command line, and the more specific source wins. A profile adds `application-<profile>.yml` and `@Profile` beans on top of the defaults.

**Small example.**

```java
@Validated
@ConfigurationProperties(prefix = "ledgerly.fees")
record FeeProps(@Min(1) long minimum) {}
// enabled with @ConfigurationPropertiesScan on the application class

// application.yml        ->  ledgerly.fees.minimum: 10
// application-prod.yml   ->  ledgerly.fees.minimum: 25
```

| Case | Result | In or out, and why |
|---|---|---|
| No profile | `minimum` = 10 | only `application.yml` is read |
| `--spring.profiles.active=prod` | `minimum` = 25 | the profile file is added on top and overrides |
| `prod` + env `LEDGERLY_FEES_MINIMUM=50` | `minimum` = 50 | environment variables beat files (relaxed binding maps the name) |
| `prod` + env 50 + `--ledgerly.fees.minimum=70` | `minimum` = 70 | command-line arguments beat environment variables |
| `ledgerly.fees.minimum: 0` in the file | startup fails with a binding validation error | `@Validated` + `@Min(1)` reject it early |

**Gotchas**
- `@Value("${ledgerly.fee.minmum:10}")` with a typo silently uses the default 10. `@ConfigurationProperties` with validation fails loudly instead.
- Activating a profile removes nothing: `application.yml` still loads first.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** The default hides the typo; that is why typed, validated properties are safer.
2. **B.** Environment variables outrank config files.
3. **B.** Profiles only add; defaults still load.

</details>
