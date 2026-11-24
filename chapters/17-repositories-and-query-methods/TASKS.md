# Chapter 17 · Repositories and query methods

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter17CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter17CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `JpaRepository` and what you get for free | not written yet | — | checks not written yet | ⬜ |
| 2 | Derived query methods: `findByCustomerIdAndBalanceGreaterThan` | not written yet | — | checks not written yet | ⬜ |
| 3 | `@Query` with JPQL and named parameters | not written yet | — | checks not written yet | ⬜ |
| 4 | `@DataJpaTest` to test a repository | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- When is a derived query method enough, and when do you switch to `@Query`? What does `@DataJpaTest` load?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- A misspelled property in a method name fails at startup, not at compile time
- `Optional` return types: `findById` returns empty, not null

## ❓ Checks (letters only)

1. `findById` for a missing id returns: A. null · B. `Optional.empty()` · C. it throws
2. A typo in a derived method name is caught: A. by the compiler · B. at application startup
3. `GreaterThan` means: A. > · B. >=

<details><summary>📖 Teaching</summary>

**Why it matters.** Ops want "all accounts for customer X with balance over Y" without anyone writing SQL.

**The concept.** `JpaRepository` gives you CRUD for free, and Spring Data writes queries from method names like `findByCustomerIdAndBalanceGreaterThan`.

**Small example.**

```java
interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCustomerIdAndBalanceGreaterThan(Long customerId, BigDecimal min);

    @Query("select a from Account a where a.branch = :branch and a.balance >= :min")
    List<Account> rich(@Param("branch") String branch, @Param("min") BigDecimal min);
}
```

| Case | Result | In or out, and why |
|---|---|---|
| Customer 7 has 52,000 / 18,500 / 75,000; `min` = 50,000 | 2 accounts | derived query: `customer_id = ? and balance > ?` |
| `findById(999L)` | `Optional.empty()` | never `null` |
| Method named `findByCustmerId` | startup fails: no property `custmerId` | a typo fails at startup, not at compile time |
| `@DataJpaTest` on the repository | starts JPA + an embedded DB only | a fast slice test, no web layer |

**Gotchas**
- A misspelled property in a method name fails at startup, not at compile time.
- `findById` returns an empty `Optional`, not `null`.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Handle the empty case, e.g. 404.
2. **B.** Spring Data parses names when it builds the repository.
3. **A.** `GreaterThanEqual` is `>=`.

</details>
