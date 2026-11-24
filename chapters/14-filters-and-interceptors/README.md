# Chapter 14: Filters and interceptors

By the end of this chapter every Ledgerly request carries a correlation id through all its log lines, the ops endpoints check a key before the controller runs, and you know exactly where a servlet filter runs and where a Spring MVC interceptor runs.

## Why you need this

A customer says "my transfer failed at 3:02 PM". Ledgerly writes 4 million log lines a day, and the lines of one request are mixed with everybody else's. With a correlation id on every line (and in the response header the app shows on its error screen) support finds the 6 lines that matter in one search. Meanwhile `/ops/cache-stats` is open to the internet; it needs a quick key check before the controller.

## What you will learn

- **Servlet `Filter` / `OncePerRequestFilter`:** wraps the whole request, before the `DispatcherServlet`; sees every URL.
- **`HandlerInterceptor`:** runs inside Spring MVC, after the handler is chosen; knows WHICH controller method; registered per path pattern.
- **MDC:** a per-thread map that the log pattern prints (`%X{cid}`); must be cleared in `finally`.
- **`FilterRegistrationBean`:** URL patterns and order (lower runs first and wraps the rest).
- **Errors in filters:** they never reach `@RestControllerAdvice`; the filter writes the response itself.

## Real output

The finished app on port 8088:

```
$ curl -i localhost:8088/whoami
X-Correlation-Id: 37df9635-48c8-4ce4-8e77-c00707f5cafe
X-Handler: LedgerController#whoami
cid=37df9635-48c8-4ce4-8e77-c00707f5cafe

$ curl -H "X-Correlation-Id: r-77" localhost:8088/accounts/ACC-1/balance
52000

$ curl -i -H "X-Correlation-Id: bad id!" localhost:8088/whoami
HTTP/1.1 400
{"title":"Invalid X-Correlation-Id","status":400}

$ curl localhost:8088/ops/cache-stats
missing or wrong API key
$ curl -H "X-Api-Key: ops-key-2026" localhost:8088/ops/cache-stats
hits=42 misses=3
```

The app log (real lines; the pattern is `[cid=%X{cid}]`):

```
15:39:31.115 INFO  [cid=r-77] LedgerController - balance asked for ACC-1
15:39:31.116 INFO  [cid=r-77] AccessLogFilter - cid=r-77 GET /accounts/ACC-1/balance 200
15:39:31.130 INFO  [cid=da1761f0-2d43-461d-9f55-43ff15aabec2] AccessLogFilter - cid=da1761f0-... GET /ops/cache-stats 401
```

The controller never mentions the id; the filter put it in the MDC and every logger prints it.

## Where each one runs

```
request → [CorrelationIdFilter (order 1)] → [AccessLogFilter (order 2)] → DispatcherServlet
            → picks LedgerController#balance → [ApiKeyInterceptor] → [HandlerNameInterceptor] → controller
```

## Try it

```bash
mvn -pl chapters/14-filters-and-interceptors spring-boot:run
mvn -pl chapters/14-filters-and-interceptors test -Popen
```

On the stub, the log shows `[cid=]` everywhere and `/ops/cache-stats` answers anyone.

## Your turn

You edit `CorrelationIdFilter.java`, `ApiKeyInterceptor.java`, `HandlerNameInterceptor.java`, `WebConfig.java` and `interview-answer.md`.

1. **Correlation id filter.** Reuse or generate the id, reject a malformed one with 400 yourself, MDC + response header, clear in `finally`.
2. **API key interceptor.** 401 + `return false` on a wrong key; register for `/ops/**` only.
3. **Handler name interceptor.** `X-Handler: Class#method` when the handler is a `HandlerMethod`; register for every path.
4. **Register the filter.** A `FilterRegistrationBean` for `/*` with an order that runs before the access log.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- Throwing from a filter: the advice never sees it; you get the container's error page.
- No `finally` around `MDC.remove`: the next request on the same thread inherits the old id.
- `preHandle` returning `false` without setting a status: the client gets an empty 200.
- Interceptor registered without `addPathPatterns`: the key check covers every URL.
- Making a filter `@Component` AND registering it with `FilterRegistrationBean`: it runs twice (`OncePerRequestFilter` hides it, a plain `Filter` doesn't).

**Interview question (task 5).** Filter vs interceptor: where does each run, and which for a correlation id and for a per-endpoint check? How do you put an id on every log line, and why clear it? How do you order filters, and why can't the advice handle a filter's exception? The questions are repeated at the top of `interview-answer.md`.
