# Chapter 23 · Fetching and the N+1 problem

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter23CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter23CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Lazy vs eager fetching and `LazyInitializationException` | not written yet | — | checks not written yet | ⬜ |
| 2 | Spotting N+1 in the SQL log | not written yet | — | checks not written yet | ⬜ |
| 3 | Fixes: `JOIN FETCH`, `@EntityGraph`, batch size | not written yet | — | checks not written yet | ⬜ |
| 4 | Open session in view and why many teams turn it off | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- The accounts page runs 201 queries. How do you spot it, and which fixes do you know, with their trade-offs?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- `EAGER` on a collection is almost always a mistake
- `JOIN FETCH` with pagination loads everything in memory

## ❓ Checks (letters only)

1. 100 customers, lazy accounts, loop reading each. How many queries? A. 1 · B. 2 · C. 101
2. A fix for N+1: A. `FetchType.EAGER` everywhere · B. `JOIN FETCH` or `@EntityGraph`
3. Why do many teams turn `open-in-view` off? A. it is slower to start · B. it hides lazy loading in the web layer and holds connections longer

<details><summary>📖 Teaching</summary>

**Why it matters.** The accounts page takes 6 seconds because it runs 201 queries for 200 customers.

**The concept.** N+1 means one query for a list, then one more for each item's lazy relation. Fix it by fetching the relation in the same query.

**Small example.**

```java
// N+1: 1 query for customers + 1 per customer for accounts
for (Customer c : customerRepo.findAll()) { c.getAccounts().size(); }

// fix
@Query("select distinct c from Customer c left join fetch c.accounts")
List<Customer> findAllWithAccounts();
```

| Case | Result | In or out, and why |
|---|---|---|
| 200 customers, loop over `findAll()` | 201 queries in the SQL log | 1 + 200 lazy loads |
| `findAllWithAccounts()` | 1 query | `JOIN FETCH` loads both |
| `@EntityGraph(attributePaths = "accounts")` on `findAll` | 1 query | same fix, declared on the method |
| Touch `accounts` outside a transaction with `open-in-view: false` | `LazyInitializationException` | the session is closed |
| `JOIN FETCH` + `Pageable` on a collection | Hibernate warns and pages in memory ❌ | it loads everything first |

**Gotchas**
- `EAGER` on a collection is almost always a mistake.
- `JOIN FETCH` with pagination loads everything in memory.

</details>

<details><summary>🔒 Answers</summary>

1. **C.** One for the list plus one per customer.
2. **B.** EAGER often makes it worse.
3. **B.** Load what you need in the service.

</details>
