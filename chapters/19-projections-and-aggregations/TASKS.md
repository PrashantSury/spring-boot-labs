# Chapter 19 · Projections and aggregations

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter19CheckTest` 11/11 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/19-projections-and-aggregations test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/19-projections-and-aggregations spring-boot:run` (H2 in memory, no Docker).
- Fresh stub: 0/11 green.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Interface projection `List<TxnLine>` | `src/main/java/ledgerly/TxnRepository.java` | `mvn -pl chapters/19-projections-and-aggregations test -Popen` | task 1 checks green | ⬜ |
| 2 | `select new ledgerly.CategoryTotal(...) ... group by` | `TxnRepository.java` | same | task 2 checks green | ⬜ |
| 3 | `coalesce(sum(t.amount), 0)` | `TxnRepository.java` | same | task 3 checks green | ⬜ |
| 4 | Native monthly query | `TxnRepository.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** projections, group by, null sum, native | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Read only a few columns: which projection types?
- Totals per category in the DB; SUM over no rows?
- Native vs JPQL: when, and the cost?

## ⚠️ Traps

- Fully qualified class in `select new`.
- `coalesce` for empty sums.
- Native SQL = table/column names, not portable.

## ❓ Checks (letters only)

1. `select sum(t.amount) from Txn t where t.accountNumber = 'ACC-9'` (no rows) returns: A. 0 · B. null
2. `List<TxnLine> findByAccountNumber(...)` selects: A. every column · B. only the getters' columns
3. In a native query you write: A. `Txn` and `accountNumber` · B. `txn` and `account_number`

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** Reports and dashboards are where JPA apps get slow. "How would you build a spend-by-category report?" tests whether you push work to the database or drag rows into Java.

**The concept.** A projection is a smaller result shape than the entity. Spring Data builds the `select` from it, and with `GROUP BY` the database returns one row per group with `SUM`/`COUNT` already computed.

**Small example.** Sample data (ACC-1):

| description | category | amount | month |
|---|---|---|---|
| Rent October | RENT | 25000 | 2026-10 |
| BigBasket | GROCERY | 1200 | 2026-10 |
| DMart | GROCERY | 800 | 2026-10 |
| Rent November | RENT | 25000 | 2026-11 |
| Zepto | GROCERY | 450 | 2026-11 |
| IndiGo PNQ-BLR | TRAVEL | 3200 | 2026-11 |

Plus ACC-2: Blinkit, GROCERY, 300.

Real result of `spendByCategory("ACC-1")`:

| category | total | count | Why |
|---|---|---|---|
| RENT | 50000 | 2 | 25000 + 25000, biggest so first |
| TRAVEL | 3200 | 1 | one flight |
| GROCERY | 2450 | 3 | 1200 + 800 + 450; ACC-2's Blinkit 300 is **out** (other account) |

**Gotchas**
- Interface projections can be nested and can use `@Value("#{...}")`, but SpEL ones load the whole entity again.
- `count(t)` counts rows; `count(distinct t.category)` counts categories.
- Native queries skip Hibernate's startup validation: a typo shows up at the first call.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** SQL `SUM` over no rows is `NULL`.
2. **B.** Only `description` and `amount` are selected.
3. **B.** Native SQL talks to the table directly.

</details>
