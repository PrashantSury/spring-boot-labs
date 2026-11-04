# Chapter 29: Method security

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

A customer must see only their own accounts, which a URL rule cannot express.

## Planned topics

- `@EnableMethodSecurity`, `@PreAuthorize`, `@PostAuthorize`
- SpEL: `#id == authentication.principal.id`
- Custom permission checks as a bean
- Testing method security with MockMvc and `@WithMockUser`

## Planned traps

- Method security works through a proxy, so self-invocation skips it
- `@PreAuthorize` on a controller and on a service can both apply

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter29CheckTest` that fails on the stub and tells you what it expects
