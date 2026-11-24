# Chapter 31 · Security errors

**Time:** ~40 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter31CheckTest` 12/12 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/31-security-errors test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/31-security-errors spring-boot:run` (port 8096; `asha`/`asha-pass`, `ops`/`ops-pass`).
- Fresh stub: 3/12 green: the examples and the two "ops is allowed" checks. They must stay green.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | 401 problem + `WWW-Authenticate` | `src/main/java/ledgerly/ProblemEntryPoint.java` | `mvn -pl chapters/31-security-errors test -Popen` | task 1 checks green | ⬜ |
| 2 | 403 problem + WARN log | `ProblemDeniedHandler.java` | same | task 2 checks green | ⬜ |
| 3 | wire into `httpBasic` + `exceptionHandling` | `SecurityConfig.java` | same | task 3 checks green | ⬜ |
| 4 | re-throw `AccessDeniedException` in the advice | `ApiErrors.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** 401 vs 403, filters vs advice | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- 401 vs 403; which components?
- Why not `@ControllerAdvice`; how to customise?
- Catch-all + method security?

## ⚠️ Traps

- Advice can't see filter exceptions.
- Wire the entry point into `httpBasic` too.
- Don't leak the reason in a 401.

## ❓ Checks (letters only)

1. A request with no credentials to a protected URL is answered by: A. `@ControllerAdvice` · B. the `AuthenticationEntryPoint`
2. A logged-in user without the role: A. 401 · B. 403
3. A `@PreAuthorize` denial with a `@ExceptionHandler(Exception.class)` returning 500 and no other handler gives: A. 403 · B. 500

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "401 or 403?" is a classic screening question, and "why does my custom error handler not work for security?" is one of the most asked Spring Security questions online.

**The concept.** A request passes the security guard (filters) before it reaches the bank counter (controllers). `@ControllerAdvice` works at the counter. The guard has two scripts of its own: the **entry point** ("please show your ID", 401) and the **denied handler** ("you're not allowed in here", 403). `ExceptionTranslationFilter` chooses the script. One exception: a `@PreAuthorize` refusal happens *at* the counter, so the advice sees it first and must step aside.

**Small example.** Real results:

| Request | Who answers | Status | Body |
|---|---|---|---|
| `GET /me`, no login | entry point | 401 | problem `Login required` |
| `GET /me`, wrong password | `httpBasic` entry point (yours after task 3) | 401 | same problem |
| `GET /admin/audit` as asha | denied handler | 403 | problem `Forbidden` |
| `POST /accounts/1/freeze` as asha | advice re-throws → denied handler | 403 | problem `Forbidden` |
| `GET /accounts/9` as asha | `@ControllerAdvice` | 404 | problem `no account 9` |
| `GET /accounts/9`, no login | entry point | 401 | security first |

**Gotchas**
- In a JWT filter (chapter 27) the filter writes its own 401; use the same `Problems.write` there.
- `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` is the built-in "just a status" entry point.
- 403 handlers are a good place for an audit log; 401s are noisy (every expired token).

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** The advice is never reached.
2. **B.** Known user, missing permission.
3. **B.** The catch-all wins unless a more specific handler re-throws.

</details>
