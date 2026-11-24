# Chapter 35: Calling other services with RestClient

By the end of this chapter Ledgerly screens every payee with an external sanctions partner before a transfer, survives the partner being slow or briefly down, and reports finished transfers without ever sending one twice.

## Why you need this

Compliance says no transfer may go out before the payee is checked against the sanctions list, and that list lives at a partner company behind an HTTP API. Last quarter a different partner slowed to 60-second answers for an hour. The bank's old app had no timeouts, every request thread ended up waiting, and the whole app went down with the partner. Then someone added "retry everything", and a retried POST filed the same report three times.

## What you will learn

- **`RestClient`:** `get()`/`post()`, `uri(...)` with template variables, `retrieve()`, `body(...)`, `toBodilessEntity()`.
- **`onStatus`:** turn a 400 or 5xx into your own exceptions instead of raw `HttpClientErrorException`s.
- **Timeouts:** `spring.http.clients.connect-timeout` / `read-timeout` on Boot's `RestClient.Builder`; a timeout arrives as `ResourceAccessException`.
- **Retries:** what to retry (an idempotent GET on 503), what not to (a 400, a timeout, a POST).
- **Idempotency keys** for calls that move or report money.
- **Testing:** `MockRestServiceServer` for no-network unit tests, and a small real HTTP server (`FakeSanctions`) for timeouts.

## Real output

`mvn -q -pl chapters/35-calling-other-services-with-restclient spring-boot:run` starts the fake partner on port 8097 and walks through every case (finished code):

```
screen Acme Traders      -> ScreenResult[name=Acme Traders, blocked=false]  (134 ms)
screen Evil Corp         -> ScreenResult[name=Evil Corp, blocked=true]  (3 ms)
screen after two 503s    -> ScreenResult[name=Acme Traders, blocked=false]  (48 ms)
screen ''                -> PayeeRejected: partner rejected payee ''  (3 ms)
screen Slow Co           -> SanctionsUnavailable: partner timed out  (2001 ms)
report TX-1              -> accepted  (23 ms)
report TX-2 during a 503 -> SanctionsUnavailable: partner answered 503  (2 ms)
partner saw:
  GET /screen?name=Acme Traders -> 200
  GET /screen?name=Evil Corp -> 200
  GET /screen?name=Acme Traders -> 503
  GET /screen?name=Acme Traders -> 503
  GET /screen?name=Acme Traders -> 200
  GET /screen?name= -> 400
  POST /reports key=TX-1 -> 202
  POST /reports key=TX-2 -> 503
```

The partner logs a request when it answers. "Slow Co" is missing from its log because Ledgerly hung up after 2 s and the tour ended before the 4-second answer. On the stub, every `screen` line is `UnsupportedOperationException: Task 1` and the POSTs arrive with `key=null`.

## The shape of it

```java
client.get().uri("/screen?name={name}", payee).retrieve()
    .onStatus(s -> s.value() == 400, (req, res) -> { throw new PayeeRejected(payee); })
    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> { throw new SanctionsUnavailable(..., true); })
    .body(ScreenResult.class);
```

```properties
spring.http.clients.connect-timeout=2s
spring.http.clients.read-timeout=2s
```

## Try it

```bash
mvn -q -pl chapters/35-calling-other-services-with-restclient spring-boot:run
mvn -pl chapters/35-calling-other-services-with-restclient test -Popen
```

The tests start `FakeSanctions` on a random port, so nothing else needs to run. The timeout test takes about 2 seconds.

## Your turn

You edit `SanctionsClient.java`, `application.properties` and `interview-answer.md`.

1. **GET and map.** `screen(payee)` → `GET /screen?name={name}` → `ScreenResult`.
2. **Error statuses.** 400 → `PayeeRejected`; 5xx → `SanctionsUnavailable("partner answered 503", true)`.
3. **Timeouts.** 2 s connect and read in `application.properties`; `ResourceAccessException` → `SanctionsUnavailable("partner timed out", false)`.
4. **Retry and idempotency.** Up to 3 attempts for a retryable failure; `report` sends `Idempotency-Key` and is never retried.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- No timeout: the default can wait forever, and every waiting call holds a thread.
- `"/screen?name=" + payee` breaks on spaces and `&`; use `{name}` template variables.
- Retrying a 400 just repeats the same bad request; retrying a timeout piles more load on a partner that is already struggling.
- Retrying a POST can file the same report (or payment) twice; an idempotency key lets the other side spot the duplicate.

**Interview question (task 5).** Calling a service with RestClient and mapping its errors. Why timeouts matter and which calls are safe to retry. How you test a client without the real partner. The questions are repeated at the top of `interview-answer.md`.
