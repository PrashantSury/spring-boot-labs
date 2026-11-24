# Chapter 31 · Security errors

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter31CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter31CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `AuthenticationEntryPoint` for 401 | not written yet | — | checks not written yet | ⬜ |
| 2 | `AccessDeniedHandler` for 403 | not written yet | — | checks not written yet | ⬜ |
| 3 | Where security exceptions are handled vs `@ControllerAdvice` | not written yet | — | checks not written yet | ⬜ |
| 4 | MockMvc tests for 401, 403 and 200 | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Where is a 401 produced, where is a 403 produced, and why can't `@ControllerAdvice` handle the filter ones?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- `@ExceptionHandler` does not catch security-filter exceptions
- A missing token is 401; a valid token without permission is 403

## ❓ Checks (letters only)

1. Missing token. Which component answers? A. `AccessDeniedHandler` · B. `AuthenticationEntryPoint`
2. Valid token, wrong role on a URL rule. Status: A. 401 · B. 403
3. `@ExceptionHandler` catches exceptions thrown in security filters: A. yes · B. no

<details><summary>📖 Teaching</summary>

**Why it matters.** Clients must get a clean JSON 401 or 403, not an HTML login page or an empty body.

**The concept.** Security failures happen in filters, before any controller. An `AuthenticationEntryPoint` writes the 401 body and an `AccessDeniedHandler` writes the 403 body.

**Small example.**

```java
http.exceptionHandling(e -> e
    .authenticationEntryPoint((req, res, ex) -> {
        res.setStatus(401); res.setContentType("application/json");
        res.getWriter().write("{\"status\":401,\"message\":\"Login required\"}");
    })
    .accessDeniedHandler((req, res, ex) -> {
        res.setStatus(403); res.setContentType("application/json");
        res.getWriter().write("{\"status\":403,\"message\":\"Access denied\"}");
    }));
```

| Case | Result | In or out, and why |
|---|---|---|
| No token | 401 `{"status":401,"message":"Login required"}` | the entry point |
| Valid token, lacks the role (URL rule) | 403 `{"status":403,...}` | the access-denied handler |
| Valid token, `@PreAuthorize` denies inside a controller method | handled by your `@ControllerAdvice` if it maps `AccessDeniedException` | that exception is thrown inside the `DispatcherServlet` |
| No custom handlers, browser request | possibly a login page or an empty body | defaults are made for web apps |

**Gotchas**
- `@ExceptionHandler` does not catch security-filter exceptions.
- A missing token is 401; a valid token without permission is 403.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Not authenticated → entry point.
2. **B.** Authenticated but not allowed.
3. **B.** Filters run before the `DispatcherServlet`.

</details>
