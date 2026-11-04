# Chapter 8: How Spring handles requests

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

A request for `/accounts/42` passes through many steps before your method runs; knowing them is how you debug a surprise 404 or 500.

## Planned topics

- The `DispatcherServlet` flow: handler mapping, adapter, argument resolvers, converters
- How a handler method is chosen when several patterns match
- Built-in answers: 400, 404, 405, 406, 415, 500, and which exception makes each
- `@RequestMapping` options: `consumes`, `produces`, `params`, `headers`

## Planned traps

- A more specific mapping beats a pattern: `/accounts/above` vs `/accounts/{number}`
- 405 means the path exists; 404 means it does not

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter08CheckTest` that fails on the stub and tells you what it expects
