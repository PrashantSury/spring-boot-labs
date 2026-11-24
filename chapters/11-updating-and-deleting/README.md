# Chapter 11: Updating and deleting

By the end of this chapter you can tell PUT from PATCH, say which methods are idempotent, make DELETE safe to repeat, and stop two people's updates from overwriting each other with `ETag` and `If-Match`.

## Why you need this

Asha and her husband share a Ledgerly login on two phones. He renames the payee "Landlord" to "Rent"; a second later her phone, still showing the old screen, saves the payee again and silently puts "Landlord" back. The same week, a flaky mobile network makes the app send `DELETE /payees/1` twice, and the second one shows the customer an error for something that worked.

## What you will learn

- **PUT vs PATCH:** PUT replaces the whole resource (missing fields become `null`); PATCH changes only what is sent.
- **Idempotency:** the same request twice leaves the same state. GET, PUT and DELETE are idempotent; POST is not.
- **DELETE answers:** 204 the first time, and (in this lab) 204 again.
- **Optimistic checks:** the version goes out as `ETag: "2"`; a client sends `If-Match: "2"` back; a stale one gets **412**.

## Real output

The finished app on port 8085. Payee 1 starts as Vikram Shah, nickname Landlord, version 1:

```
$ curl -X PATCH -H "Content-Type: application/json" -H 'If-Match: "1"' -d '{"nickname":"Rent"}' localhost:8085/payees/1
HTTP/1.1 200
ETag: "2"
{"id":1,"name":"Vikram Shah","accountNumber":"50100012345678","ifsc":"HDFC0001234","nickname":"Rent","version":2}

$ curl -X PATCH -H "Content-Type: application/json" -H 'If-Match: "1"' -d '{"nickname":"Old"}' localhost:8085/payees/1
HTTP/1.1 412

$ curl -X PUT -H "Content-Type: application/json" -d '{"name":"Vikram S","accountNumber":"50100012345678","ifsc":"HDFC0001234"}' localhost:8085/payees/1
HTTP/1.1 200
ETag: "3"
{"id":1,"name":"Vikram S","accountNumber":"50100012345678","ifsc":"HDFC0001234","nickname":null,"version":3}

$ curl -X DELETE localhost:8085/payees/1
HTTP/1.1 204
$ curl -X DELETE localhost:8085/payees/1
HTTP/1.1 204
```

The second PATCH was the "old screen": it still said version 1, so it was refused. The PUT left out `nickname`, so it became `null`.

## PATCH needs "absent" vs "null"

A record can't tell `{}` from `{"nickname":null}`: both give `nickname == null`. That's why `patch` takes a `Map<String, Object>`:

```java
changes.containsKey("nickname") ? (String) changes.get("nickname") : old.nickname()
```

## Try it

```bash
mvn -pl chapters/11-updating-and-deleting spring-boot:run
mvn -pl chapters/11-updating-and-deleting test -Popen
```

On the stub, PUT, PATCH and DELETE answer 501.

## Your turn

You edit `PayeeController.java`, `src/test/java/ledgerly/PayeeCrudTest.java` and `interview-answer.md`.

1. **PUT.** Replace the whole payee; 404 for unknown (no create); 400 if incomplete.
2. **PATCH.** Only sent keys change; `null` clears; `{}` changes nothing; `accountNumber` and unknown keys → 400.
3. **DELETE.** 204 every time.
4. **`If-Match`.** `stale()`, and 412 from PUT and PATCH.
5. **Your own tests.** At least 4 MockMvc tests in `PayeeCrudTest` (create/read, PUT, PATCH, DELETE twice).
6. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- PUT keeping the old nickname is a PATCH in disguise. The check wants `null`.
- `{}` must not bump the version: nothing changed.
- Check 404 first, then 412, then 400: a stale request is refused before its body is judged.
- `If-Match` values carry quotes: `"2"`, not `2`.

**Interview question (task 6).** PUT vs PATCH, and what happens to a field you leave out? What is idempotent, which methods are, and what should a second DELETE answer? How do you stop lost updates? The questions are repeated at the top of `interview-answer.md`.
