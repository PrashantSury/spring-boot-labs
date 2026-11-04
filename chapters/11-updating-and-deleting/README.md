# Chapter 11: Updating and deleting

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

A customer changes their phone number and closes an account: PUT, PATCH and DELETE must behave exactly as clients expect.

## Planned topics

- PUT vs PATCH, and idempotency
- DELETE answers: 204 vs 404 when it is already gone
- Optimistic checks: unknown id gives 404
- MockMvc tests for create, read, update, delete

## Planned traps

- PUT replaces the whole resource; missing fields become null
- Calling DELETE twice should give the same result

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter11CheckTest` that fails on the stub and tells you what it expects
