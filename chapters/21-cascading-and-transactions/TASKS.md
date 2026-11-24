# Chapter 21 · Cascading and transactions

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter21CheckTest` 13/13 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/21-cascading-and-transactions test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/21-cascading-and-transactions spring-boot:run` (H2 in memory, no Docker; DEBUG log shows every commit/rollback).
- Fresh stub: 3/13 green (the happy-path transfers and the batch's failed list; all must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `cascade = ALL`, `orphanRemoval = true` on payees | `src/main/java/ledgerly/Customer.java` | `mvn -pl chapters/21-cascading-and-transactions test -Popen` | task 1 checks green | ⬜ |
| 2 | `@Transactional` on `transfer` | `TransferService.java` | same | task 2 checks green | ⬜ |
| 3 | `rollbackFor = DailyLimitExceededException.class` | `TransferService.java` | same | task 3 checks green | ⬜ |
| 4 | `PaymentWorker.pay` `@Transactional`; `payAll` calls `worker.pay` | `PaymentWorker.java`, `TransferService.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** cascade, rollback rules, self-invocation | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Cascade and `orphanRemoval`?
- `@Transactional` for a transfer; which exceptions roll back?
- Why does a same-class call skip it; the fix?

## ⚠️ Traps

- Checked exceptions commit by default.
- Self-invocation and `private` methods skip the proxy.
- Swallowing the exception = commit.

## ❓ Checks (letters only)

1. `@Transactional` method debits, then throws `IOException` (checked). The debit is: A. rolled back · B. committed
2. `payAll` calls `this.payOne(p)`, and `payOne` is `@Transactional`. `payOne` runs: A. in a new transaction · B. with no transaction
3. A payee is removed from `customer.getPayees()` with `orphanRemoval = true`. At commit the row is: A. deleted · B. kept with `customer_id` null

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** Atomic money movement is the first thing any bank interview asks about with Spring, and "why didn't my `@Transactional` roll back?" (checked exception or self-invocation) is the follow-up that catches most candidates.

**The concept.** `@Transactional` makes Spring wrap the bean in a proxy. A call through the proxy opens a transaction, runs the method, and commits, or rolls back if a `RuntimeException`/`Error` escapes. Repository calls inside join the same transaction.

**Small example.** Sample data: ACC-1 = ₹10,000, ACC-2 = ₹500. Batch: `[1000 → ACC-2, 2000 → ACC-9, 500 → ACC-2]`.

| Version | ACC-1 after | ACC-2 after | Why |
|---|---|---|---|
| `this.payOne` (stub) | 6500 ❌ | 2000 | the bad payment's 2000 debit committed on its own; no proxy, no transaction |
| `worker.pay` (finished) | 8500 ✅ | 2000 | each payment is its own transaction; the bad one rolled back fully |
| one `@Transactional` around the whole batch | 10000 | 500 | all-or-nothing for the batch: one bad row undoes the salaries of everyone else, not what payroll wants |

**Gotchas**
- `readOnly = true` is a hint for queries (Hibernate skips dirty checking); use it on report methods.
- `@Transactional` from `org.springframework.transaction.annotation` and `jakarta.transaction` both work, but only Spring's has `rollbackFor`/`readOnly`.
- `orphanRemoval` only makes sense on a true parent-child relationship.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** Checked exceptions commit unless listed in `rollbackFor`.
2. **B.** The call never passes the proxy.
3. **A.** The orphan is deleted.

</details>
