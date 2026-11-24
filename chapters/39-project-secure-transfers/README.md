# Chapter 39: Project: secure transfers

By the end of this chapter the chapter 38 transfers service has logins and roles, and every request is checked against one rule: a customer only ever moves or sees their own money. Tellers can help any customer, and auditors can read everything but never move money.

> **Spoiler note.** This chapter starts from a *finished* chapter 38 (transfer logic, validation, idempotency). Do chapter 38 first if you want to solve it yourself.

## Why you need this

The penetration testers' report on the pre-release API had one critical finding. They logged in as customer Ravi, sent `{"from":"ACC-1",...}` with Asha's account number, and the money moved. The API checked *that* Ravi was logged in, never *whose* account it was. This bug has a name, IDOR (insecure direct object reference), and it is the most common serious API vulnerability.

## What you will learn

- **JWT login on top of a service:** `/auth/login` issues a token with the user's roles; a stateless filter reads it (chapters 26–30, finished here).
- **Three roles:** CUSTOMER, TELLER, AUDITOR, and what each may do.
- **URL rules vs method security:** coarse areas in `SecurityFilterChain`; "whose account?" in `@PreAuthorize`.
- **Ownership checks** in a bean used from SpEL: `@ownership.owns(authentication, #request.from())`.
- **`@PostAuthorize`** when the rule needs the loaded object (`returnObject`).
- **Security tests** that try to break in: no token, a tampered token, the wrong role, someone else's account.

## Who may do what

| | asha / ravi / meera (CUSTOMER) | tara (TELLER) | audit (AUDITOR) |
|---|---|---|---|
| `GET /accounts/{n}` | own accounts only | any | any |
| `POST /transfers` | from own accounts only | from any account | never |
| `GET /transfers/{id}` | if sender or receiver | any | any |
| `GET /audit/transfers` | never | never | yes |

Passwords are the name + `-pass`. Accounts: asha ACC-1 (1000), ravi ACC-2 (500) and ACC-3 (0, frozen), meera ACC-4 (500).

## Real output

The finished app on port 8100 (each user logged in first with `POST /auth/login`; `[403]` is the HTTP status):

```
$ curl localhost:8100/accounts/ACC-1   (no token)
{"title":"Unauthorized","status":401,"detail":"Login required","instance":"/accounts/ACC-1"} [401]
$ (ravi) POST /transfers from ACC-1
{"title":"Forbidden","status":403,"detail":"You may not do this","instance":"/transfers"} [403]
$ (asha) POST /transfers from ACC-1
{"id":1,"from":"ACC-1","to":"ACC-2","amount":100.00} [201]
$ (ravi) GET /transfers/1
{"id":1,"from":"ACC-1","to":"ACC-2","amount":100.00} [200]
$ (meera) GET /transfers/1
{"title":"Forbidden","status":403,"detail":"You may not do this","instance":"/transfers/1"} [403]
$ (asha) GET /audit/transfers
{"title":"Forbidden","status":403,"detail":"You may not do this","instance":"/audit/transfers"} [403]
$ (audit) GET /audit/transfers
[{"id":1,"from":"ACC-1","to":"ACC-2","amount":100.00}] [200]
```

On the stub, any logged-in user can do everything: Ravi's transfer from ACC-1 is a 201.

## The shape of it

```java
@PreAuthorize("hasRole('TELLER') or (hasRole('CUSTOMER') and @ownership.owns(authentication, #request.from()))")
public TransferResult transfer(String key, TransferRequest request) { ... }

@PostAuthorize("hasAnyRole('TELLER','AUDITOR') or @ownership.involved(authentication, returnObject)")
public TransferDto find(long id) { ... }
```

## Try it

```bash
mvn -pl chapters/39-project-secure-transfers spring-boot:run
curl -s -X POST localhost:8100/auth/login -H Content-Type:application/json -d '{"username":"asha","password":"asha-pass"}'
mvn -pl chapters/39-project-secure-transfers test -Popen
```

H2 in memory; no Docker.

## Your turn

You edit `SecurityConfig.java`, `Ownership.java`, `TransferService.java`, `SecurityCasesTest.java` and `interview-answer.md`.

1. **URL rules.** `@EnableMethodSecurity`; `/audit/**` AUDITOR only; `POST /transfers` CUSTOMER or TELLER.
2. **Ownership for transfers.** `Ownership.owns` + `@PreAuthorize` on `transfer`.
3. **Ownership for reads.** `@PreAuthorize` on `account`, `Ownership.involved` + `@PostAuthorize` on `find`.
4. **Your own tests.** Replace the three `fail(...)` lines in `SecurityCasesTest.java` with real negative tests.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- "Logged in" is not "allowed": the account number in the body is chosen by the client.
- Answer 404 for an unknown account to a customer and you've told them which numbers exist; treat it as "not yours" (403).
- `@PreAuthorize` does nothing without `@EnableMethodSecurity`, and nothing for `this.` calls (chapter 34).
- `@PostAuthorize` runs *after* the method: never use it on a method that changes data.
- A test suite with only happy paths would have passed the pen-testers' attack.

**Interview question (task 5).** JWT login and roles; URL rules vs method security. Ownership and IDOR. Testing security; 401 vs 403. The questions are repeated at the top of `interview-answer.md`.
