# Chapter 7 · The bean lifecycle

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter07CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter07CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Creation, injection, `@PostConstruct`, ready, `@PreDestroy`, destroy | not written yet | — | checks not written yet | ⬜ |
| 2 | `InitializingBean`, `DisposableBean` and `@Bean(initMethod, destroyMethod)` | not written yet | — | checks not written yet | ⬜ |
| 3 | `BeanPostProcessor` and what Spring does to your beans (proxies) | not written yet | — | checks not written yet | ⬜ |
| 4 | Prototype beans: Spring creates them but does not destroy them | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Walk through a singleton bean's lifecycle. Where are proxies created, and why does a self-call skip `@Transactional`?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- `@PostConstruct` runs after injection, not in the constructor
- Calling a `@Transactional` or proxied method from inside the same class bypasses the proxy

## ❓ Checks (letters only)

1. Where is a field-injected `@Autowired` field first non-null? A. in the constructor · B. in `@PostConstruct`
2. A prototype bean's `@PreDestroy`: A. runs at shutdown · B. is never called by Spring
3. Proxies (for `@Transactional`, `@Async`) are created by: A. the constructor · B. a `BeanPostProcessor` after initialisation · C. the JVM

<details><summary>📖 Teaching</summary>

**Why it matters.** A connection-warmer bean must start after its dependencies are ready and release resources on shutdown.

**The concept.** A bean is constructed, then injected, then initialised (`@PostConstruct`), then used; on shutdown `@PreDestroy` runs. `BeanPostProcessor`s run around initialisation, and that is where proxies are made.

**Small example.**

```java
@Component
class RateCacheWarmer {
    private final RateClient rates;
    private Map<String, BigDecimal> cache = Map.of();

    RateCacheWarmer(RateClient rates) { this.rates = rates; System.out.println("1 constructor"); }

    @PostConstruct void warm() { cache = rates.loadAll(); System.out.println("2 warmed " + cache.size() + " rates"); }

    @PreDestroy void close() { System.out.println("3 closing"); }
}
```

| Case | Result | In or out, and why |
|---|---|---|
| App starts (`RateClient` returns 3 rates) | `1 constructor` then `2 warmed 3 rates` | init runs after the constructor and injection |
| App stops (Ctrl+C / context close) | `3 closing` | singletons get their destroy callback |
| Same class with `@Scope("prototype")`, then shutdown | no `3 closing` | Spring creates prototypes but does not destroy them |
| A field-injected `@Autowired` field read inside the constructor | `null` | field injection happens after construction |

**Gotchas**
- `@PostConstruct` runs after injection, not in the constructor.
- Calling a `@Transactional` (proxied) method from inside the same class bypasses the proxy.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Fields are set after the object exists.
2. **B.** The caller owns prototype instances.
3. **B.** The post-processor wraps the bean and returns the proxy.

</details>
