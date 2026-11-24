# Chapter 8 · How Spring handles requests

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter08CheckTest` 18/18 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/08-how-spring-handles-requests test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/08-how-spring-handles-requests spring-boot:run` (port 8082), then `curl.exe localhost:8082/accounts/dormant`.
- Fresh stub: 3/18 green (behaviour that must not break).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `GET /accounts/dormant` → `["ACC-3"]` | `src/main/java/ledgerly/AccountController.java` | `mvn -pl chapters/08-how-spring-handles-requests test -Popen` | task 1 checks green | ⬜ |
| 2 | Statement as JSON or CSV with `produces`; XML → 406 | `StatementController.java` | same | task 2 checks green | ⬜ |
| 3 | `POST /transfers`, `consumes = "application/json"` | `TransferController.java` | same | task 3 checks green | ⬜ |
| 4 | `params = "currency"` / `"!currency"`, `headers = "X-Api-Version=2"` | `RateController.java` | same | task 4 checks green | ⬜ |
| 5 | `statusFor(exception)` → 405/415/406/400/404/500 | `HttpAnswers.java` | same | task 5 checks green | ⬜ |
| 6 | **(Interview)** the request flow and the status codes | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 6)

- What does the `DispatcherServlet` do with one request?
- Several mappings match: how does Spring choose? What do `produces`/`consumes`/`params`/`headers` add?
- 404 vs 405 vs 406 vs 415: which one means the path exists?

## ⚠️ Traps

- No `if ("dormant"...)` inside `byNumber`.
- Two plain `/rates` mappings → "Ambiguous mapping" at startup.
- Bad JSON → 400, not 500.

## ❓ Checks (letters only)

1. `POST /fee` where only `@GetMapping("/fee")` exists: A. 404 · B. 405 · C. 400
2. `GET /accounts/dormant` with mappings `/accounts/{number}` and `/accounts/dormant`: A. `{number}` wins, it was declared first · B. `/accounts/dormant` wins, it is more specific · C. startup fails
3. `POST /transfers` with `Content-Type: text/plain` and `consumes = "application/json"`: A. 400 · B. 406 · C. 415

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "What happens when a request hits a Spring app?" is the web version of "explain the bean lifecycle". Knowing which status Spring answers by itself saves hours: a 405 tells you the URL is right and the method is wrong.

**The concept.** The `DispatcherServlet` finds the best-matching handler method by path, then by conditions (`produces`, `consumes`, `params`, `headers`). If nothing matches, it answers with a status from the exception it raised, before any of your code runs.

**Small example.** Sample requests against the finished app:

| Request | Status | Exception | Why |
|---|---|---|---|
| `GET /fee` | 400 | `MissingServletRequestParameterException` | `amount` is required |
| `GET /fee?amount=abc` | 400 | `MethodArgumentTypeMismatchException` | `abc` is not a `long` |
| `POST /fee` | 405 | `HttpRequestMethodNotSupportedException` | path exists, GET only |
| `GET /no-such-path` | 404 | `NoHandlerFoundException` / `NoResourceFoundException` | nothing mapped |
| statement, `Accept: application/xml` | 406 | `HttpMediaTypeNotAcceptableException` | only JSON and CSV are produced |
| `POST /transfers`, `text/plain` | 415 | `HttpMediaTypeNotSupportedException` | only JSON is consumed |

Real output: `curl -X POST localhost:8082/fee` → `{"status":405,"error":"Method Not Allowed","path":"/fee"}`.

**Gotchas**
- 406 vs 415: 406 is about the **response** (`Accept`), 415 about the **request body** (`Content-Type`).
- A 405 response carries an `Allow: GET` header. Read it.
- Since Spring 6 an unknown path can show up as `NoResourceFoundException` (the static-resource handler tried and failed). Still 404.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** The path exists, the method doesn't.
2. **B.** A literal path is more specific than a pattern; declaration order doesn't matter.
3. **C.** The body type is not accepted.

</details>
