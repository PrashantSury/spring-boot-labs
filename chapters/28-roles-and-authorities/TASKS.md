# Chapter 28 · Roles and authorities

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter28CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter28CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Roles vs authorities and the `ROLE_` prefix | not written yet | — | checks not written yet | ⬜ |
| 2 | `hasRole`, `hasAuthority` in the filter chain | not written yet | — | checks not written yet | ⬜ |
| 3 | Putting roles in the token and reading them back | not written yet | — | checks not written yet | ⬜ |
| 4 | Role hierarchy | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Roles vs authorities: how do they differ in Spring, and how do you model teller, manager and auditor?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- `hasRole("ADMIN")` looks for `ROLE_ADMIN`
- Path rules and method rules both apply; a request must pass both

## ❓ Checks (letters only)

1. `hasRole("TELLER")` checks for the authority: A. `TELLER` · B. `ROLE_TELLER`
2. `hasAuthority("TRANSFER_APPROVE")` adds a prefix: A. yes · B. no
3. A URL rule allows, a `@PreAuthorize` denies. Result: A. 200 · B. 403

<details><summary>📖 Teaching</summary>

**Why it matters.** A teller can view accounts, a manager can approve transfers, an auditor can only read.

**The concept.** An authority is any string Spring checks, like `TRANSFER_APPROVE`. A role is an authority with the `ROLE_` prefix, so `hasRole("MANAGER")` looks for `ROLE_MANAGER`.

**Small example.**

```java
.authorizeHttpRequests(a -> a
    .requestMatchers(HttpMethod.GET, "/accounts/**").hasAnyRole("TELLER", "MANAGER", "AUDITOR")
    .requestMatchers("/transfers/*/approve").hasAuthority("TRANSFER_APPROVE")
    .anyRequest().authenticated())
```

| Case | Result | In or out, and why |
|---|---|---|
| Teller with `ROLE_TELLER`, `GET /accounts/42` | 200 | `hasAnyRole` adds `ROLE_` |
| Manager with `ROLE_MANAGER` + `TRANSFER_APPROVE`, approve | 200 | the authority matches |
| Auditor with `ROLE_AUDITOR`, approve | 403 | no `TRANSFER_APPROVE` |
| Token claim `roles: ["MANAGER"]` mapped without a prefix, then `hasRole("MANAGER")` | 403 ❌ | the check looks for `ROLE_MANAGER` |
| Path rule passes, a method rule on the service denies | 403 | a request must pass both |

**Gotchas**
- `hasRole("ADMIN")` looks for `ROLE_ADMIN`.
- Path rules and method rules both apply; a request must pass both.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** The prefix is added for you.
2. **B.** Authorities are matched exactly.
3. **B.** Both layers must allow.

</details>
