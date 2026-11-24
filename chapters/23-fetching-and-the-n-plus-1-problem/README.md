# Chapter 23: Fetching and the N+1 problem

By the end of this chapter you can spot the N+1 problem in Ledgerly's SQL log and fix it three ways (`JOIN FETCH`, `@EntityGraph`, `@BatchSize`), and you know why a `LazyInitializationException` happens and why "just make it EAGER" or "keep the session open" are the wrong fixes.

## Why you need this

The branch report "customers and their accounts" took 9 seconds in production. The SQL log showed one select for 4,000 customers, then 4,000 more selects, one per customer, each loading that customer's accounts. Locally, with 5 test customers, nobody noticed. This is the most common JPA performance bug there is.

## What you will learn

- **Lazy vs eager:** collections are lazy by default; to-one is eager by default (make it lazy, chapter 20).
- **Spotting N+1:** one query for the list, then one per row.
- **`JOIN FETCH`:** parents + children in one select (`distinct` to avoid repeated parents).
- **`@EntityGraph(attributePaths = ...)`:** the same for derived queries, no JPQL.
- **`@BatchSize`:** keeps it lazy, but loads children for many parents in one `in (...)` select.
- **`LazyInitializationException` and open-session-in-view.**

## Real output

On the **stub**, task 2's report (real, cut): 1 select for the accounts, then 1 per customer.

```
Hibernate: select a1_0.id,a1_0.balance,a1_0.customer_id,a1_0.number from account a1_0 where a1_0.balance>? order by a1_0.number
Hibernate: select c1_0.id,c1_0.name from customer c1_0 where c1_0.id=?
ACC-12 Asha Rao
Hibernate: select c1_0.id,c1_0.name from customer c1_0 where c1_0.id=?
ACC-22 Vikram Shah
... 3 more pairs ...
```

The finished code, `mvn -q -pl chapters/23-fetching-and-the-n-plus-1-problem spring-boot:run`:

```
== A. accounts over 20000 with their owner (task 2)
Hibernate: select a1_0.id,a1_0.balance,a1_0.customer_id,c1_0.id,c1_0.name,a1_0.number from account a1_0 join customer c1_0 on c1_0.id=a1_0.customer_id where a1_0.balance>? order by a1_0.number
ACC-12 Asha Rao
... 4 more ...
== B. customers with accounts, join fetch (task 1)
Hibernate: select distinct c1_0.id,a1_0.customer_id,a1_0.id,a1_0.balance,a1_0.number,c1_0.name from customer c1_0 left join account a1_0 on c1_0.id=a1_0.customer_id order by c1_0.name
Asha Rao 2
... 4 more ...
== C. ownerSummaries (tasks 3 + 4)
Hibernate: select c1_0.id,c1_0.name from customer c1_0
Hibernate: select a1_0.customer_id,a1_0.id,a1_0.balance,a1_0.number from account a1_0 where a1_0.customer_id in (?,?,?, ... 50 placeholders ...)
Asha Rao: 2 accounts
... 4 more ...
```

A and B: one select each. C: two selects, whatever the number of customers (up to 50 per batch).

## Try it

```bash
mvn -q -pl chapters/23-fetching-and-the-n-plus-1-problem spring-boot:run
mvn -pl chapters/23-fetching-and-the-n-plus-1-problem test -Popen
```

The checks count the SQL statements with Hibernate statistics, so "it works" is not enough: it must be one (or two) selects.

## Your turn

You edit `CustomerRepository.java`, `AccountRepository.java`, `Customer.java`, `StatementService.java` and `interview-answer.md`.

1. **`JOIN FETCH`.** `findAllWithAccounts` with `select distinct c from Customer c left join fetch c.accounts order by c.name`.
2. **`@EntityGraph`.** `attributePaths = "customer"` on `findByBalanceGreaterThanOrderByNumber`.
3. **`@BatchSize(size = 50)`** on `Customer.accounts` (keep it lazy).
4. **Lazy loading in the service.** `@Transactional(readOnly = true)` on `ownerSummaries`.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- `FetchType.EAGER` on a collection: every customer load drags its accounts in, even for a name lookup, and can still be N+1.
- `JOIN FETCH` + `Pageable`: Hibernate warns `HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory` and pages in Java after loading everything.
- Two `JOIN FETCH`es of two `List` collections: `MultipleBagFetchException`.
- Open-session-in-view (Spring Boot's default, `spring.jpa.open-in-view=true`) hides the `LazyInitializationException` by running N+1 during JSON rendering and holding a DB connection for the whole request. This lab turns it off.

**Interview question (task 5).** What is N+1 and how do you spot it? `JOIN FETCH` vs `@EntityGraph` vs `@BatchSize`? Why the `LazyInitializationException`, and why turn open-session-in-view off? The questions are repeated at the top of `interview-answer.md`.
