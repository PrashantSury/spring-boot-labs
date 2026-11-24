# Chapter 21 · Cascading and transactions

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter21CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter21CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Cascade types and `orphanRemoval` | not written yet | — | checks not written yet | ⬜ |
| 2 | `@Transactional` basics: commit, rollback, read-only | not written yet | — | checks not written yet | ⬜ |
| 3 | Checked vs unchecked exceptions and rollback rules | not written yet | — | checks not written yet | ⬜ |
| 4 | Where to put `@Transactional` (service, not controller) | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- A transfer debits and then the credit fails with a checked exception. What happens by default, and how do you fix it?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Checked exceptions do not roll back by default
- Self-invocation skips the transaction proxy

## ❓ Checks (letters only)

1. A checked exception leaves a `@Transactional` method. Default outcome: A. rollback · B. commit
2. `this.transfer(...)` from the same class: A. runs in a transaction · B. skips the proxy, so no transaction
3. Where should `@Transactional` usually go? A. controller · B. service

<details><summary>📖 Teaching</summary>

**Why it matters.** Closing a customer must delete or keep their accounts consistently, and a transfer must not half-succeed.

**The concept.** `@Transactional` on a service method makes all its database work commit together or roll back together. By default only unchecked exceptions roll back.

**Small example.**

```java
@Transactional
public void transfer(String from, String to, BigDecimal amount) {
    debit(from, amount);      // UPDATE account SET balance = balance - ?
    credit(to, amount);       // throws if `to` is closed
}
```

| Case | Result | In or out, and why |
|---|---|---|
| `credit` throws `IllegalStateException` (unchecked) | debit rolled back, balances unchanged ✅ | unchecked → rollback |
| `credit` throws `AccountClosedException extends Exception` (checked) | debit committed, money gone ❌ | checked exceptions commit by default |
| Same with `@Transactional(rollbackFor = Exception.class)` | rolled back ✅ | you widened the rule |
| `transfer` called from another method in the same class | no transaction at all ❌ | self-invocation skips the proxy |
| `orphanRemoval = true`, remove an account from `customer.accounts` | the account row is deleted | orphans go with the parent's collection |

**Gotchas**
- Checked exceptions do not roll back by default.
- Self-invocation skips the transaction proxy.
- Put `@Transactional` on services, not controllers.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Only runtime exceptions and errors roll back by default.
2. **B.** The call never goes through the proxy.
3. **B.** One business operation is one unit of work.

</details>
