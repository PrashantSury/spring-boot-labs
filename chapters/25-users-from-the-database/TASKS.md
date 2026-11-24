# Chapter 25 · Users from the database

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter25CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter25CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `UserDetailsService` and `UserDetails` | not written yet | — | checks not written yet | ⬜ |
| 2 | `PasswordEncoder` and BCrypt | not written yet | — | checks not written yet | ⬜ |
| 3 | `AuthenticationManager` and `DaoAuthenticationProvider` | not written yet | — | checks not written yet | ⬜ |
| 4 | Testing with `@WithMockUser` | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Why does Spring answer the same way for an unknown user and a wrong password? Why BCrypt and not SHA-256?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Never store or log a plain password
- `BadCredentialsException` is deliberately vague about which part was wrong

## ❓ Checks (letters only)

1. Unknown username at login gives the client: A. "user not found" · B. the same failure as a wrong password
2. The database column holds: A. the password · B. a BCrypt hash
3. Why is BCrypt slow on purpose? A. a bug · B. to make guessing leaked hashes expensive

<details><summary>📖 Teaching</summary>

**Why it matters.** Bank staff are stored in a table, with hashed passwords, not in a config file.

**The concept.** Spring Security loads a user through `UserDetailsService` and checks the password with a `PasswordEncoder`. The database stores only a BCrypt hash.

**Small example.**

```java
@Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

@Service
class StaffUserDetailsService implements UserDetailsService {
    public UserDetails loadUserByUsername(String username) {
        Staff s = repo.findByUsernameIgnoreCase(username)
                      .orElseThrow(() -> new UsernameNotFoundException("not found"));
        return User.withUsername(s.getUsername()).password(s.getPasswordHash())
                   .roles(s.getRole()).disabled(!s.isEnabled()).build();
    }
}
```

| Case | Result | In or out, and why |
|---|---|---|
| Right username and password | authenticated | `matches(raw, hash)` is true |
| Right username, wrong password | `BadCredentialsException` | vague on purpose |
| Unknown username | also `BadCredentialsException` | the not-found is hidden so usernames can't be probed |
| `encode("<pw>")` twice | two different hashes, both start `$2a$10$` | a random salt each time |
| A test with `@WithMockUser(roles = "TELLER")` | runs as an authenticated teller | no real login needed |

**Gotchas**
- Never store or log a plain password.
- `BadCredentialsException` is deliberately vague about which part was wrong.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Otherwise attackers learn which usernames exist.
2. **B.** One-way, salted, slow.
3. **B.** Speed helps attackers.

</details>
