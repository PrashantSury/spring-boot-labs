# Chapter 11 · Updating and deleting

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter11CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter11CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | PUT vs PATCH, and idempotency | not written yet | — | checks not written yet | ⬜ |
| 2 | DELETE answers: 204 vs 404 when it is already gone | not written yet | — | checks not written yet | ⬜ |
| 3 | Optimistic checks: unknown id gives 404 | not written yet | — | checks not written yet | ⬜ |
| 4 | MockMvc tests for create, read, update, delete | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- PUT vs PATCH: what does each do with a missing field? Is DELETE idempotent if the second call returns 404?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- PUT replaces the whole resource; missing fields become null
- Calling DELETE twice should give the same result

## ❓ Checks (letters only)

1. PUT `{"name":"Asha"}` on a customer that also has an email. The email: A. is kept · B. becomes null
2. Which is idempotent? A. POST /transfers · B. PUT /customers/7 · C. both
3. Second DELETE on a removed customer returns 404. Still idempotent? A. yes, the state didn't change · B. no, the status changed

<details><summary>📖 Teaching</summary>

**Why it matters.** A customer changes their phone number and closes an account: PUT, PATCH and DELETE must behave exactly as clients expect.

**The concept.** PUT replaces the whole resource and PATCH changes only the fields sent. PUT and DELETE are idempotent: calling them twice leaves the same state.

**Small example.**

```text
Customer 7 now: {"name":"Asha Rao","phone":"98200 11111","email":"asha@example.com"}

PUT   /customers/7  {"name":"Asha Rao","phone":"98200 22222"}
PATCH /customers/7  {"phone":"98200 22222"}
DELETE /customers/7   (twice)
```

| Case | Result | In or out, and why |
|---|---|---|
| PUT without `email` | `email` becomes `null` | PUT replaces everything; missing means empty |
| PATCH with only `phone` | `phone` changes, `email` kept | PATCH touches only what is sent |
| PUT on `/customers/999` (unknown) | 404 | nothing to replace (unless you allow create-on-PUT) |
| First `DELETE /customers/7` | 204 | deleted |
| Second `DELETE /customers/7` | 404 (or 204, if you choose that) | the state is the same either way, so it is still idempotent; pick one and document it |

**Gotchas**
- PUT replaces the whole resource; missing fields become null.
- Calling DELETE twice should leave the same state; the status code may differ.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** PUT is a full replacement.
2. **B.** Repeating PUT gives the same state; repeating POST makes another transfer.
3. **A.** Idempotency is about server state, not the status code.

</details>
