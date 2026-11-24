# Chapter 19: Projections and aggregations

By the end of this chapter Ledgerly's spending reports read only the columns they need and let the database do the adding up: interface and record projections, `GROUP BY` with `SUM`/`COUNT`, a safe empty total, and one native query.

## Why you need this

The "spend by category" screen loaded every `Txn` entity of a customer (each with 2 KB of raw bank-network payload), then summed amounts in a Java loop. For a business account with 20,000 rows that is 40 MB read to print three numbers. The database can return those three numbers directly.

## What you will learn

- **Interface projections:** return `List<TxnLine>` and Spring Data selects only `description, amount`.
- **Constructor expressions:** `select new ledgerly.CategoryTotal(t.category, sum(t.amount), count(t))` fills a record per group.
- **`GROUP BY`, `SUM`, `COUNT` in JPQL.**
- **`SUM` over no rows is `NULL`:** `coalesce(sum(...), 0)`.
- **Native queries:** real SQL (`nativeQuery = true`) for database functions JPQL doesn't have.

## Real output

`mvn -q -pl chapters/19-projections-and-aggregations spring-boot:run` on the finished code (SQL shown with `show-sql`):

```
== lines (projection)
Hibernate: select t1_0.description,t1_0.amount from txn t1_0 where t1_0.account_number=? order by t1_0.amount desc
Rent October 25000
Rent November 25000
IndiGo PNQ-BLR 3200
BigBasket 1200
DMart 800
Zepto 450
Hibernate: select t1_0.category,sum(t1_0.amount),count(t1_0.id) from txn t1_0 where t1_0.account_number=? group by t1_0.category order by sum(t1_0.amount) desc
== by category       [CategoryTotal[category=RENT, total=50000, count=2], CategoryTotal[category=TRAVEL, total=3200, count=1], CategoryTotal[category=GROCERY, total=2450, count=3]]
== total ACC-1       55650
== total ACC-9       0
== monthly
Hibernate: select formatdatetime(booked_at, 'yyyy-MM') as period, sum(amount) as total from txn where account_number = ? group by formatdatetime(booked_at, 'yyyy-MM') order by period
2026-10 27000
2026-11 28650
```

The first select has two columns, not seven: no `raw_payload`. Without `coalesce`, `totalSpent("ACC-9")` stops the tour with this real error:

```
org.springframework.aop.AopInvocationException: Null return value from advice does not match primitive return type for: public abstract long ledgerly.TxnRepository.totalSpent(java.lang.String)
```

## Try it

```bash
mvn -q -pl chapters/19-projections-and-aggregations spring-boot:run
mvn -pl chapters/19-projections-and-aggregations test -Popen
```

## Your turn

You edit `TxnRepository.java` and `interview-answer.md`. The entity, projections and sample data are finished. Each stub method has a `default` body that throws; replace it.

1. **Interface projection.** `List<TxnLine> findByAccountNumberOrderByAmountDesc(String)`.
2. **Group by.** `spendByCategory` with `select new ledgerly.CategoryTotal(...)`.
3. **Safe total.** `totalSpent` with `coalesce(sum(t.amount), 0)`.
4. **Native query.** `monthlyTotals` with `formatdatetime` and `nativeQuery = true`.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- Loading entities to read two fields: memory, bandwidth, and lazy-loading surprises.
- `select new CategoryTotal(...)` without the package (`ledgerly.`) fails at startup.
- `SUM` of nothing is `NULL`; a primitive `long` cannot hold it.
- `sum(t.amount)` is a `Long` in JPQL; a record component of type `int` won't match the constructor.
- `month` is a reserved word in H2; aliases like `period` avoid it. Native SQL is database-specific.

**Interview question (task 5).** How do you read only a few columns, and which projection types exist? Totals per category in the database, and SUM over no rows? Native query vs JPQL: when, and what do you give up? The questions are repeated at the top of `interview-answer.md`.
