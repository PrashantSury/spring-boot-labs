# Chapter 14 · Filters and interceptors

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter14CheckTest` 14/14 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/14-filters-and-interceptors test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/14-filters-and-interceptors spring-boot:run` (port 8088).
- Fresh stub: 2/14 green (MDC already clean, `/accounts` already open; both must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `CorrelationIdFilter`: reuse/generate, 400 on malformed, MDC + header, `finally` | `src/main/java/ledgerly/CorrelationIdFilter.java` | `mvn -pl chapters/14-filters-and-interceptors test -Popen` | task 1 checks green | ⬜ |
| 2 | `ApiKeyInterceptor` 401 + register for `/ops/**` | `ApiKeyInterceptor.java`, `WebConfig.java` | same | task 2 checks green | ⬜ |
| 3 | `HandlerNameInterceptor` `X-Handler` + register | `HandlerNameInterceptor.java`, `WebConfig.java` | same | task 3 checks green | ⬜ |
| 4 | `FilterRegistrationBean` for the correlation filter, order before the access log | `WebConfig.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** filter vs interceptor, MDC, order | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Filter vs interceptor: where does each run? Which for a correlation id, which for a per-endpoint check?
- How does an id get onto every log line, and why clear it?
- Filter order; why can't `@RestControllerAdvice` catch a filter's exception?

## ⚠️ Traps

- Don't throw from the filter; write the 400 yourself.
- `MDC.remove` in `finally`.
- `preHandle` → `false` needs a status, or the client sees an empty 200.

## ❓ Checks (letters only)

1. You need the name of the controller method that will run. Use: A. a servlet filter · B. a `HandlerInterceptor`
2. `CorrelationIdFilter` has order 5, `AccessLogFilter` order 2. The access log line shows: A. `cid=r-77` · B. `cid=null`
3. A filter throws `IllegalStateException`. Your `@RestControllerAdvice` catch-all: A. handles it · B. never sees it

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** Correlation ids are how real banks trace one request across services, and "filter vs interceptor" is a classic Spring interview question. Getting the order or the cleanup wrong silently corrupts the logs you need most during an incident.

**The concept.** A filter is a servlet-level wrapper: it gets the raw request, may stop it, calls `chain.doFilter` to go on, and runs again after the response. An interceptor is Spring MVC's hook around one controller method: `preHandle` (may stop), `postHandle`, `afterCompletion`.

**Small example.** Sample requests to the finished app, and who sees them:

| Request | CorrelationIdFilter | ApiKeyInterceptor | HandlerNameInterceptor | Result | Why |
|---|---|---|---|---|---|
| `GET /whoami` | ✅ generates id | ⏭ not `/ops/**` | ✅ `LedgerController#whoami` | 200 | mapped path, both layers run |
| `GET /ops/cache-stats` no key | ✅ | ✅ returns `false` | ❌ never reached | 401 | the first interceptor stopped it |
| `GET /nope` | ✅ | ⏭ | runs, handler is not a `HandlerMethod` | 404, no `X-Handler` | no controller method to name |
| `X-Correlation-Id: bad id!` | ✅ writes 400, returns | ❌ | ❌ | 400 | stopped before Spring MVC |

**Why order matters.** Filters nest like Russian dolls. Order 1 wraps order 2, so order 1's `finally` runs last. The access log writes its line on the way out; the correlation filter must still be "open" around it, so it needs the lower number.

**Gotchas**
- `OncePerRequestFilter` guards against running twice on forwards/error dispatches; a plain `Filter` does not.
- An interceptor's `postHandle` is too late to change headers for `@ResponseBody` methods (the body is already written); use `preHandle`.
- Spring Security (chapter 24) is itself one filter (`FilterChainProxy`) with its own inner chain.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** Only the interceptor gets the chosen `HandlerMethod`.
2. **B.** The correlation filter is inside the access log and removes the id before the log line is written.
3. **B.** Filters run outside the `DispatcherServlet`, where the advice lives.

</details>
