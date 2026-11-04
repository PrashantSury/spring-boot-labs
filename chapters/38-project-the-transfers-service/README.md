# Chapter 38: Project: the transfers service

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Put it together: customers, accounts, a transfer with validation, errors and tests.

## Planned topics

- Design the endpoints and DTOs
- Transfer logic with a transaction and a lock
- Validation, error body and MockMvc tests
- A short README your reviewer could run

## Planned traps

- Think about double-submit: same transfer twice
- Rolling back a debit when the credit fails

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter38CheckTest` that fails on the stub and tells you what it expects
