# Chapter 39 · Project: secure transfers

**Time:** ~75 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter39CheckTest` 12/12 ✅ + `SecurityCasesTest` 3/3 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/39-project-secure-transfers test -Popen` (runs both test classes; without `-Popen` they are skipped).
- App: `mvn -pl chapters/39-project-secure-transfers spring-boot:run` (port 8100; users `asha`, `ravi`, `meera`, `tara`, `audit`, password = name + `-pass`). H2, no Docker.
- Fresh stub: 2/15 green: the example and "asha moves money from her own account". Both must stay green.
- Spoiler: the transfer logic is chapter 38's finished solution.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@EnableMethodSecurity` + URL rules | `src/main/java/ledgerly/SecurityConfig.java` | `mvn -pl chapters/39-project-secure-transfers test -Popen` | task 1 checks green | ⬜ |
| 2 | `Ownership.owns` + `@PreAuthorize` on `transfer` | `Ownership.java`, `TransferService.java` | same | task 2 checks green | ⬜ |
| 3 | `@PreAuthorize` on `account`; `involved` + `@PostAuthorize` on `find` | `Ownership.java`, `TransferService.java` | same | task 3 checks green | ⬜ |
| 4 | write 3 negative security tests | `src/test/java/ledgerly/SecurityCasesTest.java` | same | `SecurityCasesTest` 3/3 | ⬜ |
| 5 | **(Interview)** JWT, roles, ownership, testing | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- JWT login and roles; URL rules vs method security.
- Ownership; why not trust the id?
- Testing; 401 vs 403.

## ⚠️ Traps

- Logged in ≠ allowed (IDOR).
- Unknown account → 403, not 404.
- No `@PostAuthorize` on writes.

## ❓ Checks (letters only)

1. Ravi is logged in and sends `"from":"ACC-1"` (Asha's). Without an ownership check: A. 403 · B. the money moves
2. "Only auditors under `/audit/**`" belongs in: A. the URL rules · B. an ownership bean
3. Meera asks for a transfer between Asha and Ravi. Which annotation can decide using the loaded transfer? A. `@PreAuthorize` · B. `@PostAuthorize`

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "How do you make sure users only access their own data?" is the security question for any backend role, and IDOR tops the OWASP API Security list. A good answer names both layers and how you test them.

**The concept.** The building has a guard at the door (the JWT filter: *who are you?*), signs on each floor (URL rules: *tellers' floor, auditors' floor*), and a clerk at the counter who checks that the account book you ask for has your name on it (method security + ownership). The door and the signs are cheap and catch whole groups. Only the clerk can catch Ravi asking for Asha's book, because only the clerk looks inside.

**Small example.** Real results from the tests:

| Who | Request | Status | Which layer decides |
|---|---|---|---|
| nobody | `GET /accounts/ACC-1` | 401 | filter: no token |
| asha | `GET /audit/transfers` | 403 | URL rule |
| audit | `POST /transfers` | 403 | URL rule |
| ravi | transfer from ACC-1 | 403, ACC-1 still 1000 | `@PreAuthorize` + `owns` |
| ravi | transfer from ACC-9 (doesn't exist) | 403 | `owns` = false |
| tara | transfer from meera's ACC-4 | 201 | `hasRole('TELLER')` |
| ravi | `GET /accounts/ACC-1` | 403 | `@PreAuthorize` |
| ravi | asha→ravi transfer | 200 | `@PostAuthorize`: involved |
| meera | asha→ravi transfer | 403 | `@PostAuthorize` |

**Gotchas**
- `hasRole('TELLER')` checks the authority `ROLE_TELLER`; `.roles("TELLER")` adds the prefix for you.
- `#request.from()` needs parameter names at runtime; Boot's Maven setup compiles with `-parameters`.
- Method security sits on the service, so a future second controller (or a message listener) is protected too.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** The URL rule only knows "a customer may POST".
2. **A.** A whole area, by role.
3. **B.** It sees `returnObject`.

</details>
