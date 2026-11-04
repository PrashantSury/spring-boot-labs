# Chapter 36: Actuator

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Operations need to ask the running app if it is healthy, what version it is and how it is doing.

## Planned topics

- Health, info and metrics endpoints
- Exposing endpoints safely
- Custom `HealthIndicator`
- Micrometer counters for transfers

## Planned traps

- Don't expose every endpoint to the public internet
- Health 'UP' only means the checks you configured passed

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter36CheckTest` that fails on the stub and tells you what it expects
