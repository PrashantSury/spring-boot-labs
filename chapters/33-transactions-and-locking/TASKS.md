# Chapter 33 · Transactions and locking

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter33CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter33CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Isolation levels and propagation, in plain words | not written yet | — | checks not written yet | ⬜ |
| 2 | Optimistic locking with `@Version` | not written yet | — | checks not written yet | ⬜ |
| 3 | Pessimistic locking with `@Lock` | not written yet | — | checks not written yet | ⬜ |
| 4 | Retrying on `ObjectOptimisticLockingFailureException` | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Two withdrawals hit the same account at the same moment. Compare optimistic and pessimistic locking for this case.
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Read-then-write without a lock is a race
- `REQUIRES_NEW` commits even when the outer transaction rolls back

## ❓ Checks (letters only)

1. `@Version` detects a conflict: A. when reading · B. when writing (the `UPDATE ... WHERE version = ?` matches 0 rows)
2. High contention on one hot account favours: A. optimistic · B. pessimistic
3. `REQUIRES_NEW` inside a transaction that later rolls back: A. rolls back too · B. stays committed

<details><summary>📖 Teaching</summary>

**Why it matters.** Two transfers from the same account at the same moment must not both spend the same money.

**The concept.** Two transfers reading the same balance at once can both spend it. Optimistic locking (`@Version`) detects the clash at write time; pessimistic locking (`SELECT ... FOR UPDATE`) makes the second one wait.

**Small example.**

```java
@Entity class Account { @Version private Long version; private BigDecimal balance; ... }

@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select a from Account a where a.id = :id")
Optional<Account> findForUpdate(@Param("id") Long id);
```

| Case | Result | In or out, and why |
|---|---|---|
| Balance 1,000; two ₹800 withdrawals at once, no lock | both succeed, balance -600 ❌ | read-then-write race |
| Same with `@Version` | one commits, the other gets `ObjectOptimisticLockingFailureException` | the version changed underneath it; retry or 409 |
| Same with `findForUpdate` | the second waits, then sees 200 and is refused | the row lock serialises them |
| Inner `REQUIRES_NEW` audit write, outer transfer rolls back | the audit row stays committed | it is a separate transaction |

**Gotchas**
- Read-then-write without a lock is a race.
- `REQUIRES_NEW` commits even when the outer transaction rolls back.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** That is optimistic locking.
2. **B.** Many optimistic retries would waste work.
3. **B.** It ran in its own transaction.

</details>
