# Chapter 13: Handling errors

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Clients should get the same error shape for every failure, never a stack trace.

## Planned topics

- `@RestControllerAdvice` and `@ExceptionHandler`
- Problem details (`ProblemDetail`) as a standard error body
- Handler ordering: most specific exception wins, and `@Order` on advices
- Mapping domain exceptions such as `InsufficientFundsException` to 409/422

## Planned traps

- A handler for `Exception` will swallow Spring's built-in 400/404/405 unless you extend the base class
- Two advices that handle the same exception: the order decides

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter13CheckTest` that fails on the stub and tells you what it expects
