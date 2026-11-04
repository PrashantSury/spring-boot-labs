# Chapter 31: Security errors

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Clients must get a clean JSON 401 or 403, not an HTML login page or an empty body.

## Planned topics

- `AuthenticationEntryPoint` for 401
- `AccessDeniedHandler` for 403
- Where security exceptions are handled vs `@ControllerAdvice`
- MockMvc tests for 401, 403 and 200

## Planned traps

- `@ExceptionHandler` does not catch security-filter exceptions
- A missing token is 401; a valid token without permission is 403

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter31CheckTest` that fails on the stub and tells you what it expects
