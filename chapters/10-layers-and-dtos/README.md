# Chapter 10: Layers and DTOs

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Returning the `Account` entity straight from the controller leaks internal fields and ties the API to the database.

## Planned topics

- Controller, service, repository: one job each
- Request and response DTOs as records
- Mapping entities to DTOs by hand, and when a mapper library is worth it
- Keeping business rules out of controllers

## Planned traps

- Putting logic in the controller makes it hard to test without MockMvc
- Never expose a field like `pin` or `internalNotes` by returning the entity

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter10CheckTest` that fails on the stub and tells you what it expects
