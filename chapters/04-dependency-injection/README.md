# Chapter 4: Dependency injection

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

A transfer service that builds its own fee calculator with `new` cannot be tested or changed; let Spring hand its collaborators in.

## Planned topics

- Constructor injection vs field injection, and why constructors win
- Injecting an interface with two implementations: `@Qualifier` and `@Primary`
- Injecting lists, maps and optional beans
- Circular dependencies and how to break them

## Planned traps

- Field injection lets a class collect ten dependencies unnoticed, and it cannot be built in a plain unit test
- Two beans of one type and no `@Primary` gives `NoUniqueBeanDefinitionException`

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter04CheckTest` that fails on the stub and tells you what it expects
