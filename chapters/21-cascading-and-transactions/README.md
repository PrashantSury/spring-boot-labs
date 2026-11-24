# Chapter 21: Cascading and transactions

By the end of this chapter a Ledgerly transfer is all-or-nothing, a checked exception no longer leaves half a transfer behind, each payment in a salary batch stands on its own, and a customer's payees are saved and deleted with the customer.

## Why you need this

A customer typed `ACC-9` instead of `ACC-2`. The first Ledgerly version debited ₹3,000, then failed to find the target account, and the money vanished. A salary batch with one bad row left that row's debit behind as well. Separately, support kept finding payee rows whose customer had been deleted. Transactions fix the first two; cascading fixes the third.

## What you will learn

- **`cascade = CascadeType.ALL` and `orphanRemoval = true`:** children follow their parent.
- **`@Transactional`:** commit on success, roll back on an unchecked exception.
- **Checked exceptions commit by default:** `rollbackFor`.
- **Self-invocation:** `this.method()` skips the Spring proxy, so its `@Transactional` is ignored.
- **Where to put `@Transactional`:** on service methods.

## Real output

The tour on the **stub** (no transactions, real output):

```
== transfer 3000 ACC-1 -> ACC-9 (no such account)
failed: No account ACC-9
ACC-1 balance: 7000
== transferWithLimit 6000 (checked exception)
failed: Daily limit is 5000, asked for 6000
ACC-1 balance: 1000
```

₹9,000 gone in two failed transfers. The same tour on the finished code, with the transaction manager's DEBUG log (real lines, cut):

```
payees after saving the customer: 2
== transfer 3000 ACC-1 -> ACC-9 (no such account)
Creating new transaction with name [ledgerly.TransferService.transfer]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
Participating in existing transaction
Initiating transaction rollback
failed: No account ACC-9
ACC-1 balance: 10000
== transferWithLimit 6000 (checked exception)
Creating new transaction with name [ledgerly.TransferService.transferWithLimit]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT,-ledgerly.DailyLimitExceededException
Initiating transaction rollback
ACC-1 balance: 10000
```

`-ledgerly.DailyLimitExceededException` is Spring's notation for "roll back on this one". `Participating in existing transaction` is each `accounts.save()` joining the service's transaction instead of committing on its own.

## Try it

```bash
mvn -q -pl chapters/21-cascading-and-transactions spring-boot:run
mvn -pl chapters/21-cascading-and-transactions test -Popen
```

The checks are deliberately NOT `@Transactional`, so every call commits or rolls back for real.

## Your turn

You edit `Customer.java`, `TransferService.java`, `PaymentWorker.java` and `interview-answer.md`.

1. **Cascade.** `cascade = CascadeType.ALL, orphanRemoval = true` on `Customer.payees`.
2. **Transaction.** `@Transactional` on `transfer`.
3. **Checked exception.** `rollbackFor = DailyLimitExceededException.class` on `transferWithLimit`.
4. **Self-invocation.** Move `payOne` into `PaymentWorker.pay` (`@Transactional`) and call `worker.pay(p)`.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- Checked exceptions (`extends Exception`) commit what was done before them.
- `this.payOne(p)` from inside the same class: no proxy, no transaction, even with `@Transactional` on it.
- `@Transactional` on a `private` method is ignored too (the proxy can't override it).
- Catching the exception inside the `@Transactional` method and not rethrowing: Spring sees success and commits.
- `CascadeType.REMOVE` towards a shared entity (an account's branch) deletes the branch for everyone.

**Interview question (task 5).** Cascade and orphanRemoval? What does `@Transactional` do for a transfer, and which exceptions roll back? Why does a same-class call skip it, and the fix? The questions are repeated at the top of `interview-answer.md`.
