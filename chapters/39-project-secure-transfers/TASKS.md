# Chapter 39 · Project: secure transfers

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter39CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter39CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | JWT login on top of the transfers service | not written yet | — | checks not written yet | ⬜ |
| 2 | Roles for customer, teller and auditor | not written yet | — | checks not written yet | ⬜ |
| 3 | Method security for ownership | not written yet | — | checks not written yet | ⬜ |
| 4 | Security tests for 401, 403 and success | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- How do you guarantee customers move only their own money? Which tests prove 401, 403 and success?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Don't trust an account id coming from the client without an ownership check
- Test the negative cases as hard as the happy path

## ❓ Checks (letters only)

1. A customer puts another customer's account in `from`. Expected: A. 201 · B. 403
2. Which tests matter most for security? A. only the happy path · B. the 401 and 403 cases as well
3. An auditor role should be able to: A. read · B. read and transfer

<details><summary>📖 Teaching</summary>

**Why it matters.** Add login, roles and ownership checks so customers move only their own money.

**The concept.** Add security to the transfers service: JWT login, roles for customer, teller and auditor, and an ownership check on every account id.

**Small example.**

```text
Customer cust-7 owns 0000123456. Teller t-1. Auditor a-1.

POST /transfers  {"from":"0000123456", ...}   as cust-7
POST /transfers  {"from":"0000999999", ...}   as cust-7   (not theirs)
GET  /accounts/0000123456                     as a-1
```

| Case | Result | In or out, and why |
|---|---|---|
| cust-7 transfers from their own account | 201 | role + ownership pass |
| cust-7 sends someone else's `from` | 403 | never trust an account id from the client |
| Auditor reads an account | 200 | read-only role |
| Auditor posts a transfer | 403 | no write permission |
| No token | 401 | not authenticated |

**Gotchas**
- Don't trust an account id coming from the client without an ownership check.
- Test the negative cases as hard as the happy path.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Ownership is checked on every id.
2. **B.** Negative tests catch the holes.
3. **A.** Least privilege.

</details>
