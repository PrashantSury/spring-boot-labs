# Chapter 15: Plain SQL with JdbcClient

By the end of this chapter you can read, insert and update Ledgerly accounts with hand-written SQL through `JdbcClient`, using named parameters, generated keys and update counts, against an in-memory H2 database.

## Why you need this

JPA (chapter 16) writes the SQL for you, which is great until a query is slow or wrong and you cannot read what it sent. Ledgerly's first account search was plain SQL glued together with `+`, and a pen tester typed `' OR 1=1 --` into the owner field and got every customer back. Before handing SQL to a framework you should be able to write it yourself, safely.

## What you will learn

- **DataSource + H2:** Boot creates an in-memory database and runs `schema.sql` then `data.sql` at startup. No Docker.
- **`JdbcClient`:** `sql(...)`, `.param("name", value)`, `.query(Account.class).list()/optional()/single()`, `.update()`.
- **Records as rows:** columns `id, number, owner, balance` map onto `record Account(long id, String number, String owner, long balance)`.
- **Generated keys:** `update(keyHolder, "id")`.
- **Update counts:** `update()` returns how many rows changed; 0 means "nothing happened".
- **SQL injection:** why `+` is a bug and a parameter is the fix.

## Real output

`mvn -q -pl chapters/15-plain-sql-with-jdbcclient spring-boot:run` on the finished code:

```
all accounts          [Account[id=1, number=ACC-1, owner=Asha Rao, balance=52000], Account[id=2, number=ACC-2, owner=Vikram Shah, balance=1000], Account[id=3, number=ACC-3, owner=Asha Verma, balance=7500]]
ACC-2                 Optional[Account[id=2, number=ACC-2, owner=Vikram Shah, balance=1000]]
ACC-9                 Optional.empty
owner like 'Asha'     [ACC-1, ACC-3]
owner like injection  []
new id for ACC-4      4
deposit 500 to ACC-4  true
deposit 500 to ACC-9  false
withdraw 2000 ACC-2   false
withdraw 600 ACC-2    true
ACC-2 after           400
```

The same tour on the stub prints `owner like injection  [ACC-1, ACC-2, ACC-3]`: the input `' OR 1=1 --` became part of the SQL.

## The shape of a query

```java
public Optional<Account> findByNumber(String number) {
  return jdbc.sql("select id, number, owner, balance from account where number = :number")
      .param("number", number)          // the value travels separately from the SQL
      .query(Account.class)             // columns -> record components
      .optional();                      // 0 rows -> empty, 1 row -> value
}
```

## Try it

```bash
mvn -q -pl chapters/15-plain-sql-with-jdbcclient spring-boot:run
mvn -pl chapters/15-plain-sql-with-jdbcclient test -Popen
```

Each check runs in a rolled-back transaction, so every test starts with the same 3 accounts.

## Your turn

You edit `AccountDao.java` and `interview-answer.md`. `schema.sql` and `data.sql` are finished.

1. **findAll.** All accounts ordered by number.
2. **findByNumber.** Named parameter, `Optional`.
3. **searchByOwner.** Rewrite the concatenated query with a parameter (`%` goes in the value).
4. **insert.** Return the generated id; let a duplicate number throw.
5. **deposit / withdraw.** Use the update count; withdraw checks the balance inside the same `update`.
6. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- `"... like '%" + fragment + "%'"` works in the demo and is an injection bug. `O'Brien` also breaks it.
- `like '%:pattern%'` does not work: a parameter inside quotes is just text. Put `%` in the value.
- `.single()` throws on 0 rows; use `.optional()` when the row may not exist.
- Ignoring `update()`'s return value: an unknown account "succeeds" silently.
- Select-then-update for a withdrawal is a race: two requests both see ₹1,000 and both take ₹600.

**Interview question (task 6).** Select, insert and update with JdbcClient, and the generated id? Why is concatenation dangerous, and what does a parameter change? Why check the update count, and how do you make a withdrawal that never overdraws? The questions are repeated at the top of `interview-answer.md`.
