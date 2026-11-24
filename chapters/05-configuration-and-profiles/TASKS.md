# Chapter 5 · Configuration and profiles

**Time:** ~70 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter05CheckTest` 25/25 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/05-configuration-and-profiles test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/05-configuration-and-profiles compile exec:java "-Dexec.mainClass=ledgerly.ConfigTour"`.
- Fresh stub: 0/25 green.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@Value`: `ledgerly.bank-name` (required), `ledgerly.support-phone` (default `1800-123-4567`) | `src/main/java/ledgerly/BranchInfo.java`, `src/main/resources/application.properties` | `mvn -pl chapters/05-configuration-and-profiles test -Popen` | task 1 checks green | ⬜ |
| 2 | Validated `@ConfigurationProperties("ledgerly.transfer")` + 3 keys + `@ConfigurationPropertiesScan` | `TransferProperties.java`, `application.properties`, `LedgerlyApplication.java` | same | task 2 checks green | ⬜ |
| 3 | `prod`: postgres URL + limit 100000 · `dev`: bank name `(DEV)` | `application-prod.properties`, `application-dev.properties` | same | task 3 checks green | ⬜ |
| 4 | `@Profile("prod")` real gateway, `@Profile("!prod")` fake | `RealSmsGateway.java`, `FakeSmsGateway.java` | same | task 4 checks green | ⬜ |
| 5 | `whereFrom(key)`: the winning property source | `EnvironmentReport.java` | same | task 5 checks green | ⬜ |
| 6 | **(Interview)** `@Value` vs `@ConfigurationProperties`, one jar for prod | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 6)

- When `@Value`, when `@ConfigurationProperties`? What does validation add?
- How does the same jar use another database in prod with no code change? (profiles + precedence)
- What does an active profile add, and what does it **not** do?

## ⚠️ Traps

- A `@Value` default hides a typo. Required keys get no default.
- Profile files add on top of the base. Change only what differs.
- `@Profile("dev")` on the fake leaves `uat` with no gateway. Use `"!prod"`.
- No `@ConfigurationPropertiesScan` → the record is not a bean.

## ❓ Checks (letters only)

1. `application.properties` has `ledgerly.transfer.daily-limit=200000`, `application-prod.properties` has `100000`, and you run with `--spring.profiles.active=prod --ledgerly.transfer.daily-limit=150000`. The limit is: A. 200000 · B. 100000 · C. 150000
2. With `prod` active, a key only in `application.properties` is: A. ignored · B. still used · C. an error
3. `@Value("${ledgerly.bank-nmae:Ledgerly}")` (typo) at startup: A. fails · B. starts with `Ledgerly` and nobody notices · C. starts with `null`

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "How do you run the same build in dev, UAT and prod?" is a standard interview question, and a wrong answer ("we change the file and rebuild") is a red flag. Banks also audit where settings come from.

**The concept.** Spring collects every property source into one ordered list (the Environment). A key's value comes from the first source that has it. A profile only adds sources (`application-prod.properties`) and beans (`@Profile("prod")`).

**Small example.** Sample data:

```properties
# application.properties                    # application-prod.properties
ledgerly.database.url=jdbc:h2:mem:ledgerly  ledgerly.database.url=jdbc:postgresql://db.ledgerly.internal:5432/ledgerly
ledgerly.transfer.daily-limit=200000        ledgerly.transfer.daily-limit=100000
ledgerly.transfer.fee-percent=1.0
```

Run with `prod` and `--ledgerly.transfer.daily-limit=150000`. Real output:

```
database url     jdbc:postgresql://db.ledgerly.internal:5432/ledgerly
transfer rules   TransferProperties[dailyLimit=150000, feePercent=1.0, otpValidity=PT5M]
daily-limit from commandLineArgs
```

| Key | Value used | Why |
|---|---|---|
| `database.url` | postgres | the prod file is above the base file |
| `daily-limit` | 150000 | the command line is above both files |
| `fee-percent` | 1.0 | only the base file has it, so it is inherited |
| `support-phone` | 1800-123-4567 | no source has it; the `@Value` default is used |

**Gotchas**
- Environment variables use relaxed names: `LEDGERLY_TRANSFER_DAILYLIMIT` sets `ledgerly.transfer.daily-limit`.
- If both `application.properties` and `application.yml` exist in the same folder, `.properties` wins for keys in both.
- Never put secrets (DB passwords) in committed files; pass them as environment variables.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **C.** The command line has the highest priority.
2. **B.** A profile adds a file on top; the base keys stay.
3. **B.** The default hides the typo. That is why required keys should have no default.

</details>
