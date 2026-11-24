# Chapter 25 · Users from the database

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter25CheckTest` 12/12 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/25-users-from-the-database test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/25-users-from-the-database spring-boot:run` (port 8091; H2 in memory, no Docker).
- Fresh stub: 1/12 green (the `@WithMockUser` example).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `loadUserByUsername`: hash, role, disabled, not found | `src/main/java/ledgerly/DbUserDetailsService.java` | `mvn -pl chapters/25-users-from-the-database test -Popen` | task 1 checks green | ⬜ |
| 2 | `register`: 409 on duplicate, `encoder.encode` | `RegistrationService.java` | same | task 2 checks green | ⬜ |
| 3 | `ProviderManager(DaoAuthenticationProvider)`; log without password | `SecurityConfig.java`, `AuthService.java` | same | task 3 checks green | ⬜ |
| 4 | **(Interview)** UDS, BCrypt, vague errors, logs | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 4)

- How is a user loaded from your DB at login?
- How are passwords stored; why BCrypt?
- Why one vague error; what never goes in logs?

## ⚠️ Traps

- `matches`, not `equals`, for hashes.
- Don't reveal which part was wrong.
- Never log passwords.

## ❓ Checks (letters only)

1. `encoder.encode("asha-pass")` called twice gives: A. the same string · B. two different strings
2. Unknown username at login, default `DaoAuthenticationProvider`. The caller sees: A. `UsernameNotFoundException` · B. `Bad credentials`
3. Why BCrypt over SHA-256? A. shorter output · B. salted and deliberately slow

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** Password storage is an audit item at every bank and a standard interview question ("how do you store passwords?"). Getting it wrong is a breach even if nobody has broken in yet.

**The concept.** Spring Security never touches your table directly: it calls `UserDetailsService.loadUserByUsername`, gets a `UserDetails` (username, hash, authorities, flags), and lets the `PasswordEncoder` compare the typed password with the hash.

**Small example.** Real rows after startup (hashes cut):

| username | password_hash | role | enabled |
|---|---|---|---|
| asha | `$2a$10$Q3w…` | USER | true |
| ops | `$2a$10$7Lk…` | ADMIN | true |
| ravi | `$2a$10$pZx…` | USER | false |

| Login attempt | Result | Why |
|---|---|---|
| asha / asha-pass | 200 | hash matches, enabled |
| asha / guess | 401 `Bad credentials` | hash does not match |
| nobody / guess | 401 `Bad credentials` | unknown user, same answer on purpose |
| ravi / ravi-pass | 401 | right password, but `disabled(true)` |

**Gotchas**
- `{bcrypt}$2a$...` prefixes come from `DelegatingPasswordEncoder` (lets you migrate algorithms later).
- BCrypt only uses the first 72 bytes of a password.
- `@WithMockUser` skips authentication entirely: it tests authorization rules, not your `UserDetailsService`.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** A new random salt each time.
2. **B.** `hideUserNotFoundExceptions` is on by default.
3. **B.** Salt defeats rainbow tables; slowness defeats brute force.

</details>
