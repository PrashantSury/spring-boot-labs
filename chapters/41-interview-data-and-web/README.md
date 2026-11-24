# Chapter 41: Interview: data and web

The last chapter is a rehearsal for the data-and-web round: REST status codes and errors, JPA fetching and transactions, validation, and security end to end. Each task fixes a bug interviewers love to ask about. Then you write the spoken answers, naming the default behaviour first and then how you changed it.

## Why you need this

In this round the interviewer draws a request on the whiteboard and keeps asking "and then?". DispatcherServlet, controller, validation, service, transaction, JPA, SQL, and back. The strongest answers are stories. "Our customer list ran 4 queries for 3 customers, and 1,001 for 1,000. I saw it in Hibernate's statistics and fixed it with a join fetch." That is the kind of story this chapter gives you.

## What you will rehearse

- **Request flow and status codes:** 201 + `Location`, 404, 409 as problem+json.
- **JPA:** lazy loading, `LazyInitializationException` with open-in-view off, N+1 and the join fetch, read-only transactions.
- **Validation:** `@Valid` bodies with field errors, `@Max` on a request parameter.
- **Security end to end:** a URL rule for staff, `@PreAuthorize` for "only yourself", 401 vs 403.

## Real output

The finished app on port 8101 (HTTP Basic: asha, ravi = customers 1 and 2, ops = ADMIN, password = name + `-pass`; `[403]` is the HTTP status):

```
$ curl -u ops:ops-pass localhost:8101/customers
[{"id":1,"name":"Asha","accounts":2},{"id":2,"name":"Ravi","accounts":1},{"id":3,"name":"Meera","accounts":3}] [200]
$ curl -u asha:asha-pass localhost:8101/customers
{"timestamp":"2026-11-24T11:30:28.381Z","status":403,"error":"Forbidden","path":"/customers"} [403]
$ curl -u asha:asha-pass localhost:8101/customers/1
{"id":1,"name":"Asha","accounts":[{"number":"ACC-1","balance":1000.00},{"number":"ACC-2","balance":250.00}]} [200]
$ curl -u asha:asha-pass localhost:8101/customers/2
{"timestamp":"2026-11-24T11:30:28.609Z","status":403,"error":"Forbidden","path":"/customers/2"} [403]
$ curl -i -u asha:asha-pass -X POST localhost:8101/payees -H Content-Type:application/json -d '{"name":"Kiran Rao","ifsc":"HDFC0001234","accountNumber":"50100012345678"}'
HTTP/1.1 201
Location: /payees/1
{"id":1,"name":"Kiran Rao","ifsc":"HDFC0001234","accountNumber":"50100012345678"}
$ (the same again)
HTTP/1.1 409
{"detail":"a payee with account 50100012345678 already exists","instance":"/payees","status":409,"title":"Duplicate payee"}
$ (name "", ifsc HDFC1234, accountNumber 123)
{"detail":"check the errors","instance":"/payees","status":400,"title":"Invalid request","errors":{"name":"must not be blank","ifsc":"must be a valid IFSC code","accountNumber":"must be 9 to 18 digits"}} [400]
$ curl -u asha:asha-pass localhost:8101/payees/999
{"detail":"no payee 999","instance":"/payees/999","status":404,"title":"Not found"} [404]
$ curl -u asha:asha-pass 'localhost:8101/payees?size=500'
{"detail":"Validation failure","instance":"/payees","status":400,"title":"Bad Request"} [400]
```

On the stub, `GET /customers` fails with `LazyInitializationException: Cannot lazily initialize collection of role 'ledgerly.Customer.accounts' ... (no session)`, a new payee is a plain 200, and a duplicate is a 500.

## Try it

```bash
mvn -pl chapters/41-interview-data-and-web spring-boot:run
mvn -pl chapters/41-interview-data-and-web test -Popen
```

H2 in memory; no Docker. Hibernate statistics are on, so the N+1 check can count SQL statements.

## Your turn

You edit `CustomerQueries.java`, `CustomerRepository.java`, `PayeeController.java`, `ApiErrors.java`, `PayeeRequest.java`, `SecurityConfig.java` and `interview-answer.md`.

1. **JPA fetching.** `@Transactional(readOnly = true)` on the reads; `findAllWithAccounts()` with a join fetch (1 SQL statement).
2. **Status codes.** 201 + `Location`, 409 for a duplicate, 404 for an unknown payee, as problems.
3. **Validation.** Constraints on `PayeeRequest` + `@Valid`; `@Max(100)` on `size`.
4. **Security.** `@EnableMethodSecurity`; `GET /customers` ADMIN only; `@PreAuthorize` so a customer reads only themselves.
5. **(Interview)** Four spoken answers in `interview-answer.md`.

**Traps**

- Turning open-in-view back on to "fix" lazy loading hides N+1 queries and holds a DB connection for the whole request.
- `join fetch` on two collections at once gives a Cartesian product (or `MultipleBagFetchException`); fetch one, batch the other.
- `existsBy...` then `save` can race; the unique constraint is the real guard (catch `DataIntegrityViolationException` → 409).
- `@Max` on a parameter does nothing unless Spring validates the method; Spring 6.1+ does it for controllers with constraint annotations.
- Name the default first: "Spring returns 200 by default; I return 201 with `Location` because…".

**Interview question (task 5).** The request flow and status codes. Entity states, N+1, transactions and locking. Validation and testing. JWT and method security end to end, 401 vs 403. The questions are repeated at the top of `interview-answer.md`.
