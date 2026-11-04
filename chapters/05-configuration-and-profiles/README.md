# Chapter 5: Configuration and profiles

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

The same Ledgerly jar must run with a test database on a laptop and the real one in production, with no code change.

## Planned topics

- `application.properties` and `application.yml`, and the order Spring reads them
- `@Value` vs `@ConfigurationProperties` with a validated properties record
- Spring profiles: `dev`, `uat`, `prod`, and `@Profile` on beans
- Property precedence: environment variables, command line, files

## Planned traps

- A typo in a property name is silently ignored with `@Value` defaults
- An active profile replaces nothing by itself; it only adds files and beans

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter05CheckTest` that fails on the stub and tells you what it expects
