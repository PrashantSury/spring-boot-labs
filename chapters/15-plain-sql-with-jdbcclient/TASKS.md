# Chapter 15 · Plain SQL with JdbcClient

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter15CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter15CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `DataSource`, H2 and `schema.sql` / `data.sql` | not written yet | — | checks not written yet | ⬜ |
| 2 | `JdbcClient`: query to records, named parameters | not written yet | — | checks not written yet | ⬜ |
| 3 | Insert with generated keys, update counts | not written yet | — | checks not written yet | ⬜ |
| 4 | Testing with an in-memory database | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Why must a debit check the update count, and how do named parameters stop SQL injection?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Concatenating strings into SQL is an injection bug; use parameters
- Forgetting to check the update count hides a failed update

## ❓ Checks (letters only)

1. An `UPDATE` matches no rows. `JdbcClient`: A. throws · B. returns 0
2. Safe way to filter by user input: A. concatenate it into the SQL · B. a named parameter like `:no`
3. `balance > 50000` with balances 52,000 / 50,000 / 18,500 returns: A. 1 row · B. 2 rows · C. 3 rows

<details><summary>📖 Teaching</summary>

**Why it matters.** Before JPA hides the SQL, you should see the SQL: select, insert and update accounts yourself.

**The concept.** `JdbcClient` runs SQL you write, binds named parameters, and maps rows to records. You see exactly which SQL hits the database.

**Small example.**

```java
record AccountRow(String number, BigDecimal balance) {}

List<AccountRow> rich = jdbc.sql("SELECT number, balance FROM account WHERE balance > :min ORDER BY number")
        .param("min", 50_000)
        .query(AccountRow.class)
        .list();

int changed = jdbc.sql("UPDATE account SET balance = balance - :amt WHERE number = :no")
        .param("amt", 500).param("no", "0000999999")
        .update();
```

| Case | Result | In or out, and why |
|---|---|---|
| Accounts 52,000 / 18,500 / 75,000, `min` = 50,000 | 2 rows: 52,000 and 75,000 | 18,500 is not more than 50,000 |
| `UPDATE` for an account that exists | `changed` = 1 | one row updated |
| `UPDATE` for `0000999999` (no such account) | `changed` = 0, no exception | you must check the count yourself |
| `"... WHERE number = '" + input + "'"` with `input = "x' OR '1'='1"` | every row matches ❌ | string concatenation is SQL injection |

**Gotchas**
- Concatenating strings into SQL is an injection bug; use parameters.
- Forgetting to check the update count hides a failed update.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Zero rows is not an error to JDBC.
2. **B.** The driver sends the value separately from the SQL.
3. **A.** 50,000 is not more than 50,000.

</details>
