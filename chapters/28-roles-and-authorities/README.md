# Chapter 28: Roles and authorities

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

A teller can view accounts, a manager can approve transfers, an auditor can only read.

## Planned topics

- Roles vs authorities and the `ROLE_` prefix
- `hasRole`, `hasAuthority` in the filter chain
- Putting roles in the token and reading them back
- Role hierarchy

## Planned traps

- `hasRole("ADMIN")` looks for `ROLE_ADMIN`
- Path rules and method rules both apply; a request must pass both

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter28CheckTest` that fails on the stub and tells you what it expects
