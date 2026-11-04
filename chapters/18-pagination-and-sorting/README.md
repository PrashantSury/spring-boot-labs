# Chapter 18: Pagination and sorting

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

A customer with 20,000 transactions cannot be returned in one response.

## Planned topics

- `Pageable`, `Page`, `Slice`
- `PageRequest.of` and `Sort`
- Exposing `page`, `size` and `sort` in a controller
- Counting queries and when a `Slice` is cheaper

## Planned traps

- Sorting by a user-supplied property name must be checked against a whitelist
- Page numbers start at 0

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter18CheckTest` that fails on the stub and tells you what it expects
