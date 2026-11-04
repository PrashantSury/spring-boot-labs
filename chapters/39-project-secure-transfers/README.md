# Chapter 39: Project: secure transfers

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Add login, roles and ownership checks so customers move only their own money.

## Planned topics

- JWT login on top of the transfers service
- Roles for customer, teller and auditor
- Method security for ownership
- Security tests for 401, 403 and success

## Planned traps

- Don't trust an account id coming from the client without an ownership check
- Test the negative cases as hard as the happy path

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter39CheckTest` that fails on the stub and tells you what it expects
