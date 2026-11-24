# Chapter 33 · Transactions and locking

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter33CheckTest` 13/13 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/33-transactions-and-locking test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/33-transactions-and-locking spring-boot:run` (10 parallel withdrawals, both ways). H2, no Docker.
- Fresh stub: 2/13 green ("InsufficientFunds is not retried" and "a successful withdrawal is audited once"; both must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@Version` + getter | `src/main/java/ledgerly/Account.java` | `mvn -pl chapters/33-transactions-and-locking test -Popen` | task 1 checks green | ⬜ |
| 2 | `findForUpdate` with `@Lock(PESSIMISTIC_WRITE)` | `AccountRepository.java` | same | task 2 checks green | ⬜ |
| 3 | retry up to 3 attempts | `WithdrawService.java` | same | task 3 checks green | ⬜ |
| 4 | `REQUIRES_NEW` audit | `AuditLog.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** locking, retry, propagation | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Lost update; optimistic vs pessimistic.
- When which; handling a conflict.
- Isolation, propagation, `REQUIRES_NEW`.

## ⚠️ Traps

- `@Transactional` alone ≠ no race.
- New transaction per retry.
- Don't retry business errors.

## ❓ Checks (letters only)

1. Two transactions read version 3 and both update. With `@Version`: A. both succeed · B. the second gets an optimistic locking exception
2. The busiest account gets 50 updates a second. Better: A. optimistic + retry · B. pessimistic lock
3. The outer transaction rolls back after calling a `REQUIRES_NEW` method that committed. Its work: A. is rolled back too · B. stays

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "How do you prevent two withdrawals from overdrawing an account?" is the banking interview question. The answer separates people who know `@Transactional` from people who know databases.

**The concept.** Think of one shared passbook. **Optimistic:** everyone copies the page number (version) when they read; when writing, you check the page number is unchanged, and if someone wrote in between you start again. **Pessimistic:** you take the passbook to your desk (`FOR UPDATE`); others wait in line until you hand it back. **Propagation** is "if I'm already holding a passbook, do I use it (`REQUIRED`) or open a separate one (`REQUIRES_NEW`)?" **Isolation** is how much of other people's unfinished writing you can see.

**Small example.** Account 1,000. Real results from the tests:

| Scenario | No lock | `@Version` | `FOR UPDATE` |
|---|---|---|---|
| Interest +50 lands between withdrawal's read and write | 900 (interest lost) | conflict → retry → 950 | interest waits → 950 |
| Save a stale copy after interest | overwrites | `ObjectOptimisticLockingFailureException` | n/a |
| 10 parallel withdrawals of 100 | random, often wrong | some fail after 3 tries, balance consistent | all 10 succeed, 0 |
| Failed withdrawal (insufficient funds) | rolled back | not retried | rolled back |
| ...and its audit row | `REQUIRED`: gone | | `REQUIRES_NEW`: stays |

**Gotchas**
- Hibernate bumps the version only when the row actually changes.
- H2 waits up to `LOCK_TIMEOUT` (10 s here) for a locked row, then throws.
- Spring Retry / Resilience4j can do the retry loop with backoff; the rule "new transaction per attempt" still applies.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** Its `WHERE version = 3` matches nothing.
2. **B.** With constant conflicts, optimistic retries keep failing; queueing is cheaper.
3. **B.** It committed independently.

</details>
