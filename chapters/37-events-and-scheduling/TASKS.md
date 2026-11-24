# Chapter 37 · Events and scheduling

**Time:** ~50 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter37CheckTest` 12/12 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/37-events-and-scheduling test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/37-events-and-scheduling spring-boot:run` (Ctrl+C to stop once scheduling is on). H2, no Docker.
- Fresh stub: 1/12 green (the transfer/rollback example; must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | publish `TransferCompleted` | `src/main/java/ledgerly/TransferService.java` | `mvn -pl chapters/37-events-and-scheduling test -Popen` | task 1 checks green | ⬜ |
| 2 | `@TransactionalEventListener(AFTER_COMMIT)` | `TransferNotifications.java` | same | task 2 checks green | ⬜ |
| 3 | `@EnableAsync` + `@Async` + `notify-` threads | `LedgerlyApplication.java`, `TransferNotifications.java`, `application.properties` | same | task 3 checks green | ⬜ |
| 4 | `@EnableScheduling`; cron + fixedDelay | `LedgerlyApplication.java`, `BankJobs.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** events, async, scheduling | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Events; why AFTER_COMMIT?
- `@Async` and its pool.
- `@Scheduled` on several instances.

## ⚠️ Traps

- Plain listener = inside the transaction.
- `@Async` and `@Scheduled` need their `@Enable...`.
- Every instance runs every schedule.

## ❓ Checks (letters only)

1. The ledger rolls back after the event was published. With a plain `@EventListener` the SMS: A. is sent anyway · B. is not sent
2. Which waits for the previous run to finish before counting? A. `fixedRate` · B. `fixedDelay`
3. Ledgerly runs 3 pods with a nightly `@Scheduled` interest job. Interest is posted: A. once · B. three times

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "How would you send a notification after a payment?" tests whether you know about transactions, events and async in one answer. "What happens to @Scheduled with several instances?" is the classic follow-up.

**The concept.** The transfer service rings a bell (publishes an event) and carries on; whoever cares listens. A plain listener jumps the moment the bell rings, even if the transfer is cancelled a second later. An AFTER_COMMIT listener waits until the bank stamps the transfer "done"; if it is torn up instead, the listener never hears about it. `@Async` hands the listener's work to a separate team (a thread pool), so the customer isn't kept at the counter. `@Scheduled` is the alarm clock for the nightly jobs, and every copy of the app has its own clock.

**Small example.** Real results from the tour, run after each task (SMS gateway = 300 ms):

| Situation | Events | SMS | `transfer()` time | Why |
|---|---|---|---|---|
| stub (nothing published) | 0 | none | ~30 ms | nobody rang the bell |
| task 1: publish + plain `@EventListener` | 1 | 1, on `main` | ~320 ms | the caller waits for the SMS |
| task 1, then a rejected 2,50,000 transfer | 1 | 1, wrong! | | the listener ran before the rollback |
| task 2: AFTER_COMMIT | 1 | none for the rejected one | ~320 ms | waits for the commit |
| task 3: + `@Async` | 1 | 1, on `notify-1` | ~25 ms | another thread sends it |

**Gotchas**
- `@TransactionalEventListener` outside any transaction does nothing unless `fallbackExecution = true`.
- Cron in Spring has 6 fields (seconds first): `0 0 1 * * *` = 01:00:00 every day. Always set `zone`.
- AFTER_COMMIT + `@Async` still loses the SMS if the app crashes right after the commit; the outbox pattern (write the message in the same transaction, send it later) fixes that.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **A.** It ran inside the transaction, before the rollback.
2. **B.** `fixedRate` counts from the previous *start*.
3. **B.** Each pod runs its own scheduler; use a lock.

</details>
