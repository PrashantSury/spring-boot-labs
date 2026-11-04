# Chapter 16: JPA entities

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Writing the mapping from rows to `Account` objects by hand repeats everywhere; JPA does it from annotations.

## Planned topics

- `@Entity`, `@Id`, `@GeneratedValue`, `@Column`
- Why JPA needs a no-arg constructor, and records vs classes
- `ddl-auto` options and Flyway as the real answer
- `BigDecimal` for money and why never `double`

## Planned traps

- `equals`/`hashCode` on entities with generated ids
- Using an enum without `@Enumerated(STRING)` stores its ordinal

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter16CheckTest` that fails on the stub and tells you what it expects
