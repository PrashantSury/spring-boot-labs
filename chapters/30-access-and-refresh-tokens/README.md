# Chapter 30: Access and refresh tokens

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

A 15-minute access token keeps risk low; a refresh token keeps customers from logging in all day.

## Planned topics

- Short access tokens and longer refresh tokens
- Refresh endpoint and rotating tokens
- Revoking: a stored refresh token table
- Logout and what it really means for a stateless token

## Planned traps

- A refresh token that never expires is a permanent password
- A stolen access token is valid until it expires; plan for that

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter30CheckTest` that fails on the stub and tells you what it expects
