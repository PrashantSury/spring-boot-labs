# Chapter 38: Project: the transfers service

By the end of this chapter you will have built the core of a real bank API: `POST /transfers` that validates its input, moves money all-or-nothing, survives parallel requests and double-clicks, and answers every failure with a clear problem+json body. It brings chapters 9–33 together in one small service.

## Why you need this

This is the project you talk about in interviews. "Design a money transfer endpoint" is one of the most common backend exercises, because it touches everything: REST design, validation, errors, transactions, locking and idempotency. The interviewer will push on the edge cases: what if the credit fails after the debit? What if the customer double-clicks "Send"? What if two transfers hit the same account at the same moment?

## What you will learn

- **Endpoint design:** request DTO, response DTO, `201 Created` + `Location`, the status for every failure.
- **Validation** with field errors, including a cross-field rule (`from` ≠ `to`).
- **All-or-nothing transfers:** one `@Transactional` method; an exception after the debit rolls the debit back.
- **Locking in a fixed order:** `SELECT ... FOR UPDATE` on both accounts, sorted, so opposite transfers can't deadlock.
- **Idempotency keys:** the same request twice is booked once; a reused key with a different request is a 409.
- **MockMvc tests** for the happy path *and* every unhappy one, plus real parallel transfers.

## The API

| Call | Success | Failures |
|---|---|---|
| `GET /accounts/{number}` | 200 `AccountDto` | 404 |
| `POST /transfers` (header `Idempotency-Key`) | 201 + `Location` (new), 200 (replay) | 400 invalid, 404 unknown account, 422 insufficient funds / frozen, 409 key reused |
| `GET /transfers/{id}` | 200 `TransferDto` | 404 |

Seed data: Asha has ACC-1 (1000.00); Ravi has ACC-2 (500.00) and ACC-3 (0.00, frozen: it can't receive money).

## Real output

The finished app on port 8099:

```
$ curl -s localhost:8099/accounts/ACC-1
{"number":"ACC-1","owner":"Asha","balance":1000.00}
$ curl -i -X POST localhost:8099/transfers -H Idempotency-Key:pay-1 -H Content-Type:application/json -d '{"from":"ACC-1","to":"ACC-2","amount":100}'
HTTP/1.1 201
Location: /transfers/1
{"id":1,"from":"ACC-1","to":"ACC-2","amount":100.00}
$ (the same request again)
HTTP/1.1 200
{"id":1,"from":"ACC-1","to":"ACC-2","amount":100.00}
$ (same key, amount 999)
{"detail":"Idempotency-Key pay-1 was already used for a different transfer","instance":"/transfers","status":409,"title":"Idempotency conflict"}
$ (key pay-2, from ACC-1 to ACC-1, amount -5)
{"detail":"check the errors","instance":"/transfers","status":400,"title":"Invalid request","errors":{"differentAccounts":"from and to must differ","amount":"must be greater than 0"}}
$ (key pay-3, 10 to the frozen ACC-3)
{"detail":"account ACC-3 is frozen and can't receive money","instance":"/transfers","status":422,"title":"Transfer refused"}
$ curl -s localhost:8099/accounts/ACC-1
{"number":"ACC-1","owner":"Asha","balance":900.00}
```

ACC-1 is 900.00: the replay didn't debit again and the frozen transfer's debit was rolled back. On the stub, `POST /transfers` answers `501 Not implemented yet` with the task in the detail.

## Try it

```bash
mvn -pl chapters/38-project-the-transfers-service spring-boot:run
mvn -pl chapters/38-project-the-transfers-service test -Popen
```

H2 in memory; no Docker.

## Your turn

You edit `TransferRequest.java`, `TransferController.java`, `TransferService.java` and `interview-answer.md`. Entities, repositories (including the lock query), errors and seed data are finished.

1. **Validate.** Constraints on `TransferRequest` (+ `@AssertTrue` for `from` ≠ `to`) and `@Valid` in the controller.
2. **Move the money.** Lock both accounts in sorted order, debit, credit, save the `Transfer`, all in one transaction.
3. **Double-submit.** Look up the `Idempotency-Key` first: same request → the original transfer; different request → `IdempotencyConflict`.
4. **Responses.** 201 + `Location` for a new transfer, 200 for a replay; `GET /transfers/{id}`.
5. **(Interview)** Write your answer in `interview-answer.md`: walk through your design.

**Traps**

- Checking the balance, then updating in a second step without a lock: two parallel transfers both see 1000 and both pass.
- Locking A then B in one request and B then A in another: a deadlock under load. Always lock in the same order.
- Catching the `AccountFrozen` exception inside the transaction "to be nice": the debit then commits and money disappears.
- Idempotency by "was there a transfer of the same amount a moment ago?": two genuine transfers of 100 look like duplicates. Use a client-generated key.
- Testing only the happy path. Most of this chapter's checks are failures on purpose.

**Interview question (task 5).** Your endpoint design and error statuses. Correctness under concurrency and partial failure. Double-submit and how you tested it. The questions are repeated at the top of `interview-answer.md`.
