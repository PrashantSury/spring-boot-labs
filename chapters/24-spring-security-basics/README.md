# Chapter 24: Spring Security basics

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Adding the security starter locks every endpoint. Understand what it did before you change it.

## Planned topics

- The default login, the generated password and the filter chain
- `SecurityFilterChain` bean and `authorizeHttpRequests`
- Authentication vs authorization
- 401 vs 403 and which one means what

## Planned traps

- Order of `requestMatchers` matters; the first match wins
- CSRF protection blocks POSTs in a stateless API until you configure it

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter24CheckTest` that fails on the stub and tells you what it expects
