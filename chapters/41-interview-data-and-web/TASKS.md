# Chapter 41 · Interview: data and web

**Time:** ~60 min + 20 min answers · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter41CheckTest` 14/14 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/41-interview-data-and-web test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/41-interview-data-and-web spring-boot:run` (port 8101; `asha`, `ravi`, `ops`; password = name + `-pass`). H2, no Docker.
- Fresh stub: 1/14 green (no login → 401, asha adds a payee; must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | read-only transactions + join fetch (no N+1) | `src/main/java/ledgerly/CustomerQueries.java`, `CustomerRepository.java` | `mvn -pl chapters/41-interview-data-and-web test -Popen` | task 1 checks green | ⬜ |
| 2 | 201 + `Location`, 409, 404 problems | `PayeeController.java`, `ApiErrors.java` | same | task 2 checks green | ⬜ |
| 3 | `@Valid` body + `@Max(100)` parameter | `PayeeRequest.java`, `PayeeController.java` | same | task 3 checks green | ⬜ |
| 4 | ADMIN-only list + `@PreAuthorize` "only yourself" | `SecurityConfig.java`, `CustomerQueries.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** four spoken answers | `interview-answer.md` | graded by Lumi, then aloud 60–90 s each | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Request flow; status codes and error bodies.
- Entity states, N+1, transactions, locking.
- Validation and web-layer tests.
- JWT + method security end to end; 401 vs 403.

## ⚠️ Traps

- Name the default first, then your change.
- Don't "fix" lazy loading with open-in-view.
- Be ready to draw the request flow.

## ❓ Checks (letters only)

1. 1,000 customers, each with a lazy `accounts` list you touch. Queries without a join fetch: A. 1 · B. 1,001
2. Open-in-view is off and a controller touches a lazy collection after the service returned. Result: A. it loads · B. `LazyInitializationException`
3. Asha is logged in and asks for Ravi's customer record: A. 401 · B. 403

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** This round decides whether you've run Spring in production. N+1, lazy loading, status codes and ownership come up in nearly every backend loop.

**The concept.** Think of a bank branch. The request is a customer walking in: the guard checks ID (security filters), the receptionist sends them to the right desk (DispatcherServlet → controller), the form is checked for blanks (validation), and the clerk opens the ledger (a transaction) and copies what's needed onto a slip (a DTO). Lazy loading means the clerk only fetches a drawer when you ask for it; ask for 1,000 drawers one at a time and that's N+1. A join fetch brings the whole cabinet in one trip. Close the ledger before asking (no transaction) and the drawer can't be opened at all.

**Small example.** Real results (tests and curl):

| Call | Stub | Finished | Why |
|---|---|---|---|
| `GET /customers` (ops) | `LazyInitializationException` | 200, 1 SQL statement | read-only tx + join fetch |
| same, without the join fetch | | 200, 4 SQL statements | 1 + one per customer (N+1) |
| `POST /payees` new | 200 | 201 + `Location: /payees/1` | created |
| `POST /payees` same account | 500 | 409 `Duplicate payee` | business conflict |
| `GET /payees/999` | 500 | 404 `no payee 999` | unknown id |
| bad IFSC, blank name | 200, saved! | 400 with `errors` per field | `@Valid` |
| `GET /payees?size=500` | 200 | 400 | `@Max(100)` |
| asha `GET /customers` | 200 | 403 | URL rule |
| asha `GET /customers/2` | 500 | 403 | `@PreAuthorize` |

**Gotchas**
- Hibernate statistics (`generate_statistics=true`) or `spring.jpa.show-sql` are how you *prove* an N+1 in a test or a review.
- `readOnly = true` skips dirty checking and can route to a read replica; it doesn't stop you calling a write by mistake.
- The 403 from a URL rule and from `@PreAuthorize` look the same to the client; the difference is where the rule lives.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** 1 for the customers + 1 per customer.
2. **B.** The session closed with the transaction.
3. **B.** Known user, not allowed.

</details>
