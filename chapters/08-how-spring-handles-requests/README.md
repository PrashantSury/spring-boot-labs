# Chapter 8: How Spring handles requests

By the end of this chapter you can follow one request through the `DispatcherServlet`, say how Spring picks a method when several could match, narrow a mapping with `produces`, `consumes`, `params` and `headers`, and name the exception behind every 400, 404, 405, 406 and 415.

## Why you need this

Ledgerly's mobile team reports three bugs in one morning: `GET /accounts/dormant` answers 404 "no account dormant", the statement download gives JSON when they asked for CSV, and a partner who posts `text/plain` gets a confusing error. None of these is in Ledgerly's code. All three are about how Spring **chooses** a method, or refuses to call one, before your code runs.

## What you will learn

- **The flow:** `DispatcherServlet` → handler mapping → handler adapter → argument resolvers → your method → message converters → response.
- **Choosing a method:** a fixed path beats a pattern; then `produces`, `consumes`, `params` and `headers` narrow the match.
- **Built-in answers:** 400, 404, 405, 406, 415 and the exception behind each.

## One request, step by step

```
GET /accounts/ACC-1/statement   Accept: text/csv
 1. DispatcherServlet receives it
 2. Handler mapping: which @GetMapping matches the path AND the Accept header?  -> asCsv(...)
 3. Handler adapter calls it; argument resolvers fill @PathVariable number = "ACC-1"
 4. asCsv returns a String
 5. A message converter writes it as text/csv
```

If step 2 finds the path but not the method → 405. Not the path → 404. Not the `Content-Type` → 415. Not what `Accept` wants → 406. If step 3 can't convert `?amount=abc` to a `long` → 400.

## Real output

The finished app on port 8082, real `curl` output:

```
$ curl localhost:8082/accounts/dormant
["ACC-3"]
[200]
$ curl -H "Accept: text/csv" localhost:8082/accounts/ACC-1/statement
date,amount
2026-11-01,50000
2026-11-03,-1200
[200]
$ curl -H "Accept: application/xml" localhost:8082/accounts/ACC-1/statement
[406]
$ curl -X POST -H "Content-Type: text/plain" -d x localhost:8082/transfers
{"timestamp":"2026-11-24T09:53:37.982Z","status":415,"error":"Unsupported Media Type","path":"/transfers"}
$ curl -X POST localhost:8082/fee
{"timestamp":"2026-11-24T09:53:37.991Z","status":405,"error":"Method Not Allowed","path":"/fee"}
$ curl "localhost:8082/fee?amount=abc"
{"timestamp":"2026-11-24T09:53:37.999Z","status":400,"error":"Bad Request","path":"/fee"}
```

(`[200]` is the status `curl -w` printed.) On the stub, `/accounts/dormant` answers `{"status":404,...}` because it lands in `byNumber("dormant")`.

## Narrowing a mapping

```java
@GetMapping(path = "/accounts/{number}/statement", produces = "text/csv")   // only if Accept allows text/csv
@PostMapping(path = "/transfers", consumes = "application/json")             // only if the body is JSON
@GetMapping(path = "/rates", params = "currency")                            // only if ?currency=... is present
@GetMapping(path = "/branches", headers = "X-Api-Version=2")                 // only with that header
```

## Try it

```bash
mvn -pl chapters/08-how-spring-handles-requests spring-boot:run
curl.exe localhost:8082/accounts/dormant
curl.exe -H "Accept: text/csv" localhost:8082/accounts/ACC-1/statement
mvn -pl chapters/08-how-spring-handles-requests test -Popen
```

## Your turn

You edit `AccountController.java`, `StatementController.java`, `TransferController.java`, `RateController.java`, `HttpAnswers.java` and `interview-answer.md`.

1. **Fixed beats pattern.** `GET /accounts/dormant` → `["ACC-3"]`.
2. **`produces`.** One path, two methods: JSON or CSV by `Accept`; XML → 406.
3. **`consumes`.** `POST /transfers` takes JSON only; `text/plain` → 415.
4. **`params` and `headers`.** `/rates?currency=USD` vs `/rates`; `/branches` with `X-Api-Version: 2`.
5. **Built-in answers.** `HttpAnswers.statusFor(exception)`; the check compares it with real requests.
6. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- Task 1: don't add `if ("dormant".equals(number))` inside `byNumber`. Add a mapping.
- Task 4: two methods on plain `"/rates"` fail at startup with "Ambiguous mapping". `params = "!currency"` means "only when it is absent".
- 405 means the path **exists**. 404 means it doesn't.
- Bad JSON is a 400 (`HttpMessageNotReadableException`), not a 500.

**Interview question (task 6).** What does the `DispatcherServlet` do with one request? With several matching mappings, how does Spring choose? What do 404, 405, 406 and 415 each tell you? The questions are repeated at the top of `interview-answer.md`.
