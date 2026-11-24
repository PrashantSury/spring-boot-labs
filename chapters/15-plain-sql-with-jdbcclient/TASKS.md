# Chapter 15 · Plain SQL with JdbcClient

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter15CheckTest` 15/15 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/15-plain-sql-with-jdbcclient test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/15-plain-sql-with-jdbcclient spring-boot:run` (no web server; H2 in memory, no Docker).
- Fresh stub: 3/15 green (`ACC-9` empty, `deposit` to `ACC-9` false, the "Asha" search; all must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `findAll` ordered by number | `src/main/java/ledgerly/AccountDao.java` | `mvn -pl chapters/15-plain-sql-with-jdbcclient test -Popen` | task 1 checks green | ⬜ |
| 2 | `findByNumber` with `:number` → `Optional` | `AccountDao.java` | same | task 2 checks green | ⬜ |
| 3 | `searchByOwner` with a parameter (no injection) | `AccountDao.java` | same | task 3 checks green | ⬜ |
| 4 | `insert` returns the generated id | `AccountDao.java` | same | task 4 checks green | ⬜ |
| 5 | `deposit` / `withdraw` with the update count | `AccountDao.java` | same | task 5 checks green | ⬜ |
| 6 | **(Interview)** JdbcClient, injection, update count | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 6)

- Select, insert, update with JdbcClient; how do you get the generated id?
- Why is concatenation dangerous; what does a parameter change?
- Why check the update count; how does a withdrawal never overdraw?

## ⚠️ Traps

- `%` goes in the parameter value, not in the SQL.
- `.single()` throws on 0 rows.
- No select-then-update for a withdrawal.

## ❓ Checks (letters only)

1. `update account set balance = balance + 500 where number = 'ACC-9'` (no such row) returns: A. 1 · B. 0 · C. an exception
2. `where owner like '%:pattern%'` with `.param("pattern", "Asha")` finds: A. ACC-1 and ACC-3 · B. nothing
3. Which is safe? A. `"where number = '" + n + "'"` · B. `"where number = :n"` + `.param("n", n)`

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** SQL injection is still in the OWASP Top 10, and banks get pen-tested every year. Interviewers ask "how do you prevent SQL injection?" and "what does `executeUpdate` return?" because the answers show whether you have written SQL against a real database.

**The concept.** With a named parameter, the database receives the SQL with a placeholder and the value separately, so the value can never change the statement. `update()` tells you how many rows the statement changed, which is your only proof that it did something.

**Small example.** Sample data (`data.sql`):

| id | number | owner | balance |
|---|---|---|---|
| 1 | ACC-1 | Asha Rao | 52000 |
| 2 | ACC-2 | Vikram Shah | 1000 |
| 3 | ACC-3 | Asha Verma | 7500 |

Real results of `searchByOwner`:

| Input | Concatenated SQL (stub) | With `:pattern` (finished) | Why |
|---|---|---|---|
| `Asha` | ACC-1, ACC-3 | ACC-1, ACC-3 | both contain "Asha"; ACC-2 is out |
| `' OR 1=1 --` | ACC-1, ACC-2, ACC-3 ❌ | none | concatenated: `like '%' OR 1=1 --%'` is always true and `--` comments out the rest; parameter: no owner contains that text |
| `O'Brien` | SQL syntax error ❌ | none | the quote closes the string literal early |

And `withdraw` in one statement (real results, `ACC-2` = 1000):

| Call | Rows changed | Returns | Balance after | Why |
|---|---|---|---|---|
| `withdraw("ACC-2", 2000)` | 0 | `false` | 1000 | `balance >= 2000` fails, so the row is not matched |
| `withdraw("ACC-2", 600)` | 1 | `true` | 400 | check and change happen atomically |

**Gotchas**
- Boot runs `schema.sql`/`data.sql` automatically only for embedded databases like H2 (`spring.sql.init.mode=always` for others).
- Record mapping needs column names that match component names (use `as` aliases otherwise).
- `JdbcClient` throws Spring's `DataAccessException` family (`DuplicateKeyException`, ...), not raw `SQLException`.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** 0 rows matched, 0 rows changed. No exception, which is why you must check.
2. **B.** Inside quotes `:pattern` is literal text, not a parameter.
3. **B.** The value never becomes part of the SQL.

</details>
