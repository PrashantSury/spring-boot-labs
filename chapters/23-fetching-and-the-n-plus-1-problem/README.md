# Chapter 23: Fetching and the N+1 problem

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

The accounts page takes 6 seconds because it runs 201 queries for 200 customers.

## Planned topics

- Lazy vs eager fetching and `LazyInitializationException`
- Spotting N+1 in the SQL log
- Fixes: `JOIN FETCH`, `@EntityGraph`, batch size
- Open session in view and why many teams turn it off

## Planned traps

- `EAGER` on a collection is almost always a mistake
- `JOIN FETCH` with pagination loads everything in memory

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter23CheckTest` that fails on the stub and tells you what it expects
