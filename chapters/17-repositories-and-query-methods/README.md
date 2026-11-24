# Chapter 17: Repositories and query methods

By the end of this chapter you can let Spring Data write Ledgerly's queries from method names, write JPQL with `@Query` when a name is not enough, and run a bulk update that does not leave stale objects behind.

## Why you need this

Ops ask for "all accounts of customer 7 with more than ₹1,000, biggest first", "how many accounts are frozen", "search owners by name" and a one-click "freeze every account of this customer" during a fraud alert. With chapter 15's `JdbcClient` each one is a hand-written SQL string and a row mapper. With Spring Data most of them are one line in an interface.

## What you will learn

- **`JpaRepository<Account, Long>`:** `save`, `saveAll`, `findById`, `findAll`, `count`, `deleteById` for free; Spring Data writes a proxy at startup.
- **Derived queries:** `findByNumber`, `findByCustomerIdAndBalanceGreaterThanOrderByBalanceDesc`, `countByStatus`, `existsByNumber`.
- **`@Query` with JPQL:** entity and field names, `:named` parameters with `@Param`.
- **`@Modifying` bulk updates:** need a transaction, return a row count, skip the persistence context (`clearAutomatically`).
- **`@DataJpaTest`:** a test slice with only JPA, H2 and your repositories, rolled back after each test.

## Real output

`mvn -q -pl chapters/17-repositories-and-query-methods spring-boot:run` on the finished code:

```
repository class      $Proxy109 (a proxy Spring Data wrote)
count()               4
findByNumber(ACC-3)   Optional[ACC-3(Asha Rao, cust 7, ACTIVE, 15000.00)]
findByNumber(ACC-9)   Optional.empty
cust 7, > 1000        [ACC-1(Asha Rao, cust 7, ACTIVE, 52000.00), ACC-3(Asha Rao, cust 7, ACTIVE, 15000.00)]
existsByNumber(ACC-4) true
searchOwner("asha")   [ACC-1, ACC-2, ACC-3]
freeze customer 7     3 rows
countByStatus(FROZEN) 3
```

Without `@Transactional` on the freeze method the same tour stops with this real error:

```
org.springframework.dao.InvalidDataAccessApiUsageException: No active transaction for update or delete query
```

## The shape of a repository

```java
public interface AccountRepository extends JpaRepository<Account, Long> {
  Optional<Account> findByNumber(String number);                       // derived from the name

  @Query("select a from Account a where lower(a.owner) like lower(concat('%', :name, '%'))")
  List<Account> searchOwner(@Param("name") String name);               // JPQL, not SQL
}
```

## Try it

```bash
mvn -q -pl chapters/17-repositories-and-query-methods spring-boot:run
mvn -pl chapters/17-repositories-and-query-methods test -Popen
```

On the stub, the tour stops at the first method with `UnsupportedOperationException: Task 1: ...`.

## Your turn

You edit `AccountRepository.java` and `interview-answer.md`. `Account` is finished. Each stub method has a `default` body that throws; replace it.

1. **`findByNumber`.** Derived, returns `Optional`.
2. **Customer + balance.** Derived with `And`, `GreaterThan`, `OrderBy...Desc`.
3. **`countByStatus`, `existsByNumber`.** Derived.
4. **`@Query`.** (a) case-insensitive owner search in JPQL; (b) `@Transactional @Modifying(clearAutomatically = true)` bulk freeze.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- A typo in a derived name (`findByNumbr`) fails at **startup** with "No property 'numbr' found", not at compile time.
- `findById` returns `Optional.empty()`, never `null`; don't null-check it.
- JPQL uses `Account` and `a.owner`, not `accounts` and `owner` columns. A table name in JPQL fails at startup.
- `@Modifying` without `clearAutomatically`: `ACC-1` loaded earlier still says ACTIVE after the freeze.
- A bulk update without a transaction: `No active transaction for update or delete query`.

**Interview question (task 5).** What does `JpaRepository` give you and how does a derived method work? When `@Query`, and JPQL vs SQL? What do `@Modifying` and `clearAutomatically` do? The questions are repeated at the top of `interview-answer.md`.
