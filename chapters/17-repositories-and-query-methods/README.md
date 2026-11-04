# Chapter 17: Repositories and query methods

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Ops want "all accounts for customer X with balance over Y" without anyone writing SQL.

## Planned topics

- `JpaRepository` and what you get for free
- Derived query methods: `findByCustomerIdAndBalanceGreaterThan`
- `@Query` with JPQL and named parameters
- `@DataJpaTest` to test a repository

## Planned traps

- A misspelled property in a method name fails at startup, not at compile time
- `Optional` return types: `findById` returns empty, not null

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter17CheckTest` that fails on the stub and tells you what it expects
