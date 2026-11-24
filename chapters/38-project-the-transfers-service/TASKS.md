# Chapter 38 · Project: the transfers service

**Time:** ~90 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter38CheckTest` 18/18 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/38-project-the-transfers-service test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/38-project-the-transfers-service spring-boot:run` (port 8099; curl examples in the README). H2, no Docker.
- Fresh stub: 1/18 green (`GET /accounts/ACC-1`; must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | validation + `@Valid` | `src/main/java/ledgerly/TransferRequest.java`, `TransferController.java` | `mvn -pl chapters/38-project-the-transfers-service test -Popen` | task 1 checks green | ⬜ |
| 2 | locked, all-or-nothing transfer | `TransferService.java` | same | task 2 checks green | ⬜ |
| 3 | idempotency key: replay / 409 | `TransferService.java` | same | task 3 checks green | ⬜ |
| 4 | 201 + `Location`, 200 replay, `GET /transfers/{id}` | `TransferController.java`, `TransferService.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** walk through your design | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Endpoint, DTOs, error statuses.
- Concurrency and partial failure.
- Double-submit; how you tested it.

## ⚠️ Traps

- Lock both accounts, in sorted order.
- Don't catch the credit failure inside the transaction.
- Idempotency = client key, not "same amount".

## ❓ Checks (letters only)

1. The credit to ACC-3 throws after ACC-1 was debited, inside one `@Transactional` method. ACC-1 ends at: A. 1000.00 · B. 990.00
2. Same `Idempotency-Key`, same body, sent twice. The second answer: A. 201, a new transfer · B. 200, the original transfer
3. Transfer A→B locks A then B; B→A locks B then A. Under load this can: A. deadlock · B. never deadlock

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** This is the project an interviewer can poke at for 30 minutes. Each check below is a question they could ask.

**The concept.** A transfer is a sealed envelope: everything inside (debit, credit, record) happens together or not at all; that's the transaction. The lock is a "busy" sign on each account's ledger page; everyone who needs the same page waits. If two clerks grab pages in a different order, each can end up holding the page the other needs (deadlock), so everyone grabs them in account-number order. The idempotency key is the cheque number: present the same cheque twice and the bank pays it once.

**Small example.** Real results (tests and curl, start: ACC-1 1000, ACC-2 500, ACC-3 0 frozen):

| Request | Status | After | Why |
|---|---|---|---|
| 100 ACC-1→ACC-2, key `pay-1` | 201, `Location: /transfers/1` | 900 / 600 | booked |
| same again, key `pay-1` | 200, id 1 | 900 / 600 | replay, no debit |
| key `pay-1`, amount 999 | 409 | unchanged | key reused |
| ACC-1→ACC-1, amount -5 | 400, `differentAccounts`, `amount` errors | unchanged | validation |
| 5000 ACC-1→ACC-2 | 422 `insufficient funds in ACC-1` | unchanged | debit refused |
| 10 ACC-1→ACC-3 | 422 `account ACC-3 is frozen…` | ACC-1 still 1000 | rolled back |
| 10 × 100 in parallel | all OK | ACC-1 0, ACC-2 1500 | locks queue them |
| 5 × 10 one way + 5 × 20 back, in parallel | all OK | 1050 / 450 | sorted locks, no deadlock |

**Gotchas**
- Two *first* requests with the same key can race past the lookup; the unique constraint on `idempotency_key` stops the second one.
- Return DTOs, never entities: a lazy `Customer` outside the transaction is an exception waiting to happen.
- `BigDecimal`: compare with `compareTo`, not `equals` (100 vs 100.00).

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **A.** The exception rolls back the whole transaction.
2. **B.** The client gets the same answer as the first time.
3. **A.** Lock in a fixed order to avoid it.

</details>
