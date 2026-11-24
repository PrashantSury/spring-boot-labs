# Chapter 18: Pagination and sorting

By the end of this chapter Ledgerly's statement endpoint returns one page of transactions at a time, sorted the way the client asks (but only by allowed fields), and the mobile feed scrolls with a cheaper `Slice`.

## Why you need this

A business customer of Ledgerly has 20,000 transactions. The statement screen called `findAll`, the API built a 6 MB JSON, the phone froze and the database did a full scan for every refresh. Nobody reads 20,000 rows at once: the client wants 20 rows, the newest first, and a "next page" button.

## What you will learn

- **`Pageable` / `PageRequest.of(page, size, sort)`:** Spring Data adds `ORDER BY`, `OFFSET` and `FETCH FIRST n ROWS` to the query.
- **`Page<T>`:** rows + `totalElements` + `totalPages`, paid for with an extra `count` query.
- **`Slice<T>`:** rows + `hasNext()`, no count query.
- **`Sort`:** direction, several properties, a tie-breaker for stable pages.
- **Defending the endpoint:** page numbers start at 0, cap the size, whitelist the sort property.

## Real output

The finished app on port 8089 (45 seeded transactions for ACC-1):

```
$ curl "localhost:8089/accounts/ACC-1/transactions?size=2"
{"content":[{"id":45,"amount":1265,"bookedAt":"2026-11-15","description":"UPI #45"},{"id":44,"amount":1128,"bookedAt":"2026-11-14","description":"UPI #44"}],"page":0,"size":2,"totalElements":45,"totalPages":23}

$ curl "localhost:8089/accounts/ACC-1/transactions?size=2&sort=amount,desc"
{"content":[{"id":36,"amount":5032,"bookedAt":"2026-11-06","description":"UPI #36"},{"id":35,"amount":4895,"bookedAt":"2026-11-05","description":"UPI #35"}],"page":0,"size":2,"totalElements":45,"totalPages":23}

$ curl "localhost:8089/accounts/ACC-1/transactions?sort=description"
{"timestamp":"2026-11-24T10:18:36.515Z","status":400,"error":"Bad Request","path":"/accounts/ACC-1/transactions"}

$ curl "localhost:8089/accounts/ACC-1/feed?page=4"
{"content":[{"id":5,"amount":785,...}, ... 5 rows ...],"page":4,"hasNext":false}
```

The SQL Hibernate sent (real, with `spring.jpa.show-sql=true`):

```
-- a Page: the rows AND a count
select ... from txn t1_0 where t1_0.account_number=? order by t1_0.booked_at desc,t1_0.id desc fetch first ? rows only
select count(t1_0.id) from txn t1_0 where t1_0.account_number=?
-- a Slice: only the rows (it asks for size + 1 to know if there is a next page)
select ... from txn t1_0 where t1_0.account_number=? order by t1_0.booked_at desc,t1_0.id desc offset ? rows fetch first ? rows only
```

## The shape of it

```java
Page<Txn> findByAccountNumber(String accountNumber, Pageable pageable);          // repository

Page<Txn> p = repo.findByAccountNumber("ACC-1",
    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "bookedAt", "id")));      // controller
```

## Try it

```bash
mvn -pl chapters/18-pagination-and-sorting spring-boot:run
mvn -pl chapters/18-pagination-and-sorting test -Popen
```

On the stub every endpoint answers 500 (`UnsupportedOperationException: Task 1: ...`).

## Your turn

You edit `TxnRepository.java`, `StatementController.java` and `interview-answer.md`. `Txn`, `SampleData` and the response records are finished.

1. **Page query.** `Page<Txn> findByAccountNumber(String, Pageable)`.
2. **Statement endpoint.** Defaults page 0 / size 20 / `bookedAt,desc`, size capped at 100, map to `PageResponse`.
3. **Sort parsing.** `sortFrom("amount,desc")`; only `bookedAt`, `amount`; else 400; `id` as a tie-breaker.
4. **Feed.** `Slice<Txn> findSliceByAccountNumber` + `/accounts/{number}/feed` (size 10).
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- Page numbers start at **0**. `?page=1` is the second page.
- Passing a client's sort string straight to `Sort.by(...)`: an unknown property is a 500 that names your entity's fields; whitelist it.
- No size cap: `?size=1000000` is `findAll` again.
- Sorting only by `bookedAt`: rows with the same date can swap between page 1 and page 2. Add `id`.
- A `Page` on a 50-million-row table: the `count` query can be slower than the page. Use a `Slice` when nobody needs "page 3 of 2,500,000".

**Interview question (task 5).** How do you page and sort in Spring Data, and what do `Page`/`Pageable` give you? When is a `Slice` better? Why check a user-supplied sort, and what else do you limit? The questions are repeated at the top of `interview-answer.md`.
