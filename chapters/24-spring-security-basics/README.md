# Chapter 24: Spring Security basics

By the end of this chapter Ledgerly's API has its own security rules: public rates, logged-in customers, an admin-only audit page, no CSRF trouble for a stateless API, and JSON bodies that tell the app whether it got a 401 or a 403.

## Why you need this

Ledgerly's first release added `spring-boot-starter-security` and shipped with the defaults. The rates page (meant for the public website) asked for a password, every `POST /transfers` from the app was refused even with the right password, and a later "quick fix" let any customer open `/admin/audit`. Security that you did not configure yourself is security you don't understand.

## What you will learn

- **The defaults:** no `SecurityFilterChain` bean → everything locked, form login, a generated password, CSRF on.
- **`SecurityFilterChain` + `authorizeHttpRequests`:** `permitAll`, `authenticated`, `hasRole`.
- **First match wins:** the order of `requestMatchers`.
- **Authentication vs authorization; 401 vs 403.**
- **CSRF and sessions for a stateless API.**
- **`AuthenticationEntryPoint` / `AccessDeniedHandler`:** your own 401/403 bodies.

## Real output

The finished app on port 8090 (users `asha`/`asha-pass` = USER, `ops`/`ops-pass` = ADMIN):

```
$ curl localhost:8090/public/rates
FD 1y 7.10%

$ curl -i localhost:8090/accounts/me
HTTP/1.1 401
{"status":401,"error":"Login required"}

$ curl -u asha:asha-pass localhost:8090/accounts/me
accounts of asha

$ curl -u asha:asha-pass localhost:8090/admin/audit
{"status":403,"error":"Not allowed"}

$ curl -u ops:ops-pass localhost:8090/admin/audit
3 failed logins today

$ curl -i -u asha:asha-pass -X POST -H Content-Type:application/json -d '{}' localhost:8090/transfers
HTTP/1.1 201
transfer by asha
```

No `Set-Cookie` on any response: the API is stateless. On the stub (Boot's defaults, real): `curl -i localhost:8090/public/rates` is `HTTP/1.1 401` with `WWW-Authenticate: Basic realm="Realm"`, and the `POST /transfers` above with the right password is refused (`HTTP/1.1 401`) because the default chain wants a CSRF token.

## The shape of it

```java
@Bean
SecurityFilterChain api(HttpSecurity http) throws Exception {
  http.authorizeHttpRequests(auth -> auth
          .requestMatchers("/public/**").permitAll()
          .requestMatchers("/admin/**").hasRole("ADMIN")     // before anyRequest!
          .anyRequest().authenticated())
      .httpBasic(Customizer.withDefaults())
      .csrf(c -> c.disable())
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
  return http.build();
}
```

## Try it

```bash
mvn -pl chapters/24-spring-security-basics spring-boot:run
mvn -pl chapters/24-spring-security-basics test -Popen
```

## Your turn

You edit `SecurityConfig.java` and `interview-answer.md`. `UsersConfig` and `BankController` are finished.

1. **The chain.** `/public/**` open, everything else authenticated, HTTP Basic.
2. **Admin.** `/admin/**` → `hasRole("ADMIN")`, before `anyRequest()`.
3. **Stateless.** CSRF off, `SessionCreationPolicy.STATELESS`.
4. **JSON errors.** Entry point → 401 `Login required`, access-denied handler → 403 `Not allowed`; same entry point for `httpBasic`.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- `anyRequest().authenticated()` before `/admin/**`: Spring Security 6+ refuses to start ("Can't configure requestMatchers after anyRequest"); a broad matcher above a narrow one silently wins.
- `hasRole("ADMIN")` checks the authority `ROLE_ADMIN`; `hasAuthority("ADMIN")` does not add the prefix.
- Disabling CSRF on a cookie/session web app opens it to cross-site requests. It is safe here because nothing is sent automatically by the browser.
- 401 = "who are you?", 403 = "I know you, and no".

**Interview question (task 5).** Authentication vs authorization, 401 vs 403? URL rules and why order matters? Why disable CSRF for a stateless API, and when to keep it? The questions are repeated at the top of `interview-answer.md`.
