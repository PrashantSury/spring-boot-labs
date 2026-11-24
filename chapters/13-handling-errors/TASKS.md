# Chapter 13 · Handling errors

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter13CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter13CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@RestControllerAdvice` and `@ExceptionHandler` | not written yet | — | checks not written yet | ⬜ |
| 2 | Problem details (`ProblemDetail`) as a standard error body | not written yet | — | checks not written yet | ⬜ |
| 3 | Handler ordering: most specific exception wins, and `@Order` on advices | not written yet | — | checks not written yet | ⬜ |
| 4 | Mapping domain exceptions such as `InsufficientFundsException` to 409/422 | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- How do you give every failure the same error body without breaking Spring's own 400/405/415?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- A handler for `Exception` will swallow Spring's built-in 400/404/405 unless you extend the base class
- Two advices that handle the same exception: the order decides

## ❓ Checks (letters only)

1. Handlers for `RuntimeException` and `InsufficientFundsException` both exist. Which runs for insufficient funds? A. `RuntimeException` · B. `InsufficientFundsException` · C. both
2. You add a catch-all for `Exception`. A wrong HTTP method now returns: A. 405 · B. 500
3. `ProblemDetail` is: A. a Spring-only log format · B. the RFC 9457 error body (`type`, `title`, `status`, `detail`)

<details><summary>📖 Teaching</summary>

**Why it matters.** Clients should get the same error shape for every failure, never a stack trace.

**The concept.** A `@RestControllerAdvice` turns exceptions into one error shape for the whole API. Spring picks the handler for the most specific exception type.

**Small example.**

```java
class InsufficientFundsException extends RuntimeException { ... }

@RestControllerAdvice
class ApiErrors {
    @ExceptionHandler(InsufficientFundsException.class)
    ProblemDetail funds(InsufficientFundsException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        pd.setTitle("Insufficient funds");
        return pd;
    }
}
```

| Case | Result | In or out, and why |
|---|---|---|
| Transfer of ₹60,000 from a ₹52,000 account | 422, `{"title":"Insufficient funds","status":422,"detail":"..."}` | your domain handler |
| Unknown path | 404 from Spring | no handler of yours needed |
| Add `@ExceptionHandler(Exception.class)` returning 500 | the 405 and 415 cases now become 500 ❌ | the catch-all swallows Spring's built-in answers |
| Extend `ResponseEntityExceptionHandler` instead | 405/415/400 keep their status, now as `ProblemDetail` | the base class handles Spring's exceptions properly |

**Gotchas**
- A handler for `Exception` will swallow Spring's built-in 400/404/405 unless you extend the base class.
- Two advices that handle the same exception: `@Order` decides.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** The closest exception type wins.
2. **B.** Your handler grabs it before Spring's resolver.
3. **B.** A standard error shape clients can rely on.

</details>
