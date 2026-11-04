# Chapter 33: Transactions and locking

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Two transfers from the same account at the same moment must not both spend the same money.

## Planned topics

- Isolation levels and propagation, in plain words
- Optimistic locking with `@Version`
- Pessimistic locking with `@Lock`
- Retrying on `ObjectOptimisticLockingFailureException`

## Planned traps

- Read-then-write without a lock is a race
- `REQUIRES_NEW` commits even when the outer transaction rolls back

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter33CheckTest` that fails on the stub and tells you what it expects
