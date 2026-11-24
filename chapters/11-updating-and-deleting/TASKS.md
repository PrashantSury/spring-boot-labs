# Chapter 11 · Updating and deleting

**Time:** ~70 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter11CheckTest` 18/18 ✅ + your `PayeeCrudTest` green + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/11-updating-and-deleting test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/11-updating-and-deleting spring-boot:run` (port 8085).
- Fresh stub: 0/18 green. Reference: 18/18 + 4 own tests = 22.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | PUT replaces; 404 unknown; 400 incomplete | `src/main/java/ledgerly/PayeeController.java` | `mvn -pl chapters/11-updating-and-deleting test -Popen` | task 1 checks green | ⬜ |
| 2 | PATCH merges by key; null clears; `{}` no-op; 400 rules | `PayeeController.java` | same | task 2 checks green | ⬜ |
| 3 | DELETE → 204, twice | `PayeeController.java` | same | task 3 checks green | ⬜ |
| 4 | `stale()` + 412 on PUT and PATCH | `PayeeController.java` | same | task 4 checks green | ⬜ |
| 5 | ≥ 4 own MockMvc tests with `@DisplayName` | `src/test/java/ledgerly/PayeeCrudTest.java` | same | task 5 check + your tests green | ⬜ |
| 6 | **(Interview)** PUT/PATCH, idempotency, lost updates | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 6)

- PUT vs PATCH: what happens to a left-out field?
- Idempotent: which methods, and the second DELETE?
- How do you stop lost updates?

## ⚠️ Traps

- PUT must null the missing nickname.
- `{}` keeps the version.
- Order: 404 → 412 → 400.
- `If-Match: "2"` with quotes.

## ❓ Checks (letters only)

1. `PUT /payees/1` with no `nickname` when it was "Landlord": A. stays "Landlord" · B. becomes `null` · C. 400
2. Which is **not** idempotent? A. PUT · B. DELETE · C. POST
3. The client sends `If-Match: "1"` but the payee is at version 2: A. 409 · B. 412 · C. 200 and overwrite

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "PUT vs PATCH" and "what is idempotency?" are standard REST questions, and lost updates on shared accounts are a real bank incident type. Retries on mobile networks make idempotency a must, not a nicety.

**The concept.** PUT says "here is the whole new thing"; PATCH says "change these keys". Idempotent requests can be retried safely. An `ETag` is a version stamp; `If-Match` makes an update conditional on it.

**Small example.** Start: `{name: "Vikram Shah", ifsc: "HDFC0001234", nickname: "Landlord", version: 1}`.

| Request | nickname after | version | Why |
|---|---|---|---|
| `PATCH {"nickname":"Rent"}` | Rent | 2 | only that key changes |
| `PATCH {"nickname":null}` | null | 2 | key present with null clears |
| `PATCH {}` | Landlord | 1 | nothing changed |
| `PUT` without nickname | null | 2 | PUT replaces everything |
| `PATCH` + `If-Match: "7"` | Landlord | 1 | 412, nothing changes |

Real output: the stale PATCH above answered `HTTP/1.1 412`.

**Gotchas**
- 409 Conflict is for business conflicts (a duplicate PAN); 412 is specifically "your precondition (`If-Match`) failed".
- JPA's `@Version` (chapter 33) does the same check in the database.
- Some APIs answer 404 for a repeated DELETE. Both are defensible; be consistent and document it.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** PUT replaces the whole resource.
2. **C.** Each POST creates a new payee.
3. **B.** Precondition Failed.

</details>
