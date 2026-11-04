# Chapter 27: JWT login and the JWT filter

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

A customer posts a username and password once, then sends a token with each request.

## Planned topics

- `/auth/login` returning a token
- A `OncePerRequestFilter` that reads `Authorization: Bearer`
- Setting the `SecurityContext`
- Stateless session policy

## Planned traps

- Place the JWT filter before `UsernamePasswordAuthenticationFilter`
- An exception thrown inside the filter skips `@ControllerAdvice`

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter27CheckTest` that fails on the stub and tells you what it expects
