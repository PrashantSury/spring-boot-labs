# Chapter 29 · Method security

**Time:** ~50 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter29CheckTest` 14/14 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/29-method-security test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/29-method-security spring-boot:run` (port 8094; `asha`, `meera`, `ravi`, `ops`, password = name + `-pass`).
- Fresh stub: 4/14 green: the "allowed" cases (ops closes, ravi lists and reads) and the self-invocation example. They must stay green.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@EnableMethodSecurity` + `close` ADMIN only | `src/main/java/ledgerly/SecurityConfig.java`, `AccountService.java` | `mvn -pl chapters/29-method-security test -Popen` | task 1 checks green | ⬜ |
| 2 | `@PreAuthorize` ownership on `accountsOf` | `AccountService.java` | same | task 2 checks green | ⬜ |
| 3 | `@PostAuthorize` on `find` | `AccountService.java` | same | task 3 checks green | ⬜ |
| 4 | `TransferLimits.allowed` + `@PreAuthorize("@transferLimits...")` | `TransferLimits.java`, `AccountService.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** proxies, pre/post, custom checks | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- How does method security work? Pre vs Post.
- Ownership rule / custom check; how to test.
- Why can a check be skipped?

## ⚠️ Traps

- No `@EnableMethodSecurity` → nothing enforced.
- `this.method()` skips the proxy.
- `@PostAuthorize` only on reads.

## ❓ Checks (letters only)

1. `@PreAuthorize` is on `close`, but `closeEmptyAccounts()` in the same class calls `this.close(3)` as a USER. Result: A. AccessDeniedException · B. account closed
2. The owner of an account is only known after loading it. Use: A. `@PreAuthorize` · B. `@PostAuthorize`
3. Method security denies a call made from a controller. The client gets: A. 500 · B. 403

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "How do you make sure a user can only see their own data?" is asked in nearly every backend interview. Insecure direct object access (changing `/accounts/1` to `/accounts/2`) is the top API vulnerability in the OWASP list.

**The concept.** Spring wraps `AccountService` in a proxy. When someone calls `service.find(1)`, the call goes to the proxy first; the proxy evaluates the SpEL expression with `authentication` (the logged-in user), the method arguments (`#id`) and, after the call, `returnObject`. False → `AccessDeniedException` → 403.

**Small example.** Account 1 = asha (82,000), accounts 2 and 3 = meera. Real results:

| Call | As | Rule | Result | Why |
|---|---|---|---|---|
| `GET /accounts/2` | meera | post: owner = me | 200 | her account |
| `GET /accounts/1` | meera | post: owner = me | 403 | loaded, then refused |
| `GET /customers/asha/accounts` | ravi | pre: me or TELLER | 200 | teller |
| `POST /transfers amount=60000` | asha | `@transferLimits` | 403 | USER limit 50,000 |
| same | ravi | `@transferLimits` | 200 | TELLER limit 5,00,000 |
| `DELETE /accounts/2` | asha | `hasRole('ADMIN')` | 403 | not an admin |
| `closeEmptyAccounts()` | asha | (none reached) | closes 3 | `this.close` skips the proxy |

**Gotchas**
- Method security and URL rules both apply; use URL rules for coarse "logged in / staff area" and methods for data rules.
- `@WithMockUser(roles = "TELLER")` gives `ROLE_TELLER` to the test's `SecurityContext`, so you can call the service directly.
- A denied `@PostAuthorize` still ran the method (and its SQL).

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** The internal call never goes through the proxy.
2. **B.** Only after the call is `returnObject` known.
3. **B.** `AccessDeniedException` is translated by the security filter into 403.

</details>
