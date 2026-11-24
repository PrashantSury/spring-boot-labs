# Chapter 34: AOP

By the end of this chapter every Ledgerly transfer writes an audit line and every `TransferService` method is timed, and `TransferService` itself has no logging or timing code in it at all.

## Why you need this

The auditors want a line for every transfer, good or failed. Operations want to know which service methods are slow. Adding `trail.add(...)` and `System.nanoTime()` to every method means dozens of copy-pasted lines that someone will forget in the next method. Aspect-oriented programming (AOP) lets you write that logic once and say *where* it applies.

## What you will learn

- **Aspect, pointcut, advice:** the class, the "where", and the "what".
- **`@Around`** with `ProceedingJoinPoint.proceed()` for timing; **`@AfterReturning`** and **`@AfterThrowing`** for auditing.
- **`@annotation(...)` pointcuts:** apply advice only to methods marked `@Audited`.
- **Proxies:** Spring AOP wraps the bean in a CGLIB subclass (JDK proxies only for interfaces).
- **The two proxy traps:** self-invocation and `final` methods.
- Why `@Transactional`, `@Cacheable`, `@Async` and `@PreAuthorize` behave the same way: they are all AOP.

## Real output

`mvn -q -pl chapters/34-aop spring-boot:run` (finished code):

```
bean class : TransferService$$SpringCGLIB$$0
CGLIB proxy: true
caller saw : insufficient funds in ACC-1
audit trail:
  OK transfer [ACC-1, ACC-2, 100] -> TX-1
  FAILED transfer [ACC-1, ACC-2, 5000]: insufficient funds in ACC-1
  OK transfer [ACC-1, ACC-2, 10] -> TX-2
  OK transfer [ACC-1, ACC-3, 20] -> TX-3
timings    : {payroll=1, transfer=4}
```

The tour runs one transfer, one failing transfer, a payroll of two lines and a `feeFor` call. `feeFor` never shows up in the timings because it is `final`.

On the stub:

```
bean class : TransferService
CGLIB proxy: false
caller saw : insufficient funds in ACC-1
audit trail:
timings    : {}
```

## The shape of it

```java
@Aspect @Component
class TimingAspect {
  @Around("execution(* ledgerly.TransferService.*(..))")
  Object time(ProceedingJoinPoint pjp) throws Throwable {
    long start = System.nanoTime();
    try { return pjp.proceed(); }
    finally { timings.record(pjp.getSignature().getName(), System.nanoTime() - start); }
  }
}

@AfterReturning(pointcut = "@annotation(audited)", returning = "result")
void succeeded(JoinPoint jp, Audited audited, Object result) { ... }
```

## Try it

```bash
mvn -q -pl chapters/34-aop spring-boot:run
mvn -pl chapters/34-aop test -Popen
```

## Your turn

You edit `TimingAspect.java`, `AuditAspect.java`, `TransferService.java` (task 4 only) and `interview-answer.md`.

1. **Timing aspect.** `@Aspect` + `@Component`, `@Around` every `TransferService` method, record in a `finally`.
2. **Audit success.** `@AfterReturning` on `@annotation(audited)`: `OK transfer [ACC-1, ACC-2, 100] -> TX-1`.
3. **Audit failure.** `@AfterThrowing`: `FAILED transfer [...]: message`; the exception still reaches the caller.
4. **Self-invocation.** `payroll` calls `this.transfer(...)`, so its lines are never audited. Call through the proxy.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- `@Aspect` alone is not a bean: without `@Component` nothing happens.
- Forgetting `return pjp.proceed()` in `@Around` makes every method return `null`; catching the exception there hides failures.
- `this.method()` inside the bean skips every aspect (and every `@Transactional`).
- `final` and `private` methods cannot be intercepted; on a CGLIB proxy a final method even runs on the empty proxy object.

**Interview question (task 5).** Aspects, pointcuts and advice, and when to use `@Around`. How Spring applies aspects at runtime; JDK vs CGLIB proxies. Why self-invocation and final methods skip the aspect, and which other features share the limit. The questions are repeated at the top of `interview-answer.md`.
