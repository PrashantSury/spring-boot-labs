# Chapter 27 · JWT login and the JWT filter

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter27CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter27CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `/auth/login` returning a token | not written yet | — | checks not written yet | ⬜ |
| 2 | A `OncePerRequestFilter` that reads `Authorization: Bearer` | not written yet | — | checks not written yet | ⬜ |
| 3 | Setting the `SecurityContext` | not written yet | — | checks not written yet | ⬜ |
| 4 | Stateless session policy | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Walk a request with a Bearer token through your filter. Where does a bad token get turned into 401?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Place the JWT filter before `UsernamePasswordAuthenticationFilter`
- An exception thrown inside the filter skips `@ControllerAdvice`

## ❓ Checks (letters only)

1. Where does the JWT filter go in the chain? A. after the controller · B. before `UsernamePasswordAuthenticationFilter`
2. An exception thrown in the JWT filter is handled by `@ControllerAdvice`: A. yes · B. no
3. `SessionCreationPolicy.STATELESS` means: A. Spring never creates an HTTP session for security · B. tokens expire faster

<details><summary>📖 Teaching</summary>

**Why it matters.** A customer posts a username and password once, then sends a token with each request.

**The concept.** `/auth/login` checks the password once and returns a token. A `OncePerRequestFilter` reads `Authorization: Bearer <token>` on every later call and puts the user into the `SecurityContext`.

**Small example.**

```java
class JwtFilter extends OncePerRequestFilter {
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            Authentication auth = tokens.toAuthentication(h.substring(7));   // verifies signature + exp
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(req, res);
    }
}
// http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
//     .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

| Case | Result | In or out, and why |
|---|---|---|
| `POST /auth/login` with good credentials | 200 `{"accessToken":"eyJ..."}` | the password is checked once |
| `GET /accounts/42` with `Authorization: Bearer eyJ...` | 200 | the filter sets the authentication |
| Same call with no header | 401 | nothing in the `SecurityContext` |
| An expired token makes `toAuthentication` throw inside the filter | your `@ControllerAdvice` does not see it | filters run before the `DispatcherServlet`; answer 401 from the filter or the entry point |
| Session policy left at the default | a `JSESSIONID` cookie may appear | set `STATELESS` for a token API |

**Gotchas**
- Place the JWT filter before `UsernamePasswordAuthenticationFilter`.
- An exception thrown inside the filter skips `@ControllerAdvice`.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Authentication must be set before authorization runs.
2. **B.** It never reaches the `DispatcherServlet`.
3. **A.** Every request carries its own proof.

</details>
