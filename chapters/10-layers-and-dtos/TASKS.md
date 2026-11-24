# Chapter 10 · Layers and DTOs

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter10CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter10CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Controller, service, repository: one job each | not written yet | — | checks not written yet | ⬜ |
| 2 | Request and response DTOs as records | not written yet | — | checks not written yet | ⬜ |
| 3 | Mapping entities to DTOs by hand, and when a mapper library is worth it | not written yet | — | checks not written yet | ⬜ |
| 4 | Keeping business rules out of controllers | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Why should a controller never return a JPA entity? Where does a rule like "minimum balance 1,000" live, and why?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Putting logic in the controller makes it hard to test without MockMvc
- Never expose a field like `pin` or `internalNotes` by returning the entity

## ❓ Checks (letters only)

1. Returning the `Account` entity from a controller mainly risks: A. slower JSON · B. leaking fields like `pin` and coupling the API to the schema
2. "Minimum balance 1,000" belongs in: A. the controller · B. the service · C. the repository
3. A client sends `"balance": 999999` in a deposit request bound to the entity. This is: A. fine · B. mass assignment

<details><summary>📖 Teaching</summary>

**Why it matters.** Returning the `Account` entity straight from the controller leaks internal fields and ties the API to the database.

**The concept.** The controller speaks HTTP, the service holds the rules, the repository talks to the database. The API takes and returns DTOs (records), never entities.

**Small example.**

```java
@Entity class Account {           // what the database holds
    Long id; String number; BigDecimal balance; String pin; String internalNotes;
}
record AccountView(String maskedNumber, BigDecimal balance) {   // what the client sees
    static AccountView of(Account a) {
        return new AccountView("******" + a.getNumber().substring(a.getNumber().length() - 4), a.getBalance());
    }
}
```

| Case | Result | In or out, and why |
|---|---|---|
| Return the entity | `{"id":1,"number":"0000123456","balance":52000,"pin":"4821","internalNotes":"VIP, waive fees"}` | leaks `pin` and notes ❌ |
| Return `AccountView.of(account)` | `{"maskedNumber":"******3456","balance":52000}` | only what the client needs ✅ |
| Bind the request straight into the entity | a client can send `"balance": 999999` | mass assignment ❌ |
| Bind into `record DepositRequest(BigDecimal amount)` | only `amount` can arrive | the DTO is the contract ✅ |

**Gotchas**
- Putting logic in the controller makes it hard to test without MockMvc.
- Never expose a field like `pin` or `internalNotes` by returning the entity.
- Entities with lazy relations can fail or loop when Jackson serialises them.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** The API should not change when a column changes.
2. **B.** Business rules live in the service so every caller gets them.
3. **B.** Bind requests to DTOs with only the allowed fields.

</details>
