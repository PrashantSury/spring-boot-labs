# Chapter 6: How auto-configuration works

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Interviewers ask "how does Spring Boot know to create a DataSource?" and you should answer from the mechanism, not from memory of a slide.

## Planned topics

- `@Conditional` annotations: `OnClass`, `OnMissingBean`, `OnProperty`
- Reading the `--debug` conditions report
- Writing your own tiny auto-configuration and starter-style bean
- How to back off a default by defining your own bean

## Planned traps

- Your own bean of the same type makes the default back off, which can surprise you
- Auto-configuration classes are not found by component scan

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter06CheckTest` that fails on the stub and tells you what it expects
