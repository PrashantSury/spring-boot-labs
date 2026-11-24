# Chapter 14 · Filters and interceptors

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter14CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter14CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Servlet `Filter` vs `HandlerInterceptor`: where each runs | not written yet | — | checks not written yet | ⬜ |
| 2 | `OncePerRequestFilter` and registering filter order | not written yet | — | checks not written yet | ⬜ |
| 3 | Logging request id with MDC | not written yet | — | checks not written yet | ⬜ |
| 4 | What an interceptor can and cannot see (it does not see 404s for unmapped paths) | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Filter vs interceptor: where does each run, and which one sees a 404 for an unmapped path?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Exceptions thrown in a filter do not reach `@ControllerAdvice`
- Filter order decides who runs first, and security filters have their own chain

## ❓ Checks (letters only)

1. Which one runs for a request to an unmapped path? A. the interceptor · B. the filter · C. neither
2. An exception thrown in a filter is handled by `@ControllerAdvice`: A. yes · B. no
3. Why `MDC.remove` in `finally`? A. style · B. pooled threads would carry the old id into the next request

<details><summary>📖 Teaching</summary>

**Why it matters.** Every request needs a correlation id in the logs, and some paths need a quick check before the controller.

**The concept.** A servlet `Filter` runs around the whole `DispatcherServlet`. A `HandlerInterceptor` runs inside it, only once a handler has been chosen.

**Small example.**

```java
@Component
class CorrelationIdFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String id = Optional.ofNullable(req.getHeader("X-Request-Id")).orElse(UUID.randomUUID().toString());
        MDC.put("requestId", id);
        res.setHeader("X-Request-Id", id);
        try { chain.doFilter(req, res); } finally { MDC.remove("requestId"); }
    }
}
```

| Case | Result | In or out, and why |
|---|---|---|
| `GET /accounts/42` with `X-Request-Id: abc` | every log line shows `requestId=abc`; the response echoes it | the filter wraps everything |
| `GET /nope` (unmapped) | the filter runs; an interceptor's `preHandle` does not | no handler, so no interceptor |
| The filter throws `IllegalStateException` | your `@ControllerAdvice` never sees it | the filter runs outside the `DispatcherServlet` |
| Forgetting `MDC.remove` in `finally` | the next request on that thread logs the old id ❌ | threads are pooled and reused |

**Gotchas**
- Exceptions thrown in a filter do not reach `@ControllerAdvice`.
- Filter order decides who runs first, and security filters have their own chain.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Interceptors need a chosen handler.
2. **B.** Advice lives inside the `DispatcherServlet`.
3. **B.** MDC is thread-local.

</details>
