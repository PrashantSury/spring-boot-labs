# Chapter 31: Security errors

By the end of this chapter every "you can't do that" from Ledgerly looks the same as every other error: a `application/problem+json` body with a title, status, detail and path. That covers no login, a wrong password, a missing role on a URL and a missing role on a service method.

## Why you need this

Chapter 13 made controller errors neat. Then security arrived, and the mobile app suddenly got three different shapes: an empty 401, Boot's `{"timestamp",...,"error":"Forbidden"}` 403, and a 500 "Something went wrong" when a `@PreAuthorize` said no. The app shows "unknown error" for all three. The reason is that security errors happen in filters, before your `@ControllerAdvice` exists.

## What you will learn

- **`AuthenticationEntryPoint`** writes 401s; **`AccessDeniedHandler`** writes 403s.
- **Where each is plugged in:** `exceptionHandling(...)` *and* `httpBasic(...)`.
- **Filters vs `@ControllerAdvice`:** why the advice never sees a missing login.
- **The catch-all trap:** `@ExceptionHandler(Exception.class)` turning a method-security 403 into a 500.
- **MockMvc tests** for 401, 403 and 200 with `httpBasic(...)`.

## Real output

The finished app on port 8096 (asha = USER, ops = ADMIN, password = name + `-pass`):

```
$ curl -i localhost:8096/me
HTTP/1.1 401
WWW-Authenticate: Basic realm="ledgerly"
{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Login required","instance":"/me"}
$ curl -i -u asha:wrong localhost:8096/me
HTTP/1.1 401
WWW-Authenticate: Basic realm="ledgerly"
{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Login required","instance":"/me"}
$ curl -i -u asha:asha-pass localhost:8096/admin/audit
HTTP/1.1 403
{"type":"about:blank","title":"Forbidden","status":403,"detail":"You do not have permission for this action","instance":"/admin/audit"}
$ curl -i -u asha:asha-pass -X POST localhost:8096/accounts/1/freeze
HTTP/1.1 403
{"type":"about:blank","title":"Forbidden","status":403,"detail":"You do not have permission for this action","instance":"/accounts/1/freeze"}
$ curl -i -u asha:asha-pass localhost:8096/accounts/9
HTTP/1.1 404
{"detail":"no account 9","instance":"/accounts/9","status":404,"title":"Not Found"}
```

The last one comes from the `@ControllerAdvice`. The first four come from your two handlers.

## The shape of it

```java
http.httpBasic(b -> b.authenticationEntryPoint(entryPoint))
    .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(denied));

@ExceptionHandler(AccessDeniedException.class)   // in the advice, before the catch-all
void denied(AccessDeniedException e) { throw e; }
```

## Try it

```bash
mvn -pl chapters/31-security-errors spring-boot:run
mvn -pl chapters/31-security-errors test -Popen
```

On the stub a 401 has an empty body, a URL 403 has Boot's default body, and the freeze call is a 500.

## Your turn

You edit `ProblemEntryPoint.java`, `ProblemDeniedHandler.java`, `SecurityConfig.java`, `ApiErrors.java` and `interview-answer.md`.

1. **Entry point.** 401 problem `Login required` + `WWW-Authenticate: Basic realm="ledgerly"`.
2. **Denied handler.** 403 problem + a WARN log line with user and path.
3. **Wire them.** In `httpBasic(...)` and in `exceptionHandling(...)`.
4. **Advice fix.** Re-throw `AccessDeniedException` so it is a 403, not a 500.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- `@ExceptionHandler(AuthenticationException.class)` in the advice: never called for a missing login.
- Setting only `exceptionHandling`: a wrong Basic password still goes to the default entry point.
- Putting `ex.getMessage()` in the 401: "Bad credentials" vs "User not found" tells attackers which user names exist.
- An unauthenticated call to a missing resource is 401, not 404: security runs first.

**Interview question (task 5).** 401 vs 403 and which components produce each. Why doesn't `@ControllerAdvice` handle authentication failures, and how do you customise them? What goes wrong when a catch-all meets method security? The questions are repeated at the top of `interview-answer.md`.
