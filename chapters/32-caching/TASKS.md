# Chapter 32 · Caching

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter32CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter32CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@EnableCaching`, `@Cacheable`, `@CacheEvict`, `@CachePut` | not written yet | — | checks not written yet | ⬜ |
| 2 | Cache keys and conditions | not written yet | — | checks not written yet | ⬜ |
| 3 | Cache providers: simple map vs Caffeine, with expiry | not written yet | — | checks not written yet | ⬜ |
| 4 | Verifying a cache hit in a test | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- How do you cache the branch list, keep it correct after a change, and prove the cache hit in a test?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Caching works through a proxy, so self-invocation skips it
- Caching mutable objects returns the same shared instance

## ❓ Checks (letters only)

1. Second call with the same arguments to a `@Cacheable` method: A. runs the method · B. returns the cached value
2. Self-invocation of a `@Cacheable` method: A. uses the cache · B. skips it
3. After a branch is added, the cache should be: A. left alone · B. evicted (or updated with `@CachePut`)

<details><summary>📖 Teaching</summary>

**Why it matters.** The branch list changes once a month but is read thousands of times per minute.

**The concept.** `@Cacheable` stores a method's result by its arguments and returns the stored copy next time, without running the method. `@CacheEvict` removes entries when the data changes.

**Small example.**

```java
@Cacheable("branches")
public List<Branch> all() { System.out.println("DB hit"); return repo.findAll(); }

@CacheEvict(value = "branches", allEntries = true)
public void add(Branch b) { repo.save(b); }
```

| Case | Result | In or out, and why |
|---|---|---|
| `all()` called twice | `DB hit` printed once | the second call is served from the cache |
| `add(...)`, then `all()` | `DB hit` again | the eviction cleared the entry |
| Another method in the same class calls `this.all()` | `DB hit` every time ❌ | caching works through a proxy |
| Caller mutates the returned list | every later caller sees the change ❌ | the cache returns the same shared instance |
| Caffeine with `expireAfterWrite=1h` | entries refresh at least hourly | the simple map cache never expires |

**Gotchas**
- Caching works through a proxy, so self-invocation skips it.
- Caching mutable objects returns the same shared instance.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** That is the point.
2. **B.** No proxy, no cache.
3. **B.** Otherwise readers get stale data.

</details>
