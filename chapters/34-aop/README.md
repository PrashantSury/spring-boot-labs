# Chapter 34: AOP

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Audit logging for every service method should not mean editing every service method.

## Planned topics

- Aspects, pointcuts and advice: `@Around`, `@Before`, `@AfterReturning`
- Writing a timing aspect
- How Spring AOP uses proxies (JDK vs CGLIB)
- Why `@Transactional`, `@Cacheable` and `@Async` are all AOP

## Planned traps

- Self-invocation skips the aspect
- Final classes and private methods cannot be proxied

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter34CheckTest` that fails on the stub and tells you what it expects
