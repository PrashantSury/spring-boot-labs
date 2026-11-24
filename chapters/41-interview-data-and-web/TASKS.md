# Chapter 41 · Interview: data and web

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter41CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter41CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Request flow, status codes and error handling | not written yet | — | checks not written yet | ⬜ |
| 2 | JPA states, N+1, transactions and locking | not written yet | — | checks not written yet | ⬜ |
| 3 | Validation and `@Valid`, with MockMvc tests | not written yet | — | checks not written yet | ⬜ |
| 4 | JWT login and method security in an end-to-end story | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Draw the full path of `POST /transfers` and name, at each step, the failure and its status code.
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Name the default behaviour first, then how you changed it
- Be ready to draw the request flow on a whiteboard

## ❓ Checks (letters only)

1. A body fails `@Valid`. Status: A. 400 · B. 422 · C. 500
2. Where is a missing JWT turned into 401? A. in your controller · B. in the security filter chain (entry point)
3. The cheapest proof that N+1 is fixed: A. it feels faster · B. count the SQL statements in a test or the log

<details><summary>📖 Teaching</summary>

**Why it matters.** Rehearse the questions on REST, JPA, validation, security and testing.

**The concept.** Rehearse the data and web answers end to end: request flow, status codes, JPA states and N+1, validation, security, and how you tested each.

**Small example.**

```text
Whiteboard: POST /transfers
client -> security filters (JWT) -> DispatcherServlet -> @Valid DTO -> @PreAuthorize
       -> TransferService @Transactional (lock, debit, credit) -> 201 / 400 / 401 / 403 / 409 / 422
```

| Case | Result | In or out, and why |
|---|---|---|
| "What does Spring answer for a wrong method?" | 405, before your code | name the default first |
| "Why 201 queries on the accounts page?" | N+1; `JOIN FETCH` or `@EntityGraph` | say how you spotted it (SQL log) |
| "Missing token vs wrong role?" | 401 vs 403, from the entry point and the access-denied handler | where each is produced |
| "How did you test it?" | MockMvc for 400/401/403/201, `@DataJpaTest` for queries | evidence, not claims |

**Gotchas**
- Name the default behaviour first, then how you changed it.
- Be ready to draw the request flow on a whiteboard.

</details>

<details><summary>🔒 Answers</summary>

1. **A.** Bad input is 400.
2. **B.** Before the `DispatcherServlet`.
3. **B.** Evidence.

</details>
