# Chapter 32 · Caching

**Time:** ~45 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter32CheckTest` 13/13 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/32-caching test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/32-caching spring-boot:run` (prints timings and upstream call counts).
- Fresh stub: 1/13 green (the self-invocation example; must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@EnableCaching` + `@Cacheable("fxRates")` | `src/main/java/ledgerly/LedgerlyApplication.java`, `FxRateService.java` | `mvn -pl chapters/32-caching test -Popen` | task 1 checks green | ⬜ |
| 2 | statement key + condition | `StatementService.java` | same | task 2 checks green | ⬜ |
| 3 | `@CachePut` / `@CacheEvict` | `FxRateService.java` | same | task 3 checks green | ⬜ |
| 4 | Caffeine spec | `src/main/resources/application.properties` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** abstraction, keys, staleness | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- How does it work; the three annotations?
- Keys and conditions?
- Staleness and traps?

## ⚠️ Traps

- `@EnableCaching` or nothing happens.
- Self-invocation skips the cache.
- Don't cache what still changes (this month's statement).

## ❓ Checks (letters only)

1. `rate("USD")` is called 3 times with `@Cacheable`. Upstream calls: A. 3 · B. 1
2. Which annotation ALWAYS runs the method and stores its result? A. `@Cacheable` · B. `@CachePut`
3. `statement(1, lastMonth, "pdf")` then `("csv")` with the default key: A. one build · B. two builds

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "How would you speed up this endpoint?" almost always leads to caching, then straight to "and how do you invalidate it?", the famously hard part.

**The concept.** A cache is the sticky note on the teller's desk with today's dollar rate. `@Cacheable` reads the note first and only calls head office when there is none. `@CachePut` writes a new note (the treasury changed the rate). `@CacheEvict` throws notes away (end of day). Caffeine's `expireAfterWrite` makes notes fade by themselves after 10 minutes, so even a forgotten eviction cannot serve yesterday's rate.

**Small example.** Real counts from the tests (upstream = paid API calls):

| Calls | Upstream calls | Why |
|---|---|---|
| `rate(USD)` ×3 | 1 | hit after the first |
| `rate(USD)`, `rate(EUR)`, `rate(USD)` | 2 | key = currency |
| `rate(USD)`, `override(USD, 84.00)`, `rate(USD)` | 1, returns 84.00 | put replaced the note |
| `rate(USD)`, `rate(EUR)`, `endOfDay()`, both again | 4 | allEntries evict |
| statement(1, last month, pdf / csv / pdf) | 1 build | key ignores format |
| statement(1, this month) ×2 | 2 builds | condition: not cached |
| `usdTwiceInternally()` | 2 | `this.rate` skips the proxy |

**Gotchas**
- The default key uses all parameters; with none it is `SimpleKey.EMPTY` (one entry for everything).
- `condition` is checked before the call; `unless` after (it can use `#result`).
- Without Caffeine on the classpath Boot uses a `ConcurrentHashMap`: no expiry, no size limit, a slow memory leak.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** Hits come from memory.
2. **B.** `@Cacheable` skips the method on a hit.
3. **B.** The default key includes `format`, so pdf and csv are different entries.

</details>
