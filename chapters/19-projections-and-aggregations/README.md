# Chapter 19: Projections and aggregations

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

The dashboard needs total balance per branch, not every account.

## Planned topics

- Interface and record projections
- `@Query` with `GROUP BY`, `SUM`, `COUNT`
- Constructor expressions in JPQL
- Native queries and when to use them

## Planned traps

- Selecting the whole entity to read two fields wastes memory
- `SUM` over no rows returns null, not 0

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter19CheckTest` that fails on the stub and tells you what it expects
