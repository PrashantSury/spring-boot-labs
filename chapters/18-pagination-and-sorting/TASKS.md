# Chapter 18 · Pagination and sorting

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter18CheckTest` 13/13 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/18-pagination-and-sorting test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/18-pagination-and-sorting spring-boot:run` (port 8089; H2 in memory, no Docker).
- Fresh stub: 0/13 green.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `Page<Txn> findByAccountNumber(String, Pageable)` | `src/main/java/ledgerly/TxnRepository.java` | `mvn -pl chapters/18-pagination-and-sorting test -Popen` | task 1 checks green | ⬜ |
| 2 | Statement endpoint: defaults, size cap 100, `PageResponse` | `StatementController.java` | same | task 2 checks green | ⬜ |
| 3 | `sortFrom`: parse, whitelist → 400, `id` tie-breaker | `StatementController.java` | same | task 3 checks green | ⬜ |
| 4 | `Slice` query + `/feed` endpoint | `TxnRepository.java`, `StatementController.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** Page vs Slice, sort whitelist | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Paging and sorting in Spring Data: `Pageable`, `Page`?
- When a `Slice`?
- Why whitelist the sort; what else do you cap?

## ⚠️ Traps

- Pages start at 0.
- Whitelist sort properties; cap size.
- Add `id` as a tie-breaker.

## ❓ Checks (letters only)

1. 45 rows, size 20. `?page=2` returns: A. rows 21–40 · B. rows 41–45
2. Which runs a `count` query? A. `Page` · B. `Slice` · C. both
3. `?sort=password,asc` on an entity without that field, no whitelist: A. 400 · B. 500 with a "No property" message

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** Every list endpoint in a real bank is paged, and interviewers ask "Page vs Slice?", "how do you stop a client from requesting a million rows?" and "why do rows repeat across pages?" to see if you have run a list endpoint in production.

**The concept.** A `Pageable` says "page n, size s, sorted by x". Spring Data turns it into `ORDER BY x OFFSET n*s FETCH FIRST s ROWS`. A `Page` also counts all matching rows; a `Slice` fetches `s + 1` rows and only reports whether there was an extra one.

**Small example.** Sample data: ACC-1 has 45 transactions, UPI #1 (2 Oct) to UPI #45 (15 Nov); ACC-2 has 3. Real results:

| Request | Rows returned | In or out, and why |
|---|---|---|
| `?page=0&size=20` | UPI #45 … #26 | newest first (default `bookedAt,desc`); #25 and older are on later pages |
| `?page=2&size=20` | UPI #5 … #1 (5 rows) | 45 − 40 = 5 left on the last page |
| `?size=2&sort=amount,desc` | UPI #36 (5032), #35 (4895) | the biggest amounts; date ignored |
| any ACC-1 page | never ACC-2's NEFT rows | the `where account_number = ?` comes first, then sort and page |
| `?sort=description` | 400 | not on the whitelist |

**Gotchas**
- Spring MVC can also inject a `Pageable` parameter directly (`?page=&size=&sort=`), but then the whitelist and cap still have to be added (`spring.data.web.pageable.max-page-size`).
- Deep offsets get slow (`OFFSET 1000000` reads and throws away a million rows); very large feeds use keyset pagination ("after id 12345").
- `Page.map(TxnView::of)` converts the content but keeps the totals.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** Page 0 = 1–20, page 1 = 21–40, page 2 = 41–45.
2. **A.** Only `Page` needs the total.
3. **B.** Spring Data fails on the unknown property and the client sees a 500.

</details>
