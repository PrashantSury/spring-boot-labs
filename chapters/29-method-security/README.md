# Chapter 29: Method security

By the end of this chapter Ledgerly's rules live on the service methods themselves: only an admin may close an account, a customer sees only their own accounts, and a transfer above your limit is refused, no matter whether the call came from a controller, a batch job or a test.

## Why you need this

URL rules guard the front door. But `AccountService.find(1)` is also called by the statement job and the support tool, which have no URL. And "your own account" cannot be written as a URL pattern at all, because the owner is inside the data. Method security puts the check where the data is.

## What you will learn

- **`@EnableMethodSecurity`**, **`@PreAuthorize`** and **`@PostAuthorize`**.
- **SpEL ownership rules:** `#owner == authentication.name`, `returnObject.owner()`.
- **A custom permission bean:** `@transferLimits.allowed(authentication, #amount)`.
- **Testing** with `@WithMockUser`, directly and through MockMvc.
- **The proxy trap:** `this.close(...)` skips the check.

## Real output

The finished app on port 8094 (HTTP Basic; password = name + `-pass`):

```
$ curl -u meera:meera-pass localhost:8094/accounts/2
{"id":2,"owner":"meera","balance":1500.00}
$ curl -u meera:meera-pass localhost:8094/accounts/1
{"status":403,"error":"Not allowed"}
$ curl -u ravi:ravi-pass localhost:8094/customers/asha/accounts
[{"id":1,"owner":"asha","balance":82000.00}]
$ curl -u asha:asha-pass -X POST "localhost:8094/transfers?from=1&to=2&amount=60000"
{"status":403,"error":"Not allowed"}
$ curl -u ravi:ravi-pass -X POST "localhost:8094/transfers?from=1&to=2&amount=60000"
moved 60000 from 1 to 2
$ curl -u asha:asha-pass -X DELETE localhost:8094/accounts/2
{"status":403,"error":"Not allowed"}
```

## The shape of it

```java
@EnableMethodSecurity   // on a @Configuration

@PreAuthorize("#owner == authentication.name or hasRole('TELLER')")
public List<Account> accountsOf(String owner) { ... }

@PostAuthorize("returnObject.owner() == authentication.name or hasRole('TELLER')")
public Account find(long id) { ... }
```

## Try it

```bash
mvn -pl chapters/29-method-security spring-boot:run
mvn -pl chapters/29-method-security test -Popen
```

On the stub every logged-in user can do everything.

## Your turn

You edit `SecurityConfig.java` (one annotation), `AccountService.java` (annotations only), `TransferLimits.java` and `interview-answer.md`.

1. **Enable + admin only.** `@EnableMethodSecurity`; `close` needs `hasRole('ADMIN')`.
2. **Ownership before.** `accountsOf(owner)`: own name or TELLER.
3. **Ownership after.** `find(id)`: `@PostAuthorize` on `returnObject.owner()`.
4. **Custom bean.** `TransferLimits.allowed` (USER ≤ 50,000; TELLER/ADMIN ≤ 5,00,000; positive) and the `@PreAuthorize` that calls it.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- Forgetting `@EnableMethodSecurity`: the annotations compile and do nothing.
- Self-invocation: `closeEmptyAccounts()` calls `this.close(...)`, which bypasses the proxy (the example test proves it).
- `@PostAuthorize` runs after the method: never use it on something that changes data.
- Annotations on a private method are ignored; the proxy only sees public calls from outside.
- `#owner` needs parameter names (`-parameters`, which the Boot parent sets).

**Interview question (task 5).** How does method security work; what do `@PreAuthorize` and `@PostAuthorize` do? How do you write an ownership rule or custom check, and test it? Why can a check be skipped? The questions are repeated at the top of `interview-answer.md`.
