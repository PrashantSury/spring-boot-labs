# Chapter 12: Validating input

By the end of this chapter you can put rules on a request record, make Spring enforce them with `@Valid`, validate nested objects, path variables and query parameters, and write your own constraint for a Ledgerly account number.

## Why you need this

A partner bank's integration sends Ledgerly transfers with `"amount": 0`, remarks of 70 characters that break the SMS template, and IFSC codes like `HDFC1234`. Each one reaches the service, fails somewhere deep inside, and comes back as a 500. Bad input should be stopped at the door with a 400 that names the field.

## What you will learn

- **Bean Validation:** `@NotBlank`, `@Positive`, `@Max`, `@Size`, `@Email`, `@Pattern`, `@NotNull` on record components.
- **`@Valid` on `@RequestBody`:** turns the rules on; a broken body → 400 `MethodArgumentNotValidException`, every bad field listed.
- **Cascading:** rules inside `Beneficiary` run only if the field is also `@Valid`.
- **Parameters:** constraints on `@PathVariable`/`@RequestParam` → 400 `HandlerMethodValidationException`.
- **A custom constraint:** `@AccountNumber` + `AccountNumberValidator`.

## Real output

The finished app on port 8086:

```
$ curl -X POST -H "Content-Type: application/json" -d '{"from":"ACC-1","amount":5000,"beneficiary":{"name":"Vikram Shah","ifsc":"HDFC0001234"}}' localhost:8086/transfers
sent 5000 from ACC-1 to Vikram Shah

$ curl -X POST -H "Content-Type: application/json" -d '{"from":"SAV-9","amount":0,"beneficiary":{"name":"Vikram Shah","ifsc":"HDFC1234"}}' localhost:8086/transfers
{"timestamp":"2026-11-24T10:03:01.832Z","status":400,"error":"Bad Request","path":"/transfers"}

$ curl "localhost:8086/accounts/ACC-1/statement?months=13"
{"timestamp":"2026-11-24T10:03:01.859Z","status":400,"error":"Bad Request","path":"/accounts/ACC-1/statement"}
```

The app log says why (real line, cut):

```
Resolved [org.springframework.web.bind.MethodArgumentNotValidException: Validation failed for argument [0] in public ... ledgerly.TransferController.transfer(ledgerly.TransferRequest) with 3 errors: [Field error in object 'transferRequest' on field 'from': rejected value [SAV-9]; ...
```

Three errors in one go: `from`, `amount` and `beneficiary.ifsc`. Chapter 13 turns them into a JSON body the client can read.

## The rules on the record

```java
public record TransferRequest(
    @NotBlank @AccountNumber String from,
    @Positive @Max(200_000) long amount,
    @Size(max = 50) String remark,
    @Email String notifyEmail,
    @NotNull @Valid Beneficiary beneficiary) {}     // @Valid: also check the rules INSIDE Beneficiary
```

## Try it

```bash
mvn -pl chapters/12-validating-input spring-boot:run
mvn -pl chapters/12-validating-input test -Popen
```

On the stub, `amount: 0` is happily "sent", and a missing beneficiary is a 500.

## Your turn

You edit `TransferRequest.java`, `Beneficiary.java`, `TransferController.java`, `AccountNumberValidator.java` and `interview-answer.md`.

1. **Rules + `@Valid`.** Constraints on `TransferRequest`, `@Valid` on the body.

3. **Nested.** `Beneficiary` rules, and `@NotNull @Valid` on the field.
4. **Custom constraint.** `AccountNumberValidator.isValid`: `ACC-` + 1-8 digits; `null` is valid.
5. **Parameters.** `@AccountNumber` on the path variable, `@Min(1) @Max(12)` on `months`.
6. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- Rules without `@Valid` on the parameter do nothing at all.
- `@NotBlank` on a `long` is an error; numbers use `@Positive`/`@Min`/`@Max`.
- No `@Valid` on `beneficiary` → a bad IFSC passes.
- `@Validated` on the controller class switches to a different mechanism (AOP) that throws `ConstraintViolationException`, a 500 by default. Don't add it.
- The custom validator returns `true` for `null`.

**Interview question (task 6).** How do you make Spring reject a bad body, and what status and exception? Why don't nested rules run by default? How do you validate a path variable, and why does a custom validator return true for null? The questions are repeated at the top of `interview-answer.md`.
