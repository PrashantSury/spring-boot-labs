# Chapter 32: Caching

By the end of this chapter Ledgerly asks the paid exchange-rate API for the dollar rate once, not on every transfer screen, builds last month's statement once, and still never shows a stale rate after the treasury desk changes it or the day ends.

## Why you need this

Every rupee-to-dollar conversion calls an outside API that takes 200 ms and costs money per call. The app shows the same USD rate to thousands of customers a minute. Statements for closed months never change, yet each download rebuilds them. A cache answers repeated questions from memory. The hard part is knowing when the remembered answer is wrong.

## What you will learn

- **`@EnableCaching`, `@Cacheable`, `@CachePut`, `@CacheEvict`.**
- **Keys and conditions:** `key = "#accountId + ':' + #month"`, `condition = "..."`.
- **Providers:** Boot's simple map vs **Caffeine** with size limit and expiry (`spring.cache.caffeine.spec`).
- **Proving a hit in a test:** count calls to the slow dependency.
- **The proxy trap** and why public fields read `null` through a proxy.

## Real output

`mvn -q -pl chapters/32-caching spring-boot:run` (finished code):

```
rate(USD) = 83.12  in 215 ms  upstream calls so far: 1
rate(USD) = 83.12  in   0 ms  upstream calls so far: 1
rate(EUR) = 90.45  in 200 ms  upstream calls so far: 2
rate(USD) = 83.12  in   0 ms  upstream calls so far: 2
after endOfDay: rate(USD) in 200 ms  upstream calls: 3
```

On the stub every line takes 200 ms and the count goes 1, 2, 3, 4, 5.

## The shape of it

```java
@Cacheable("fxRates")                                   public BigDecimal rate(String currency)
@CachePut(cacheNames = "fxRates", key = "#currency")    public BigDecimal override(String currency, BigDecimal rate)
@CacheEvict(cacheNames = "fxRates", allEntries = true)  public void endOfDay()
```
```properties
spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=10m
```

## Try it

```bash
mvn -q -pl chapters/32-caching spring-boot:run
mvn -pl chapters/32-caching test -Popen
```

## Your turn

You edit `LedgerlyApplication.java` (one annotation), `FxRateService.java`, `StatementService.java` (annotations only), `application.properties` and `interview-answer.md`.

1. **Cache rates.** `@EnableCaching` + `@Cacheable("fxRates")`.
2. **Statements.** Key without the `format` flag; cache only closed months.
3. **Put and evict.** `override` updates the cache; `endOfDay` empties it.
4. **Caffeine.** Max 500 entries, expire 10 minutes after write.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- No `@EnableCaching`: the annotations do nothing, silently.
- Self-invocation: `usdTwiceInternally()` calls `this.rate(...)` and skips the cache (the example test proves it).
- Caching a mutable object (a `List` you later `add` to) changes the cached copy for everyone.
- `@Cacheable` and `@CachePut` on the same method: the put always runs the method, so the cache never helps.
- A local cache on three app instances means three different "current" rates until expiry; a shared cache (Redis) fixes that.

**Interview question (task 5).** How does the cache abstraction work; what do the three annotations do? How do you choose keys and conditions? How do you avoid stale data, and what are the traps? The questions are repeated at the top of `interview-answer.md`.
