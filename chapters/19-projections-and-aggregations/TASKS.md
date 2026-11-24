# Chapter 19 · Projections and aggregations

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter19CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter19CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Interface and record projections | not written yet | — | checks not written yet | ⬜ |
| 2 | `@Query` with `GROUP BY`, `SUM`, `COUNT` | not written yet | — | checks not written yet | ⬜ |
| 3 | Constructor expressions in JPQL | not written yet | — | checks not written yet | ⬜ |
| 4 | Native queries and when to use them | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- How do you build a dashboard of total balance per branch without loading every account?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Selecting the whole entity to read two fields wastes memory
- `SUM` over no rows returns null, not 0

## ❓ Checks (letters only)

1. `SUM(balance)` over zero rows returns: A. 0 · B. null
2. A JPQL constructor expression needs: A. the class's fully qualified name with `new` · B. a no-arg constructor
3. Best way to get one total per branch: A. load all accounts and add in Java · B. `GROUP BY` in the query

<details><summary>📖 Teaching</summary>

**Why it matters.** The dashboard needs total balance per branch, not every account.

**The concept.** Select only what the screen needs: a projection reads chosen columns, and `GROUP BY` lets the database add the numbers up.

**Small example.**

```java
record BranchTotal(String branch, BigDecimal total, Long accounts) {}

@Query("""
    select new com.ledgerly.BranchTotal(a.branch, sum(a.balance), count(a))
    from Account a group by a.branch order by a.branch""")
List<BranchTotal> totals();
```

| Case | Result | In or out, and why |
|---|---|---|
| Andheri: 52,000 + 18,500 · Pune: 75,000 | `[BranchTotal[Andheri, 70500, 2], BranchTotal[Pune, 75000, 1]]` | one row per branch |
| `sum(a.balance)` for a branch with no accounts (via a filter) | `null`, not 0 | SQL `SUM` over no rows is null; use `coalesce(sum(...), 0)` |
| Load every `Account` entity to add balances in Java | same numbers, far more memory and SQL | selecting whole entities to read two fields wastes memory |
| Native query | fine for database-specific SQL | but no JPQL type checking |

**Gotchas**
- Selecting the whole entity to read two fields wastes memory.
- `SUM` over no rows returns null, not 0.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Wrap it in `coalesce` if you need 0.
2. **A.** `select new com.ledgerly.BranchTotal(...)`.
3. **B.** Let the database aggregate.

</details>
