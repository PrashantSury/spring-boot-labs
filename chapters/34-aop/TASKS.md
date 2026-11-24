# Chapter 34 · AOP

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter34CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter34CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Aspects, pointcuts and advice: `@Around`, `@Before`, `@AfterReturning` | not written yet | — | checks not written yet | ⬜ |
| 2 | Writing a timing aspect | not written yet | — | checks not written yet | ⬜ |
| 3 | How Spring AOP uses proxies (JDK vs CGLIB) | not written yet | — | checks not written yet | ⬜ |
| 4 | Why `@Transactional`, `@Cacheable` and `@Async` are all AOP | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- How does Spring AOP work underneath (JDK vs CGLIB proxies), and why do `@Transactional`, `@Cacheable` and `@Async` share the same limits?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Self-invocation skips the aspect
- Final classes and private methods cannot be proxied

## ❓ Checks (letters only)

1. `@Around` advice must call: A. `pjp.proceed()`, or the method never runs · B. nothing
2. A bean with no interface is proxied with: A. a JDK dynamic proxy · B. CGLIB (a subclass)
3. Which can't be advised by Spring AOP? A. public service methods · B. private methods

<details><summary>📖 Teaching</summary>

**Why it matters.** Audit logging for every service method should not mean editing every service method.

**The concept.** An aspect is code that runs around chosen methods (the pointcut) without editing them. Spring applies it by wrapping your bean in a proxy.

**Small example.**

```java
@Aspect @Component
class TimingAspect {
    @Around("execution(public * com.ledgerly..service.*.*(..))")
    Object time(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        try { return pjp.proceed(); }
        finally { System.out.println(pjp.getSignature().toShortString() + " took " + (System.nanoTime() - start) / 1_000_000 + " ms"); }
    }
}
```

| Case | Result | In or out, and why |
|---|---|---|
| Controller calls `transferService.transfer(...)` | `TransferService.transfer(..) took 12 ms` | the call goes through the proxy |
| `transfer` calls `this.validate(...)` (also public, same class) | no line for `validate` ❌ | self-invocation skips the aspect |
| A `private` or `final` method | never advised | a proxy can't override it |
| `@Transactional`, `@Cacheable`, `@Async` | work the same way | they are all proxy-based AOP |

**Gotchas**
- Self-invocation skips the aspect.
- Final classes and private methods cannot be proxied.

</details>

<details><summary>🔒 Answers</summary>

1. **A.** You decide whether and when the real method runs.
2. **B.** JDK proxies need an interface; Spring Boot uses CGLIB by default.
3. **B.** A proxy only intercepts calls it can override.

</details>
