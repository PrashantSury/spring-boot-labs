# Chapter 24 · Spring Security basics

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter24CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter24CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | The default login, the generated password and the filter chain | not written yet | — | checks not written yet | ⬜ |
| 2 | `SecurityFilterChain` bean and `authorizeHttpRequests` | not written yet | — | checks not written yet | ⬜ |
| 3 | Authentication vs authorization | not written yet | — | checks not written yet | ⬜ |
| 4 | 401 vs 403 and which one means what | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- 401 vs 403: what does each mean? What did the starter do the moment you added it?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Order of `requestMatchers` matters; the first match wins
- CSRF protection blocks POSTs in a stateless API until you configure it

## ❓ Checks (letters only)

1. No credentials on a protected path: A. 401 · B. 403
2. Logged in, but not allowed: A. 401 · B. 403
3. Two matchers both match a path. Which applies? A. the last one · B. the first one

<details><summary>📖 Teaching</summary>

**Why it matters.** Adding the security starter locks every endpoint. Understand what it did before you change it.

**The concept.** Adding the security starter puts a filter chain in front of every request. Your own `SecurityFilterChain` bean replaces the default and says which paths need what.

**Small example.**

```java
@Bean
SecurityFilterChain api(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())                       // stateless JSON API, no cookies
        .authorizeHttpRequests(a -> a
            .requestMatchers("/branches/**").permitAll()
            .requestMatchers("/accounts/**").authenticated()
            .anyRequest().denyAll())
        .httpBasic(Customizer.withDefaults())
        .build();
}
```

| Case | Result | In or out, and why |
|---|---|---|
| Starter added, no config, `GET /branches` | 401; the log prints `Using generated security password: ...` | everything is locked by default |
| With this chain, `GET /branches` | 200 | `permitAll` |
| `GET /accounts/42`, no credentials | 401 | authentication missing |
| `GET /accounts/42`, logged in but the rule needs a role you lack | 403 | authorization failed |
| `.anyRequest()` placed before `/branches/**` | startup fails | `anyRequest` must be last; the first matching rule wins |

**Gotchas**
- Order of `requestMatchers` matters; the first match wins.
- CSRF protection blocks POSTs in a stateless API until you configure it.

</details>

<details><summary>🔒 Answers</summary>

1. **A.** Not authenticated.
2. **B.** Authenticated, not authorised.
3. **B.** Rules are checked in order.

</details>
