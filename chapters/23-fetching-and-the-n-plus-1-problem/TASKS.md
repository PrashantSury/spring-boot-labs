# Chapter 23 · Fetching and the N+1 problem

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter23CheckTest` 11/11 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/23-fetching-and-the-n-plus-1-problem test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/23-fetching-and-the-n-plus-1-problem spring-boot:run` (H2 in memory, no Docker; SQL is printed).
- Fresh stub: 2/11 green (task 2's rows are right, just too many selects; `accounts` is lazy; both must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `findAllWithAccounts` with `left join fetch` | `src/main/java/ledgerly/CustomerRepository.java` | `mvn -pl chapters/23-fetching-and-the-n-plus-1-problem test -Popen` | task 1 checks green (1 select) | ⬜ |
| 2 | `@EntityGraph(attributePaths = "customer")` | `AccountRepository.java` | same | task 2 checks green (1 select) | ⬜ |
| 3 | `@BatchSize(size = 50)`, still lazy | `Customer.java` | same | task 3 checks green (2 selects) | ⬜ |
| 4 | `@Transactional(readOnly = true)` on `ownerSummaries` | `StatementService.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** N+1, the three fixes, lazy errors, OSIV | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- What is N+1; how do you spot it?
- `JOIN FETCH` vs `@EntityGraph` vs `@BatchSize`?
- `LazyInitializationException`; why turn off open-session-in-view?

## ⚠️ Traps

- No EAGER collections.
- No `JOIN FETCH` with paging.
- OSIV hides the problem.

## ❓ Checks (letters only)

1. 200 accounts, each shows its customer's name, no fetch plan. Selects: A. 1 · B. 201 (at most)
2. `@BatchSize(size = 50)`, 200 customers, touch every customer's accounts. Selects: A. 1 + 200 · B. 1 + 4
3. `getAccounts().size()` after the repository call returned, outside a transaction, OSIV off: A. works · B. `LazyInitializationException`

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** N+1 is the classic "works on my laptop, dies in production" bug, and "how would you find and fix N+1?" is a standard mid-level Spring interview question.

**The concept.** A lazy relationship is a placeholder that runs a select the first time you touch it. Touching it once per row in a loop is N extra selects. A fetch plan (join fetch, entity graph) or a batch tells Hibernate to load them together.

**Small example.** Sample data: 5 customers (Asha Rao … Zoya Khan) × 2 accounts; the `…2` account of customer n holds 50,000 × n. Real statement counts from the checks:

| Approach | Selects for 5 customers | For 4,000 customers | In or out | Why |
|---|---|---|---|---|
| lazy, loop touches each (stub) | 1 + 5 = 6 | 4,001 | ❌ out | N+1 |
| `JOIN FETCH` | 1 | 1 | ✅ in | one join; but no paging |
| `@EntityGraph("customer")` | 1 | 1 | ✅ in | one join on a derived query |
| `@BatchSize(50)` | 2 | 1 + 80 | ✅ in | lazy stays lazy, loads in groups |
| `FetchType.EAGER` | varies | varies | ❌ out | loads accounts even when you only want names |

**Gotchas**
- `distinct` in a JPQL fetch join removes repeated parents (Hibernate 6+ does this in memory anyway).
- `hibernate.default_batch_fetch_size=50` sets the batch size for all lazy relationships at once.
- DTO projections (chapter 19) avoid the problem entirely for read-only screens.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** 1 for the accounts + 1 per distinct customer (up to 200).
2. **B.** 200 / 50 = 4 batch selects.
3. **B.** The persistence context is closed.

</details>
