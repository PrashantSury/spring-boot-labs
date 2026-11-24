# Chapter 10: Layers and DTOs

By the end of this chapter you can split a feature into controller, service and repository with one job each, send clients a DTO instead of the stored object, and test business rules with plain Java.

## Why you need this

Ledgerly's first `CustomerController` did everything: it checked the PAN with a regex, called the repository, and returned the stored `Customer`. A penetration tester opened `GET /customers/101` and read `"pin":"4821"` and `"internalNotes":"VIP; called about loan"`. And nobody could test the PAN rule without starting a web server.

## What you will learn

- **Three layers:** controller (HTTP only), service (rules), repository (storage only).
- **DTOs as records:** `CustomerResponse(id, name, maskedPan)`, nothing more.
- **Mapping by hand:** a small static mapper. A library (MapStruct) pays off only when there are dozens of DTOs.
- **Testing:** rules in the service are tested with `new CustomerService(new CustomerRepository())`, no MockMvc.

## Who does what

```
HTTP  ──▶  CustomerController   reads JSON, calls the service, picks 201/400/404/409
            │  (takes CustomerService, never the repository)
            ▼
          CustomerService       PAN format, duplicate check, pin rule, summary sums
            │
            ▼
          CustomerRepository    save / findById / existsByPan; no rules
```

## Real output

The finished app on port 8084:

```
$ curl localhost:8084/customers/101
HTTP/1.1 200
{"id":101,"name":"Asha Rao","maskedPan":"XXXXXX234F"}

$ curl -X POST -H "Content-Type: application/json" -d '{"name":" Ravi Kumar ","pan":"bxkpk4321l","pin":"1234"}' localhost:8084/customers
HTTP/1.1 201
Location: /customers/102
{"id":102,"name":"Ravi Kumar","maskedPan":"XXXXXX321L"}

$ curl localhost:8084/customers/101/summary
HTTP/1.1 200
{"id":101,"transactions":3,"totalIn":50000,"totalOut":2000}

$ curl -X POST -H "Content-Type: application/json" -d '{"name":"R","pan":"ABCPR1234F","pin":"1234"}' localhost:8084/customers
HTTP/1.1 409
```

The stored `Customer` has `pin` and `internalNotes`. Neither ever appears. On the stub every endpoint answers 501.

## Try it

```bash
mvn -pl chapters/10-layers-and-dtos spring-boot:run
curl.exe localhost:8084/customers/101
mvn -pl chapters/10-layers-and-dtos test -Popen
```

## Your turn

You edit `web/CustomerResponse.java`, `service/CustomerMapper.java`, `service/CustomerService.java`, `web/CustomerController.java` and `interview-answer.md`.

1. **DTO + mapper.** `CustomerResponse(id, name, maskedPan)`; `mask("ABCPR1234F")` → `XXXXXX234F`.
2. **Rules in the service.** Trim name, upper-case and check the PAN, refuse duplicates, 4-digit pin.
3. **Thin controller.** Constructor takes `CustomerService`; 201/400/404/409.
4. **Summary.** Count, total in, total out (positive) computed in the service.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- Don't return `Customer` from the controller "just for now". The leak check reads the JSON.
- Task 2: compare the upper-cased PAN, or `abcpr1234f` slips past the duplicate check.
- Task 3: the controller must not take `CustomerRepository`, even "only for find".
- Task 4: `totalOut` is positive (2000), not -2000.

**Interview question (task 5).** Each layer's one job? Why a DTO instead of the entity, with a Ledgerly example? Why does a rule like the PAN check belong in the service, and how does that change your tests? The questions are repeated at the top of `interview-answer.md`.
