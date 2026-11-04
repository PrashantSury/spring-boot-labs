# Chapter 14: Filters and interceptors

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Every request needs a correlation id in the logs, and some paths need a quick check before the controller.

## Planned topics

- Servlet `Filter` vs `HandlerInterceptor`: where each runs
- `OncePerRequestFilter` and registering filter order
- Logging request id with MDC
- What an interceptor can and cannot see (it does not see 404s for unmapped paths)

## Planned traps

- Exceptions thrown in a filter do not reach `@ControllerAdvice`
- Filter order decides who runs first, and security filters have their own chain

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter14CheckTest` that fails on the stub and tells you what it expects
