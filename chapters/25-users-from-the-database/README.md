# Chapter 25: Users from the database

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Bank staff are stored in a table, with hashed passwords, not in a config file.

## Planned topics

- `UserDetailsService` and `UserDetails`
- `PasswordEncoder` and BCrypt
- `AuthenticationManager` and `DaoAuthenticationProvider`
- Testing with `@WithMockUser`

## Planned traps

- Never store or log a plain password
- `BadCredentialsException` is deliberately vague about which part was wrong

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter25CheckTest` that fails on the stub and tells you what it expects
