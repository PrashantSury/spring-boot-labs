# Chapter 37: Events and scheduling

By the end of this chapter a Ledgerly transfer sends its "money received" SMS only after the transfer is really committed, without making the customer wait for the SMS gateway, and interest is posted by a nightly job.

## Why you need this

Two complaints reached the bank in one week. A customer got "You received ₹2,50,000", but the ledger had rejected the transfer a moment later and rolled it back. And the transfer button got slow, because every transfer waited 300 ms for the SMS gateway. On top of that, the team still posts interest by hand every night.

## What you will learn

- **Application events:** `ApplicationEventPublisher.publishEvent(...)` and `@EventListener`; the publisher no longer knows who listens.
- **`@TransactionalEventListener(phase = AFTER_COMMIT)`:** react only to work that really committed.
- **`@Async`** with `@EnableAsync` and a named thread pool.
- **`@Scheduled`:** `cron` with a zone, `fixedRate` vs `fixedDelay`, `@EnableScheduling`.
- **The multi-instance trap:** three pods mean the nightly job runs three times.

## Real output

`mvn -q -pl chapters/37-events-and-scheduling spring-boot:run` (finished code). It books 500, tries 2,50,000 (the ledger rejects it after the event was published), waits a moment, then lists what happened:

```
transfer 500      : returned in 25 ms
transfer 250000   : ledger rejected the posting (rolled back)
rows in transfers : 1
events published  : 2
SMS sent          : [Sms[text=ACC-2 received 500 from ACC-1, thread=notify-1]]
scheduled job     : ledgerly.BankJobs.sweepOutbox -> fixedDelay PT30S
scheduled job     : ledgerly.BankJobs.postInterest -> cron 0 0 1 * * *
(the scheduler keeps the app running for those jobs; press Ctrl+C to stop)
```

Two events were published but only one SMS went out: the rolled-back transfer never reached its listener. On the stub, nothing is published, no SMS is sent and there are no scheduled jobs:

```
events published  : 0
SMS sent          : []
scheduled jobs    : none
```

## The shape of it

```java
events.publishEvent(new TransferCompleted(id, from, to, amount));     // in the @Transactional service

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Async
public void on(TransferCompleted e) { notifier.send(...); }

@Scheduled(cron = "0 0 1 * * *", zone = "Asia/Kolkata")
public void postInterest() { ... }
```

## Try it

```bash
mvn -q -pl chapters/37-events-and-scheduling spring-boot:run
mvn -pl chapters/37-events-and-scheduling test -Popen
```

H2 in memory; no Docker. The tests take about 10 seconds because they wait for SMS that should (or should not) arrive.

## Your turn

You edit `TransferService.java`, `TransferNotifications.java`, `LedgerlyApplication.java`, `BankJobs.java`, `application.properties` and `interview-answer.md`.

1. **Publish.** `publishEvent(new TransferCompleted(...))` in `transfer`.
2. **After commit.** Replace `@EventListener` with `@TransactionalEventListener(phase = AFTER_COMMIT)`.
3. **Async.** `@EnableAsync`, `@Async` on the listener, threads named `notify-`.
4. **Schedule.** `@EnableScheduling`; `postInterest` on cron `0 0 1 * * *` (Asia/Kolkata); `sweepOutbox` with `fixedDelay` 30 s.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- A plain `@EventListener` runs inside the publisher's transaction, before the commit decision.
- An AFTER_COMMIT listener that writes to the database needs its own transaction (`REQUIRES_NEW`); the old one is finished.
- `@Async` exceptions never reach the caller; log them or use an `AsyncUncaughtExceptionHandler`.
- `fixedRate` starts runs on a clock even if the last one is still running; `fixedDelay` waits for it to finish.
- Every instance runs every `@Scheduled` method: use a lock (ShedLock, a DB row) for jobs that must run once.

**Interview question (task 5).** Events and AFTER_COMMIT listeners. `@Async` and its pool. `@Scheduled` and several instances. The questions are repeated at the top of `interview-answer.md`.
