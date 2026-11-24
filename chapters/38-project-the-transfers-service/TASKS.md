# Chapter 38 · Project: the transfers service

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter38CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter38CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Design the endpoints and DTOs | not written yet | — | checks not written yet | ⬜ |
| 2 | Transfer logic with a transaction and a lock | not written yet | — | checks not written yet | ⬜ |
| 3 | Validation, error body and MockMvc tests | not written yet | — | checks not written yet | ⬜ |
| 4 | A short README your reviewer could run | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Walk through your transfers service design: endpoints, transaction boundary, locking, idempotency and the tests that prove each.
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Think about double-submit: same transfer twice
- Rolling back a debit when the credit fails

## ❓ Checks (letters only)

1. The client retries a timed-out transfer with the same idempotency key. You should: A. debit again · B. return the first result
2. Debit and credit belong in: A. two transactions · B. one transaction
3. Insufficient funds should be: A. 500 · B. a 4xx such as 409 or 422 with a clear message

<details><summary>📖 Teaching</summary>

**Why it matters.** Put it together: customers, accounts, a transfer with validation, errors and tests.

**The concept.** Build one small service end to end: endpoints and DTOs, a transfer in one transaction with a lock, validation, one error shape, and MockMvc tests.

**Small example.**

```text
POST /transfers  {"from":"0000123456","to":"0000654321","amount":500}
Header: Idempotency-Key: 3f1c...

Account 0000123456 balance 52,000 · Account 0000654321 balance 18,500
```

| Case | Result | In or out, and why |
|---|---|---|
| Valid request | 201; balances 51,500 and 19,000 | debit and credit in one transaction |
| Same request again with the same `Idempotency-Key` | the same 201 response, no second debit | double-submit is safe |
| `amount: -500` | 400 with violations | validation before any money moves |
| `amount: 60000` | 422 insufficient funds | a domain error, not 500 |
| The credit fails after the debit | both rolled back | one `@Transactional` service method |

**Gotchas**
- Think about double-submit: same transfer twice.
- Roll back the debit when the credit fails.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** The key identifies the operation.
2. **B.** All or nothing.
3. **B.** It's the client's request that can't be met.

</details>
