# Chapter 28 · Roles and authorities

**Time:** ~45 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter28CheckTest` 12/12 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/28-roles-and-authorities test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/28-roles-and-authorities spring-boot:run` (port 8093; users `asha`, `meera`, `ravi`, `ops`).
- Fresh stub: 0/12 green.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `"authorities"` claim in the token | `src/main/java/ledgerly/TokenService.java` | `mvn -pl chapters/28-roles-and-authorities test -Popen` | task 1 checks green | ⬜ |
| 2 | `TokenService.authorities(claims)` | `TokenService.java` | same | task 2 checks green | ⬜ |
| 3 | URL rules: `hasAuthority` / `hasRole` | `SecurityConfig.java` | same | task 3 checks green | ⬜ |
| 4 | `RoleHierarchy` bean | `SecurityConfig.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** roles, authorities, hierarchy | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Role vs authority; what does `ROLE_` do?
- When `hasAuthority` instead of `hasRole`?
- Authorities in the JWT: gain and cost; role hierarchy.

## ⚠️ Traps

- `hasRole("ROLE_X")` / `hasAuthority("X")` for a role.
- Specific matchers before `anyRequest()`.
- Hierarchy ≠ extra authorities.

## ❓ Checks (letters only)

1. User has `ROLE_TELLER`. Which rule lets them in? A. `hasAuthority("TELLER")` · B. `hasRole("TELLER")`
2. Ops (only `ROLE_ADMIN`, hierarchy ADMIN > TELLER) calls a `hasAuthority("report:read")` endpoint: A. 200 · B. 403
3. You remove `transfer:write` from asha in the database. Her 15-minute token issued a minute ago: A. stops working at once · B. still works until it expires

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** Almost every real API has "only some logged-in users may do this". Interviewers ask the `ROLE_` question to see whether you have actually configured Spring Security.

**The concept.** Think of a bank badge. The colour says who you are (customer, teller, admin): that is the **role**. The stickers say what you may do (send money, read reports): those are **authorities**. Spring stores both as plain strings on the `Authentication`; a role is just a string that starts with `ROLE_`. `hasRole("TELLER")` is shorthand for `hasAuthority("ROLE_TELLER")`.

**Small example.** Users: asha = `ROLE_USER, transfer:write`; meera = `ROLE_USER`; ravi = `ROLE_TELLER, report:read`; ops = `ROLE_ADMIN`. Hierarchy ADMIN > TELLER > USER. Real results:

| Request | Rule | asha | meera | ravi | ops | Why |
|---|---|---|---|---|---|---|
| `POST /transfers` | `hasAuthority("transfer:write")` | 201 ✅ | 403 | 403 | 403 | only asha has the sticker |
| `GET /reports/daily` | `hasAuthority("report:read")` | 403 | 403 | 200 ✅ | 403 | the hierarchy does not hand out stickers |
| `GET /teller/cash-drawer` | `hasRole("TELLER")` | 403 | 403 | 200 ✅ | 200 ✅ | ADMIN includes TELLER |
| `GET /admin/audit` | `hasRole("ADMIN")` | 403 | 403 | 403 | 200 ✅ | top of the hierarchy only |
| `GET /me` | `authenticated()` | 200 | 200 | 200 | 200 | everyone logged in |

**Gotchas**
- With authorities in the token, the filter needs no database call per request, but changes take effect only on the next token.
- `@WithMockUser(roles = "ADMIN")` adds `ROLE_ADMIN`; `@WithMockUser(authorities = "report:read")` adds exactly that string.
- Spring Security 6+ rejects `hasRole("ROLE_ADMIN")` with an exception at startup: a helpful failure.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** `hasRole` adds `ROLE_`; `hasAuthority("TELLER")` looks for the bare string.
2. **B.** The hierarchy expands roles, not other authorities.
3. **B.** The token is a snapshot; that is why access tokens are short-lived.

</details>
