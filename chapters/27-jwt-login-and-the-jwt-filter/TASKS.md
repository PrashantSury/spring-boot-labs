# Chapter 27 · JWT login and the JWT filter

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter27CheckTest` 11/11 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/27-jwt-login-and-the-jwt-filter test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/27-jwt-login-and-the-jwt-filter spring-boot:run` (port 8092; `asha`/`asha-pass`, `ops`/`ops-pass`).
- Fresh stub: 1/11 green (no header → 401 `Login required`; must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `/auth/login` → token or 401 | `src/main/java/ledgerly/AuthController.java` | `mvn -pl chapters/27-jwt-login-and-the-jwt-filter test -Popen` | task 1 checks green | ⬜ |
| 2 | `JwtAuthFilter`: Bearer → verify → `SecurityContext`; 401 JSON on bad token | `JwtAuthFilter.java` | same | task 2 checks green | ⬜ |
| 3 | `addFilterBefore(..., UsernamePasswordAuthenticationFilter.class)` | `SecurityConfig.java` | same | task 3 checks green | ⬜ |
| 4 | **(Interview)** login flow, filter, error handling | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 4)

- JWT login, then later requests: walk through.
- Where is the filter; what goes in the `SecurityContext`?
- Missing/expired/forged token; why not throw?

## ⚠️ Traps

- No `@Component` on the filter.
- Don't throw from the filter.
- No header → continue, not reject.

## ❓ Checks (letters only)

1. Request without `Authorization` reaches the JWT filter. It should: A. answer 401 · B. continue; the rules decide
2. The filter is not in the chain (no `addFilterBefore`), but the token is valid. `/me` returns: A. 200 · B. 401
3. Where does the user live during a request? A. the HTTP session · B. `SecurityContextHolder` (per request, stateless)

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "How does JWT authentication work in Spring Security?" is the single most common Spring security interview question, and a filter in the wrong place or a thrown exception are the usual bugs.

**The concept.** Authentication happens once (password → token). After that, a filter early in the chain converts each request's token back into an `Authentication` and stores it in the `SecurityContextHolder`; the authorization filter later reads it like any other login.

**Small example.** Users: asha (USER), ops (USER, ADMIN). Real results:

| Request | Filter does | Rules say | Result | Why |
|---|---|---|---|---|
| `POST /auth/login` asha/asha-pass | no header → continue | `/auth/**` permitAll | 200 + token | the one time the password is sent |
| `GET /me` + asha's token | verify ✅, set context | authenticated ✅ | 200 `asha [ROLE_USER]` | token → user |
| `GET /admin/audit` + asha's token | verify ✅ | `hasRole(ADMIN)` ❌ | 403 | known user, missing role |
| `GET /me`, no header | continue | authenticated ❌ | 401 `Login required` | anonymous; the entry point answers |
| `GET /me` + token ending in `x` | verify ❌, write 401 | never reached | 401 `invalid token: bad signature` | the filter stops the request |

**Gotchas**
- Spring Security's own `oauth2ResourceServer().jwt()` does all of this with a library (Nimbus); writing it once by hand makes that configuration understandable.
- `SecurityContextHolder` is thread-local; Spring clears it after the request.
- Logging out a JWT needs a deny-list or short expiry + refresh tokens (chapter 30).

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** Public endpoints (like `/auth/login`) must still work.
2. **B.** Nobody turned the token into a user.
3. **B.** STATELESS: no session, a fresh context per request.

</details>
