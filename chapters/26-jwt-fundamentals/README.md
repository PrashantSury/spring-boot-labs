# Chapter 26: JWT fundamentals

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

Mobile clients need to prove who they are on every call without a server session.

## Planned topics

- Header, payload and signature of a JWT
- Signing and verifying with an HMAC secret
- Claims, `exp`, `iat`, `sub`
- What a JWT is not: it is signed, not encrypted

## Planned traps

- Anyone can read the payload; never put secrets in it
- Always check `exp`, and reject the `none` algorithm

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter26CheckTest` that fails on the stub and tells you what it expects
