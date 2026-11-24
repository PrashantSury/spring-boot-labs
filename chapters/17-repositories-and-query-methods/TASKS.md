# Chapter 17 · Repositories and query methods

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter17CheckTest` 15/15 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/17-repositories-and-query-methods test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/17-repositories-and-query-methods spring-boot:run` (H2 in memory, no Docker).
- Fresh stub: 1/15 green (`count()` comes free from `JpaRepository`).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `findByNumber` → `Optional` | `src/main/java/ledgerly/AccountRepository.java` | `mvn -pl chapters/17-repositories-and-query-methods test -Popen` | task 1 checks green | ⬜ |
| 2 | `findByCustomerIdAndBalanceGreaterThanOrderByBalanceDesc` | `AccountRepository.java` | same | task 2 checks green | ⬜ |
| 3 | `countByStatus`, `existsByNumber` | `AccountRepository.java` | same | task 3 checks green | ⬜ |
| 4 | `@Query` owner search; `@Transactional @Modifying(clearAutomatically = true)` freeze | `AccountRepository.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** derived vs `@Query`, `@Modifying` | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- What does `JpaRepository` give you? How does a derived method work?
- When `@Query`? JPQL vs SQL?
- `@Modifying` and `clearAutomatically`?

## ⚠️ Traps

- Typos in derived names fail at startup.
- JPQL = entity/field names.
- Bulk update: transaction + `clearAutomatically`.

## ❓ Checks (letters only)

1. `findByCustomerIdAndBalanceGreaterThan(7, 15000)` with balances 52000, 15000, 900: A. 1 account · B. 2 accounts
2. `@Query("select a from accounts a")`: A. works · B. fails at startup (no entity named `accounts`)
3. ACC-1 is loaded, then `freezeAllOfCustomer(7)` runs with plain `@Modifying`. `findByNumber("ACC-1")` in the same transaction says: A. FROZEN · B. ACTIVE

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** Spring Data repositories are in nearly every Spring codebase, and interviewers like to ask how a method name becomes a query and what goes wrong with bulk updates, because it shows whether you understand the persistence context underneath.

**The concept.** Spring Data reads each method name at startup (`find` + `By` + properties + operators), builds a JPQL query from it, and puts it in a generated proxy. When a name would be unreadable, you write the JPQL yourself with `@Query`.

**Small example.** Sample data:

| number | owner | customerId | balance |
|---|---|---|---|
| ACC-1 | Asha Rao | 7 | 52000.00 |
| ACC-2 | Asha Rao | 7 | 900.00 |
| ACC-3 | Asha Rao | 7 | 15000.00 |
| ACC-4 | Vikram Shah | 8 | 80000.00 |

Real result of `findByCustomerIdAndBalanceGreaterThanOrderByBalanceDesc(7, 1000)`: `[ACC-1, ACC-3]`.

| Row | In or out | Why |
|---|---|---|
| ACC-1 | ✅ in, first | customer 7, 52000 > 1000, biggest |
| ACC-3 | ✅ in, second | customer 7, 15000 > 1000 |
| ACC-2 | ❌ out | 900 is not > 1000 |
| ACC-4 | ❌ out | customer 8, even though 80000 is the biggest balance |

**Gotchas**
- `GreaterThan` is strict; `GreaterThanEqual` includes the boundary.
- Derived `deleteBy...` loads each entity and deletes it one by one; a `@Modifying` JPQL delete is one statement.
- `@DataJpaTest` does not load your `@Service` or `@RestController` beans; it is a slice.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **A.** Only 52000; `GreaterThan` excludes 15000.
2. **B.** JPQL needs the entity name `Account`.
3. **B.** The bulk update bypassed the already-loaded object; `clearAutomatically = true` fixes it.

</details>
