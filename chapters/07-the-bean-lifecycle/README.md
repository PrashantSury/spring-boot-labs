# Chapter 7: The bean lifecycle

> **Status: NOT_STARTED.** This chapter is planned. The story, code, tasks and checks are not written yet.

## Why you need this

A connection-warmer bean must start after its dependencies are ready and release resources on shutdown.

## Planned topics

- Creation, injection, `@PostConstruct`, ready, `@PreDestroy`, destroy
- `InitializingBean`, `DisposableBean` and `@Bean(initMethod, destroyMethod)`
- `BeanPostProcessor` and what Spring does to your beans (proxies)
- Prototype beans: Spring creates them but does not destroy them

## Planned traps

- `@PostConstruct` runs after injection, not in the constructor
- Calling a `@Transactional` or proxied method from inside the same class bypasses the proxy

## What the finished chapter will have

- A short story from Ledgerly Bank, and what you will learn
- Small code with real output
- Numbered tasks with `// Task N` comments in stub files, one marked **(Interview)**
- A `Chapter07CheckTest` that fails on the stub and tells you what it expects
