# Chapter 37: Events and scheduling

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

After a transfer commits, send a notification; every night, post interest. Neither should slow the request.

## Planned topics

- `ApplicationEventPublisher` and `@EventListener`
- `@TransactionalEventListener(AFTER_COMMIT)`
- `@Scheduled` with fixedRate and cron
- `@Async` and its thread pool

## Planned traps

- A plain listener runs inside the publisher's transaction
- `@Scheduled` on many instances runs on every instance

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter37CheckTest` that fails on the stub and tells you what it expects
