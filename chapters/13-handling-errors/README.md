# Chapter 13: Handling errors

By the end of this chapter every failure in the Ledgerly API answers with the same JSON error body (RFC 9457 problem details), with the right status, and without leaking a stack trace.

## Why you need this

Ledgerly's mobile app shows "Something went wrong" for everything, because the API answers an unknown account with a 500, a short balance with a 500 and a bad amount with a 400 whose body lists nothing. Support cannot tell the cases apart, and one 500 even showed `NullPointerException` to a customer. One `@RestControllerAdvice` fixes all of it.

## What you will learn

- **`@RestControllerAdvice` + `@ExceptionHandler`:** one class turns exceptions into responses for every controller.
- **`ProblemDetail`:** the standard error body: `type`, `title`, `status`, `detail`, `instance`, plus your own properties.
- **Status choice:** 404 for an unknown account, 422 for a valid request the business refuses, 400 for bad input, 500 for bugs.
- **`ResponseEntityExceptionHandler`:** the base class that keeps Spring's own 400/405/415 correct, and the hook to reshape validation errors.
- **Handler choice:** the closest exception type wins; across several advices `@Order` decides.

## Real output

The finished app on port 8087:

```
$ curl localhost:8087/accounts/ACC-9/balance
{"detail":"No account ACC-9","instance":"/accounts/ACC-9/balance","status":404,"title":"Account not found","type":"https://ledgerly.dev/errors/account-not-found"}

$ curl -X POST -H Content-Type:application/json -d '{"from":"ACC-1","to":"ACC-2","amount":60000}' localhost:8087/transfers
{"detail":"Account ACC-1 is short by 8000","instance":"/transfers","status":422,"title":"Insufficient funds","shortBy":8000}

$ curl -X POST -H Content-Type:application/json -d '{"from":"ACC-1","to":"","amount":0}' localhost:8087/transfers
{"instance":"/transfers","status":400,"title":"Invalid request","errors":{"amount":"must be greater than 0","to":"must not be blank"}}

$ curl localhost:8087/accounts/ACC-1/statement
{"detail":"Something went wrong. Quote this to support.","instance":"/accounts/ACC-1/statement","status":500,"title":"Internal error"}

$ curl -X DELETE localhost:8087/transfers
{"detail":"Method 'DELETE' is not supported.","instance":"/transfers","status":405,"title":"Method Not Allowed"}
```

The last one is the point of task 3: the catch-all exists, and a wrong method is still 405.

## The shape of the solution

```java
@RestControllerAdvice
public class ApiErrors extends ResponseEntityExceptionHandler {

  @ExceptionHandler(AccountNotFoundException.class)
  public ProblemDetail notFound(AccountNotFoundException e) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    pd.setTitle("Account not found");
    return pd;                       // Spring writes it as application/problem+json
  }
}
```

## Try it

```bash
mvn -pl chapters/13-handling-errors spring-boot:run
mvn -pl chapters/13-handling-errors test -Popen
```

On the stub, `ACC-9` and the ₹60,000 transfer both come back as Spring Boot's default 500 page.

## Your turn

You edit `ApiErrors.java` and `interview-answer.md`. The controller has no try/catch on purpose; leave it alone.

1. **404.** `AccountNotFoundException` → `ProblemDetail` 404, title `Account not found`, the `type` URI from the comment.
2. **422.** `InsufficientFundsException` → 422, title `Insufficient funds`, property `shortBy`.
3. **Validation as ProblemDetail.** Extend `ResponseEntityExceptionHandler`; override `handleMethodArgumentNotValid` → 400, title `Invalid request`, `errors` map.
4. **Safety net.** `Exception` → 500, title `Internal error`, a fixed detail, nothing from the exception.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- A catch-all `@ExceptionHandler(Exception.class)` in a class that does NOT extend the base class turns 405 and 415 into 500.
- Adding your own `@ExceptionHandler(MethodArgumentNotValidException.class)` next to the base class: two handlers for one exception, the app fails to start. Override the method instead.
- `e.getMessage()` in a 500 body leaks internals. Only domain exceptions you wrote may show their message.
- 400 vs 422: 400 = the request is malformed or breaks a field rule; 422 = well-formed, but the business refuses it.

**Interview question (task 5).** How do you give every failure one error body, and which status for an unknown account and for insufficient funds? Why is a catch-all dangerous, and how do you keep Spring's 400/405/415? Which handler runs when two match? The questions are repeated at the top of `interview-answer.md`.
