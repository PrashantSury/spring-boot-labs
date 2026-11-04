# Chapter 9: Requests and responses

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

The mobile app will POST a new customer as JSON and expect a proper status and a `Location` header back.

## Planned topics

- `@RequestBody`, `@PathVariable`, `@RequestParam`, `@RequestHeader`
- `ResponseEntity`, status codes 200/201/204 and the `Location` header
- Content negotiation: `Accept` and `Content-Type`, and 415/406 answers
- Jackson basics: field names, dates, ignoring unknown fields

## Planned traps

- A POST with the wrong `Content-Type` answers 415 before your method runs
- A body that is not valid JSON answers 400 with a parse message, not 500

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter09CheckTest` that fails on the stub and tells you what it expects
