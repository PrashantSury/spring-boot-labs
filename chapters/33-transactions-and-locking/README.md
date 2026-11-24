# Chapter 33: Transactions and locking

By the end of this chapter two things touching Asha's account at the same moment can no longer cost the bank money. Picture a withdrawal and the nightly interest credit: either the second one notices the first (optimistic locking) or it waits for it (pessimistic locking). And the regulator's audit trail keeps every attempt, even the ones that roll back.

## Why you need this

Asha has 1,000. At 00:00:00.010 the interest job reads 1,000 and plans to write 1,050. At 00:00:00.012 her ATM withdrawal reads 1,000 and plans to write 900. Both commit. The balance is 900 or 1,050, depending on who was last, and either way the bank's books are wrong. This is a **lost update**, and `@Transactional` alone does not prevent it.

## What you will learn

- **Isolation and propagation** in plain words: what a transaction sees, and what happens when one calls another.
- **Optimistic locking** with `@Version`, and `ObjectOptimisticLockingFailureException`.
- **Pessimistic locking** with `@Lock(PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`).
- **Retrying** a conflict in a fresh transaction, and not retrying business errors.
- **`REQUIRES_NEW`** for an audit entry that must survive a rollback.

## Real output

`mvn -q -pl chapters/33-transactions-and-locking spring-boot:run` runs 10 parallel withdrawals of 100 from 1,000, done two ways (finished code, two runs):

```
optimistic + retry: 4 ok, 6 failed, final balance 600.00, version 4
pessimistic lock   : 10 ok, 0 failed, final balance 0.00, version 10

optimistic + retry: 5 ok, 5 failed, final balance 500.00, version 5
pessimistic lock   : 10 ok, 0 failed, final balance 0.00, version 10
```

Both are **correct**: balance = 1,000 − 100 × successes, never a lost update. Under heavy contention on one row, optimistic locking gives up after 3 tries, while pessimistic locking makes everyone queue. The exact numbers change from run to run.

## The shape of it

```java
@Version private Long version;                       // UPDATE ... WHERE id = ? AND version = ?

@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select a from Account a where a.id = :id")
Optional<Account> findForUpdate(@Param("id") long id);

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void record(String message) { ... }
```

## Try it

```bash
mvn -q -pl chapters/33-transactions-and-locking spring-boot:run
mvn -pl chapters/33-transactions-and-locking test -Popen
```

H2 in memory: no Docker needed.

## Your turn

You edit `Account.java`, `AccountRepository.java`, `WithdrawService.java`, `AuditLog.java` and `interview-answer.md`. The worker, interest job and tour are finished.

1. **`@Version`.** Plus the `getVersion()` getter.
2. **Pessimistic.** `findForUpdate` with `@Lock(PESSIMISTIC_WRITE)`.
3. **Retry.** Up to 3 attempts on `ObjectOptimisticLockingFailureException`; never on `InsufficientFunds`.
4. **`REQUIRES_NEW`.** The audit entry commits on its own.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- Read-then-write without a version or a lock is a race, even inside `@Transactional`.
- Retrying inside the same transaction: the persistence context still holds the stale row. Each attempt needs a new transaction (here: a call to another bean).
- Making `WithdrawService.withdrawWithRetry` `@Transactional` puts all attempts in one transaction and breaks the retry.
- `REQUIRES_NEW` takes a second database connection while the first is held; with a tiny pool, that can deadlock.
- Test hooks and counters on a proxied bean must be methods, not fields: the proxy has its own copy of each field.

**Interview question (task 5).** What is a lost update; how do optimistic and pessimistic locking prevent it? When choose which, and how handle a conflict? Isolation and propagation, especially `REQUIRES_NEW`. The questions are repeated at the top of `interview-answer.md`.
