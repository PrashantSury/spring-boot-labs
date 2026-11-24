# Chapter 13 · Handling errors

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter13CheckTest` 15/15 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/13-handling-errors test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/13-handling-errors spring-boot:run` (port 8087).
- Fresh stub: 3/15 green (the happy paths and Spring's own 405/415, which must stay green after task 4).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `AccountNotFoundException` → 404 `ProblemDetail` | `src/main/java/ledgerly/ApiErrors.java` | `mvn -pl chapters/13-handling-errors test -Popen` | task 1 checks green | ⬜ |
| 2 | `InsufficientFundsException` → 422 + `shortBy` | `ApiErrors.java` | same | task 2 checks green | ⬜ |
| 3 | Extend `ResponseEntityExceptionHandler`, override `handleMethodArgumentNotValid` → `errors` map | `ApiErrors.java` | same | task 3 checks green | ⬜ |
| 4 | Catch-all `Exception` → 500, no leak, 405/415 intact | `ApiErrors.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** one error body, statuses, catch-all trap | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- One error body for every failure: how? Status for unknown account vs insufficient funds?
- Why is `@ExceptionHandler(Exception.class)` dangerous, and the fix?
- Two matching handlers: which runs?

## ⚠️ Traps

- Catch-all without the base class → 405/415 become 500.
- Don't add a second handler for `MethodArgumentNotValidException`; override.
- Never put `e.getMessage()` of an unexpected exception in the body.

## ❓ Checks (letters only)

1. Handlers exist for `RuntimeException` and `InsufficientFundsException`. Insufficient funds runs: A. `RuntimeException` · B. `InsufficientFundsException` · C. both
2. `ApiErrors` does NOT extend the base class and has a catch-all for `Exception`. `DELETE /transfers` returns: A. 405 · B. 500
3. Valid JSON, but the account is short by ₹8,000. Best status: A. 400 · B. 422 · C. 500

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** Every client (mobile, partner bank, support dashboard) needs to tell "you typed it wrong" from "you can't afford it" from "we broke". Interviewers ask "how do you handle exceptions globally in Spring?" in almost every backend round.

**The concept.** A `@RestControllerAdvice` is a bean that catches exceptions thrown by any controller. Each `@ExceptionHandler` method maps one exception type to a response; returning a `ProblemDetail` gives the standard body and the `application/problem+json` content type.

**Small example.** Sample data: `ACC-1` has ₹52,000, `ACC-2` has ₹1,000. Real responses from the finished app:

| Request | Exception thrown | Handler | Status | In or out of your code, and why |
|---|---|---|---|---|
| `GET /accounts/ACC-9/balance` | `AccountNotFoundException` | `notFound` | 404 | **in**: your domain exception, your handler |
| ₹60,000 from `ACC-1` | `InsufficientFundsException` | `funds` | 422, `shortBy: 8000` | **in**: valid request, business says no |
| `amount: 0`, `to: ""` | `MethodArgumentNotValidException` | your override | 400, `errors` map | **in**: base class catches it, you shape the body |
| `DELETE /transfers` | `HttpRequestMethodNotSupportedException` | base class | 405 | **out**: Spring already knows the right answer |
| `GET /accounts/ACC-1/statement` | `NullPointerException` | `unexpected` | 500, fixed detail | **in**: the safety net, no leak |

**Why "closest type wins".** `InsufficientFundsException` is also a `RuntimeException` and an `Exception`. Spring ranks the handlers by distance in the class hierarchy and picks the nearest, so the 422 handler beats the catch-all.

**Gotchas**
- An exception thrown in a servlet filter never reaches the advice (chapter 14).
- `ProblemDetail.setProperty("shortBy", 8000)` appears at the top level of the JSON, not under `properties`.
- `@ResponseStatus` on an exception class is the quick alternative, but it gives no body you control.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** The nearest exception type in the hierarchy wins.
2. **B.** The catch-all grabs `HttpRequestMethodNotSupportedException` before Spring's default resolver.
3. **B.** 422: the request is understood and well-formed, the business rule refuses it.

</details>
