# Chapter 27: JWT login and the JWT filter

By the end of this chapter the Ledgerly app logs in once at `/auth/login`, gets a 15-minute token, and sends `Authorization: Bearer <token>` on every call; your filter turns that header into a logged-in user for the request, and rejects expired or forged tokens with a clear 401.

## Why you need this

With HTTP Basic (chapters 24–25) the phone sends the customer's password on every single request, and stores it to do so. A stolen phone or one leaked log line exposes the password itself. With a token, the password crosses the network once, and a leaked token dies on its own after 15 minutes.

## What you will learn

- **`/auth/login`:** `AuthenticationManager.authenticate(...)` → issue a JWT (your codec from chapter 26, finished here).
- **A `OncePerRequestFilter`** that reads `Authorization: Bearer`, verifies, and sets the `SecurityContext`.
- **`addFilterBefore(..., UsernamePasswordAuthenticationFilter.class)`:** where it sits in the chain.
- **Stateless sessions:** no cookie, no CSRF, every request carries its own proof.
- **Errors in filters:** the filter writes the 401 itself.

## Real output

The finished app on port 8092:

```
$ curl -H Content-Type:application/json -d '{"username":"asha","password":"asha-pass"}' localhost:8092/auth/login
{"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhc2hhIiwiaWF0IjoxNzk1NTE2Njg5LCJ...","expiresIn":900}

$ curl -H "Authorization: Bearer $T" localhost:8092/me
asha [ROLE_USER]
$ curl -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $T" localhost:8092/admin/audit
403
$ curl localhost:8092/me
{"error":"Login required"}
$ curl -H "Authorization: Bearer ${T}x" localhost:8092/me
{"error":"invalid token: bad signature"}
```

## The shape of it

```java
http.addFilterBefore(new JwtAuthFilter(codec, users), UsernamePasswordAuthenticationFilter.class);

// in the filter, for a valid token:
UserDetails user = users.loadUserByUsername(subject);
SecurityContextHolder.getContext().setAuthentication(
    UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
chain.doFilter(req, res);
```

## Try it

```bash
mvn -pl chapters/27-jwt-login-and-the-jwt-filter spring-boot:run
mvn -pl chapters/27-jwt-login-and-the-jwt-filter test -Popen
```

On the stub `/auth/login` answers 501 and every Bearer request is 401 `Login required`.

## Your turn

You edit `AuthController.java`, `JwtAuthFilter.java`, `SecurityConfig.java` (one line) and `interview-answer.md`. The codec, users and controllers are finished.

1. **Login.** Authenticate; 200 `{"token", "expiresIn"}` or 401 `Bad credentials`.
2. **Filter.** No/other header → continue; bad token → 401 `invalid token: <reason>`, stop; good → `SecurityContext`, continue.
3. **Chain.** `addFilterBefore(new JwtAuthFilter(codec, users), UsernamePasswordAuthenticationFilter.class)`.
4. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- `@Component` on the filter: Boot registers it a second time as a plain servlet filter, outside the security chain.
- Throwing `JwtException` from the filter: it never reaches an advice; the client gets a generic error.
- Rejecting requests without a header in the filter: then `/auth/login` itself needs a token. Let the rules decide.
- Trusting roles written in the token forever: here roles are loaded fresh, so removing ADMIN takes effect at once.
- Long-lived tokens: 15 minutes plus a refresh token (chapter 30), not 30 days.

**Interview question (task 4).** Walk through a JWT login and later requests. Where does the filter go and what does it put in the `SecurityContext`? Missing, expired, forged token: how does the filter react, and why not throw? The questions are repeated at the top of `interview-answer.md`.
