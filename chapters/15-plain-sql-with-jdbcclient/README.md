# Chapter 15: Plain SQL with JdbcClient

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Before JPA hides the SQL, you should see the SQL: select, insert and update accounts yourself.

## Planned topics

- `DataSource`, H2 and `schema.sql` / `data.sql`
- `JdbcClient`: query to records, named parameters
- Insert with generated keys, update counts
- Testing with an in-memory database

## Planned traps

- Concatenating strings into SQL is an injection bug; use parameters
- Forgetting to check the update count hides a failed update

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter15CheckTest` that fails on the stub and tells you what it expects
