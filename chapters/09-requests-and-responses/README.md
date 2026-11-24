# Chapter 9: Requests and responses

By the end of this chapter you can read data from the path, the query string, headers and the JSON body, answer with the right status (200, 201, 204, 404) and headers (`Location`, `Cache-Control`), and control what the JSON looks like.

## Why you need this

The mobile team is building three screens on Ledgerly's API: "open account", "close account" and "mini statement". Their review comments: "open account answers 200 and doesn't say where the new account is", "close account sends back an empty JSON object", "the statement leaks the core-banking reference `CBS-99812`". The data was right every time. The **HTTP** around it was wrong.

## What you will learn

- **Where data comes in:** `@PathVariable`, `@RequestParam` (with defaults), `@RequestHeader`, `@RequestBody`.
- **`ResponseEntity`:** choose the status, headers and body: `created(uri)`, `noContent()`, `notFound()`, `ok().cacheControl(...)`.
- **Jackson basics:** `@JsonProperty` renames a field, `@JsonIgnore` hides one, `LocalDate` becomes `"2026-11-01"`, unknown fields are ignored.

## Real output

The finished app on port 8083 (`curl -i`, headers trimmed):

```
$ curl -X POST -H "Content-Type: application/json" -d '{"holder":"Asha Rao","openingBalance":5000,"nickname":"A"}' localhost:8083/accounts
HTTP/1.1 201
Location: /accounts/ACC-4
{"number":"ACC-4","holder":"Asha Rao","balance":5000}

$ curl -X DELETE localhost:8083/accounts/ACC-2
HTTP/1.1 204

$ curl -H "X-Request-Id: req-42" "localhost:8083/accounts/ACC-1/transactions?from=2026-11-03"
HTTP/1.1 200
X-Request-Id: req-42
[{"txn_id":"T2","date":"2026-11-03","amount":-1200},{"txn_id":"T3","date":"2026-11-20","amount":-800}]

$ curl localhost:8083/accounts/ACC-1/balance
HTTP/1.1 200
Cache-Control: no-store
{"number":"ACC-1","balance":52000,"asOf":"2026-11-24"}
```

Notice: `nickname` was ignored, the transaction has `txn_id` and no `internalRef`, and `from=2026-11-03` **includes** that day.

## Which status for which action

| Action | Success | Body | Extra header | Failure |
|---|---|---|---|---|
| read `GET /accounts/ACC-1` | 200 OK | the account | | 404 |
| create `POST /accounts` | 201 Created | the new account | `Location: /accounts/ACC-4` | 400 bad input |
| delete `DELETE /accounts/ACC-2` | 204 No Content | none | | 404 |

```java
return ResponseEntity.created(URI.create("/accounts/" + number)).body(account);   // 201 + Location
return ResponseEntity.noContent().build();                                        // 204, no body
return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(balance);    // 200 + header
```

## Try it

```bash
mvn -pl chapters/09-requests-and-responses spring-boot:run
curl.exe -i -X POST -H "Content-Type: application/json" -d "{\"holder\":\"Asha Rao\",\"openingBalance\":5000}" localhost:8083/accounts
mvn -pl chapters/09-requests-and-responses test -Popen
```

On the stub, only `GET /accounts/{number}` is mapped; the rest answer 404 or 405.

## Your turn

You edit `AccountController.java`, `OpenAccountRequest.java`, `TransactionView.java` and `interview-answer.md`.

1. **Create.** `POST /accounts` → 201, `Location`, the new account; blank holder or negative balance → 400; unknown fields ignored explicitly.
2. **Delete.** `DELETE /accounts/{number}` → 204 or 404.
3. **Params and headers.** `/transactions?from=&limit=` (optional date, default 10), echo `X-Request-Id`.
4. **JSON shape.** `txn_id`, ISO date, no `internalRef`.
5. **Headers on a read.** `/balance` with `Cache-Control: no-store`; unknown → 404 with no body.
6. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- Task 1: `ResponseEntity.ok(account)` is 200, not 201. Use `created(...)`.
- Task 2: `ResponseEntity<Void>` and `noContent()`. Returning the deleted account makes it a 200.
- Task 3: `from` is **on or after**: `!date.isBefore(from)`. `limit` needs `defaultValue = "10"`, or a missing limit is a 400.
- Task 4: renaming the record component to `txn_id` breaks Java naming; use `@JsonProperty`.

**Interview question (task 6).** Where can a request carry data, and which annotation reads each? Which status and headers should create, delete and read answer, and why `ResponseEntity`? What decides JSON in and out, and what happens with `text/plain` or an unknown field? The questions are repeated at the top of `interview-answer.md`.
