# Chapter 24 · Spring Security basics

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter24CheckTest` 13/13 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/24-spring-security-basics test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/24-spring-security-basics spring-boot:run` (port 8090; `asha`/`asha-pass`, `ops`/`ops-pass`).
- Fresh stub: 4/13 green (Boot's defaults already demand a login and know the users; must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `SecurityFilterChain`: `/public/**` open, rest authenticated, Basic | `src/main/java/ledgerly/SecurityConfig.java` | `mvn -pl chapters/24-spring-security-basics test -Popen` | task 1 checks green | ⬜ |
| 2 | `/admin/**` → `hasRole("ADMIN")` before `anyRequest` | `SecurityConfig.java` | same | task 2 checks green | ⬜ |
| 3 | CSRF off, `STATELESS` | `SecurityConfig.java` | same | task 3 checks green | ⬜ |
| 4 | JSON 401/403 via entry point + access-denied handler | `SecurityConfig.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** authn vs authz, 401/403, order, CSRF | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Authentication vs authorization; 401 vs 403?
- URL rules; why does order matter?
- Why disable CSRF here; when keep it?

## ⚠️ Traps

- `anyRequest()` last.
- `hasRole` adds `ROLE_`.
- CSRF off only for stateless, header-auth APIs.

## ❓ Checks (letters only)

1. asha (USER) calls `/admin/audit`: A. 401 · B. 403
2. Rules: `/accounts/**` authenticated, then `/accounts/rates` permitAll. Anonymous `/accounts/rates`: A. 200 · B. 401
3. `hasRole("ADMIN")` matches the authority: A. `ADMIN` · B. `ROLE_ADMIN`

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** Every bank API sits behind Spring Security, and 401 vs 403, filter chain order and CSRF are the first three security questions in a Spring interview.

**The concept.** Spring Security is a chain of servlet filters in front of your controllers. One filter authenticates (Basic, form, JWT); a later one checks your `authorizeHttpRequests` rules against the request, top to bottom, and stops at the first rule that matches.

**Small example.** Users: asha (USER), ops (USER, ADMIN). Real results:

| Request | Who | First matching rule | Result | Why |
|---|---|---|---|---|
| `GET /public/rates` | nobody | `/public/**` permitAll | 200 | open by rule |
| `GET /accounts/me` | nobody | `anyRequest` authenticated | 401 | no credentials: authentication failed |
| `GET /accounts/me` | asha | `anyRequest` authenticated | 200 | logged in is enough |
| `GET /admin/audit` | asha | `/admin/**` hasRole ADMIN | 403 | known user, missing role |
| `GET /admin/audit` | ops | `/admin/**` hasRole ADMIN | 200 | has `ROLE_ADMIN` |
| `POST /transfers`, no CSRF token | asha | `anyRequest` | 201 | CSRF disabled for this stateless API |

**Gotchas**
- `PasswordEncoder` is required; store BCrypt hashes, never plain text (chapter 25).
- Security filters run before `@RestControllerAdvice`: their errors need the entry point/handler, not an advice (chapter 31).
- `@WebMvcTest` also loads your `SecurityFilterChain`; tests need `httpBasic(...)` or `@WithMockUser`.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** Authenticated, not authorized.
2. **B.** `/accounts/**` matched first.
3. **B.** `hasRole` prefixes `ROLE_`.

</details>
