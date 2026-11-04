# Chapter 22: The persistence context

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

A field changes and nobody called `save`, yet the database updated; this chapter explains why.

## Planned topics

- Managed, detached, new and removed entity states
- Dirty checking and flush
- First-level cache: same id, same object
- `save` vs `saveAndFlush` vs `merge` behaviour

## Planned traps

- Changing an entity inside a transaction updates it without `save`
- Detached entities from a closed transaction need `merge` or a reload

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter22CheckTest` that fails on the stub and tells you what it expects
