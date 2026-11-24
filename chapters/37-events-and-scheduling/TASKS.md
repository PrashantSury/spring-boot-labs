# Chapter 37 · Events and scheduling

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter37CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter37CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `ApplicationEventPublisher` and `@EventListener` | not written yet | — | checks not written yet | ⬜ |
| 2 | `@TransactionalEventListener(AFTER_COMMIT)` | not written yet | — | checks not written yet | ⬜ |
| 3 | `@Scheduled` with fixedRate and cron | not written yet | — | checks not written yet | ⬜ |
| 4 | `@Async` and its thread pool | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- How do you send a notification only after a transfer commits, and post interest nightly exactly once across several instances?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- A plain listener runs inside the publisher's transaction
- `@Scheduled` on many instances runs on every instance

## ❓ Checks (letters only)

1. The transfer rolls back. An `AFTER_COMMIT` listener: A. runs · B. does not run
2. `@Scheduled` on 3 instances runs: A. once in total · B. on each instance
3. A plain `@EventListener` runs: A. after commit · B. synchronously, inside the publisher's transaction

<details><summary>📖 Teaching</summary>

**Why it matters.** After a transfer commits, send a notification; every night, post interest. Neither should slow the request.

**The concept.** Publish an event after something happens and let listeners react. `@TransactionalEventListener(AFTER_COMMIT)` waits until the transfer has really committed; `@Scheduled` runs jobs on a timer.

**Small example.**

```java
record TransferCompleted(Long transferId, String toPhone) {}

@Transactional
public void transfer(...) { ...; events.publishEvent(new TransferCompleted(id, phone)); }

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
void notify(TransferCompleted e) { sms.send(e.toPhone(), "Transfer " + e.transferId() + " done"); }

@Scheduled(cron = "0 0 1 * * *", zone = "Asia/Kolkata")   // 01:00 IST daily
void postInterest() { ... }
```

| Case | Result | In or out, and why |
|---|---|---|
| Transfer commits | SMS sent once, after commit | AFTER_COMMIT |
| Transfer rolls back | no SMS | the listener never fires |
| Plain `@EventListener` instead | SMS sent inside the transaction, even if it later rolls back ❌ | a plain listener runs in the publisher's transaction |
| Three app instances with `@Scheduled` interest posting | interest posted 3 times ❌ | each instance runs the schedule; use a lock such as ShedLock |
| `@Async` on `notify` (with `@EnableAsync`) | the request doesn't wait for the SMS | runs on a task executor thread |

**Gotchas**
- A plain listener runs inside the publisher's transaction.
- `@Scheduled` on many instances runs on every instance.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** There was no commit.
2. **B.** You need a distributed lock.
3. **B.** Unless it is made async or transactional.

</details>
