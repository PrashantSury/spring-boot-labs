# Chapter 35: Calling other services with RestClient

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Ledgerly checks a payee against an external sanctions service before a transfer.

## Planned topics

- `RestClient` basics: get, post, retrieve
- Timeouts, error statuses and `onStatus`
- Testing with `MockRestServiceServer`
- A simple retry and what not to retry

## Planned traps

- No timeout means a slow partner hangs your threads
- Retrying a POST can double a payment; use an idempotency key

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter35CheckTest` that fails on the stub and tells you what it expects
