# Chapter 21: Cascading and transactions

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Closing a customer must delete or keep their accounts consistently, and a transfer must not half-succeed.

## Planned topics

- Cascade types and `orphanRemoval`
- `@Transactional` basics: commit, rollback, read-only
- Checked vs unchecked exceptions and rollback rules
- Where to put `@Transactional` (service, not controller)

## Planned traps

- Checked exceptions do not roll back by default
- Self-invocation skips the transaction proxy

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter21CheckTest` that fails on the stub and tells you what it expects
