# Chapter 7 · The bean lifecycle

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter07CheckTest` 18/18 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/07-the-bean-lifecycle test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/07-the-bean-lifecycle compile exec:java "-Dexec.mainClass=ledgerly.LifecycleTour"`.
- Fresh stub: 2/18 green (guards that must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@PostConstruct` on `load()` | `src/main/java/ledgerly/RateCache.java` | `mvn -pl chapters/07-the-bean-lifecycle test -Popen` | task 1 checks green | ⬜ |
| 2 | `@PreDestroy` on `closeAll()` | `SessionRegistry.java` | same | task 2 checks green | ⬜ |
| 3 | `@Bean(initMethod = "connect", destroyMethod = "disconnect")` | `GatewayConfig.java` | same | task 3 checks green | ⬜ |
| 4 | `BeanPostProcessor` → counting proxy for `@Metered` beans | `MeteringPostProcessor.java` | same | task 4 checks green | ⬜ |
| 5 | All six hooks on `LifecycleProbe` + its `@Bean` names | `LifecycleProbe.java`, `GatewayConfig.java` | same | task 5 checks green | ⬜ |
| 6 | **(Interview)** the lifecycle, proxies, prototypes | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 6)

- A singleton's life from creation to destruction. Why `@PostConstruct` and not the constructor?
- What does a `BeanPostProcessor` do, and why does `this.method()` skip the proxy?
- Does Spring destroy prototype beans?

## ⚠️ Traps

- Setter-injected fields are `null` in the constructor.
- Return `bean` unchanged for beans that are not `@Metered`.
- `line=0` after `statement()` is the right answer (self-invocation).

## ❓ Checks (letters only)

1. Which runs first? A. `afterPropertiesSet` · B. `@PostConstruct` · C. `initMethod`
2. A prototype bean has a `@PreDestroy` method. On `context.close()` Spring: A. calls it once per instance · B. does not call it · C. calls it once
3. `@Transactional save()` is called from `saveAll()` in the **same** class. The transaction for `save()`: A. starts · B. does not start, the call skips the proxy

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "Explain the bean lifecycle" and "why didn't my `@Transactional` work?" are both lifecycle questions. The second one is a real production bug class: a transaction or cache silently not applied.

**The concept.** Spring builds a bean, injects it, runs the init hooks, then lets every `BeanPostProcessor` replace it (often with a proxy). On close it runs the destroy hooks in a fixed order, for singletons only.

**Small example.** Sample bean: `StatementPrinter` (`@Metered`) with `statement()` calling `this.line()` twice.

```java
printer.statement("ACC-1");      // through the proxy
printer.line("ACC-1", 100);      // through the proxy
```

Real output:

```
after statement(): statement=1 line=0
after line():      statement=1 line=1
```

| Call | Counted? | Why |
|---|---|---|
| `printer.statement(...)` from outside | yes | the caller holds the proxy |
| `this.line(...)` inside `statement` | no | `this` is the real object, not the proxy |
| `printer.line(...)` from outside | yes | through the proxy again |

**Gotchas**
- A `BeanPostProcessor` is created very early; inject other beans into it lazily (`ObjectProvider`), or they miss their own post-processing.
- `@Bean` infers a destroy method named `close` or `shutdown` automatically; `disconnect` must be named.
- Shutdown hooks only run if the context is closed: `ctx.close()`, or the JVM shutdown hook Boot registers for you.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** `@PostConstruct`, then `afterPropertiesSet`, then `initMethod`.
2. **B.** Spring hands prototypes out and forgets them.
3. **B.** Self-invocation goes to `this`, not the proxy; move `save()` to another bean or call through the proxy.

</details>
