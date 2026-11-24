# Chapter 25: Users from the database

By the end of this chapter Ledgerly's logins live in a database table: Spring Security loads them through your `UserDetailsService`, passwords are stored as BCrypt hashes, a disabled user cannot log in, and failed logins are vague to the caller and safe in the log.

## Why you need this

Chapter 24 kept two users in memory. Ledgerly has 2 million customers, who sign up, get disabled after fraud, and change passwords. An internal audit also found an old table with passwords in plain text, and a log line that printed `login failed for asha with password Asha@1990`. Both are reportable incidents at a bank.

## What you will learn

- **`UserDetailsService` / `UserDetails`:** how Spring Security asks "who is asha?".
- **`PasswordEncoder` + BCrypt:** salted, deliberately slow hashes.
- **`AuthenticationManager` → `DaoAuthenticationProvider`:** load user, compare hash.
- **Disabled accounts:** `UserDetails.isEnabled()`.
- **`BadCredentialsException`:** the same answer for "no such user" and "wrong password".
- **`@WithMockUser`:** test role rules without passwords.

## Real output

The finished app on port 8091 (asha/asha-pass USER, ops/ops-pass ADMIN, ravi/ravi-pass disabled):

```
$ curl -u asha:asha-pass localhost:8091/me
hello asha
$ curl -o /dev/null -w "ravi: %{http_code}\n" -u ravi:ravi-pass localhost:8091/me
ravi: 401

$ curl -H Content-Type:application/json -d '{"username":"meera","password":"s3cret-Pass"}' localhost:8091/auth/register
user id 4
$ curl -H Content-Type:application/json -d '{"username":"asha","password":"x"}' localhost:8091/auth/register
{"timestamp":"2026-11-24T10:34:31.146Z","status":409,"error":"Conflict","path":"/auth/register"}

$ curl -H Content-Type:application/json -d '{"username":"asha","password":"guess"}' localhost:8091/auth/check
{"error":"Bad credentials"}
$ curl -H Content-Type:application/json -d '{"username":"nobody","password":"guess"}' localhost:8091/auth/check
{"error":"Bad credentials"}
$ curl -H Content-Type:application/json -d '{"username":"meera","password":"s3cret-Pass"}' localhost:8091/auth/check
{"user":"meera"}
```

The app log (real):

```
WARN ... ledgerly.AuthService : login failed for asha
WARN ... ledgerly.AuthService : login failed for nobody
```

While writing this chapter the duplicate-username call first answered **401**, not 409: `@ResponseStatus` forwards to `/error`, and `/error` needed a login. The finished chain permits `/error`.

## Try it

```bash
mvn -pl chapters/25-users-from-the-database spring-boot:run
mvn -pl chapters/25-users-from-the-database test -Popen
```

## Your turn

You edit `DbUserDetailsService.java`, `RegistrationService.java`, `SecurityConfig.java` (only the `authenticationManager` bean), `AuthService.java` and `interview-answer.md`.

1. **`UserDetailsService`.** Load by username; hash, role, `disabled(!enabled)`; unknown → `UsernameNotFoundException`.
2. **Registration.** 409 on a taken name; store `encoder.encode(raw)`.
3. **AuthenticationManager.** `ProviderManager(DaoAuthenticationProvider)`; log failures without the password.
4. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- `password(u.getPasswordHash())` is the hash; never encode it again on load.
- `BCryptPasswordEncoder.encode` gives a different string every time (random salt): compare with `matches`, not `equals`.
- Telling the caller "unknown user" lets attackers list valid usernames. `DaoAuthenticationProvider` hides it on purpose.
- Logging the password, or the whole request body of `/auth/*`.
- Forgetting `/error` in `permitAll`: error statuses turn into 401.

**Interview question (task 4).** How does Spring Security load a user from your DB? How should passwords be stored, and why BCrypt? Why the same error for unknown user and wrong password, and what never goes in logs? The questions are repeated at the top of `interview-answer.md`.
