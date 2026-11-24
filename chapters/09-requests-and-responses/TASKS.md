# Chapter 9 · Requests and responses

**Time:** ~70 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter09CheckTest` 21/21 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/09-requests-and-responses test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/09-requests-and-responses spring-boot:run` (port 8083).
- Fresh stub: 2/21 green (Spring's own 400s, which must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `POST /accounts` → 201 + `Location` + body; 400 on bad input; `@JsonIgnoreProperties` | `src/main/java/ledgerly/AccountController.java`, `OpenAccountRequest.java` | `mvn -pl chapters/09-requests-and-responses test -Popen` | task 1 checks green | ⬜ |
| 2 | `DELETE /accounts/{number}` → 204 / 404 | `AccountController.java` | same | task 2 checks green | ⬜ |
| 3 | `/transactions?from=&limit=` + echo `X-Request-Id` | `AccountController.java` | same | task 3 checks green | ⬜ |
| 4 | `@JsonProperty("txn_id")`, `@JsonIgnore internalRef` | `TransactionView.java` | same | task 4 checks green | ⬜ |
| 5 | `/balance` with `Cache-Control: no-store`; 404 empty | `AccountController.java` | same | task 5 checks green | ⬜ |
| 6 | **(Interview)** request data, statuses, content types | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 6)

- Path, query, header, body: which annotation reads each?
- Create / delete / read: which status and headers, and why `ResponseEntity`?
- JSON in and out: what decides it; `text/plain` or an unknown field?

## ⚠️ Traps

- `ok(...)` is 200. Create is `created(uri)`.
- Delete returns `ResponseEntity<Void>` → 204.
- `from` includes the day. `limit` has a default.

## ❓ Checks (letters only)

1. A successful `POST /accounts` should answer: A. 200 + body · B. 201 + `Location` + body · C. 204
2. `@RequestParam int limit` (no default) and the client sends no `limit`: A. 0 · B. 400 · C. 500
3. A record field `internalRef` with `@JsonIgnore`: A. written as `null` · B. not written at all · C. written as `"***"`

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** API reviews in banks check HTTP semantics, not just data: a create without `Location`, a delete that returns 200 with a body, or a cacheable balance are all review blockers. This is the "HTTP semantics" weak spot from the banking app.

**The concept.** Each part of the request has its own annotation, and `ResponseEntity` lets one method choose status, headers and body. Jackson turns records into JSON by component name unless an annotation says otherwise.

**Small example.** Sample data: transactions T1 (2026-11-01, 50000), T2 (2026-11-03, -1200), T3 (2026-11-20, -800).

```java
@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
@RequestParam(defaultValue = "10") int limit
```

Real output for `?from=2026-11-03`:

```
[{"txn_id":"T2","date":"2026-11-03","amount":-1200},{"txn_id":"T3","date":"2026-11-20","amount":-800}]
```

| Txn | Date | In? | Why |
|---|---|---|---|
| T1 | 2026-11-01 | out | before `from` |
| T2 | 2026-11-03 | in | **on** `from` counts |
| T3 | 2026-11-20 | in | after `from` |

**Gotchas**
- `Location` should point at the new resource, so the client can `GET` it next.
- `ResponseEntity.notFound().build()` has no body; a `ResponseStatusException` gives Spring Boot's JSON error body instead.
- `Cache-Control: no-store` for anything with a balance or personal data.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** 201 Created, where it is, and what was created.
2. **B.** A required param is missing: `MissingServletRequestParameterException` → 400.
3. **B.** `@JsonIgnore` removes the property from the JSON.

</details>
