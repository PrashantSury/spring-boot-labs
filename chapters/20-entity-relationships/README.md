# Chapter 20: Entity relationships

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

A customer has many accounts and each account has many transactions; the model must say so.

## Planned topics

- `@ManyToOne`, `@OneToMany`, `mappedBy` and the owning side
- `@OneToOne` and `@ManyToMany` and when to avoid them
- Bidirectional helper methods
- JSON loops when an entity points back at itself

## Planned traps

- Without `mappedBy`, JPA creates an extra join table
- Serialising a bidirectional entity gives infinite recursion

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter20CheckTest` that fails on the stub and tells you what it expects
