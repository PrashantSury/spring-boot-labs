# Chapter 12: Validating input

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

A transfer of -500 rupees or to a blank account number must be refused with a clear message, before any money moves.

## Planned topics

- Bean Validation: `@NotBlank`, `@Positive`, `@Size`, `@Pattern`, `@Email`
- `@Valid` on `@RequestBody` and the 400 it produces
- Validating `@RequestParam` and `@PathVariable` with `@Validated`
- Custom constraint annotation for an account number

## Planned traps

- `@Valid` on a nested object does not cascade unless the field is also `@Valid`
- Validation on a path variable throws a different exception from a body

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter12CheckTest` that fails on the stub and tells you what it expects
