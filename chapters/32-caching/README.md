# Chapter 32: Caching

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

The branch list changes once a month but is read thousands of times per minute.

## Planned topics

- `@EnableCaching`, `@Cacheable`, `@CacheEvict`, `@CachePut`
- Cache keys and conditions
- Cache providers: simple map vs Caffeine, with expiry
- Verifying a cache hit in a test

## Planned traps

- Caching works through a proxy, so self-invocation skips it
- Caching mutable objects returns the same shared instance

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter32CheckTest` that fails on the stub and tells you what it expects
