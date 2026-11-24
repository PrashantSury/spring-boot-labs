# Chapter 10 · Layers and DTOs

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter10CheckTest` 17/17 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/10-layers-and-dtos test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/10-layers-and-dtos spring-boot:run` (port 8084).
- Fresh stub: 0/17 green.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `CustomerResponse(id, name, maskedPan)` + `mask` + `toResponse` | `src/main/java/ledgerly/web/CustomerResponse.java`, `service/CustomerMapper.java` | `mvn -pl chapters/10-layers-and-dtos test -Popen` | task 1 checks green | ⬜ |
| 2 | `open()` rules: name, PAN, duplicate, pin | `service/CustomerService.java` | same | task 2 checks green | ⬜ |
| 3 | Thin controller on `CustomerService`: 201/400/404/409 | `web/CustomerController.java` | same | task 3 checks green | ⬜ |
| 4 | `summary()` in the service + endpoint | `CustomerService.java`, `CustomerController.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** layers and DTOs | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Controller, service, repository: one job each?
- Why a DTO and not the entity? A Ledgerly example of the leak.
- Why the PAN rule in the service, and what it does to your tests?

## ⚠️ Traps

- Never return `Customer`.
- Upper-case the PAN **before** the duplicate check.
- Controller takes the service only.

## ❓ Checks (letters only)

1. A new field `kycScore` is added to `Customer`. With a DTO, the JSON: A. gains `kycScore` · B. stays the same
2. The best place for "PAN must match `[A-Z]{5}[0-9]{4}[A-Z]`": A. controller · B. service · C. repository
3. Testing the duplicate-PAN rule with the rule in the service needs: A. MockMvc · B. a running server · C. plain `new CustomerService(...)`

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "Why DTOs?" and "where do business rules go?" come up in every backend interview, and leaking a field from an entity is a classic bank audit finding.

**The concept.** Each layer has one reason to change: HTTP shape (controller), rules (service), storage (repository). The DTO is the contract with clients; it changes only when the API changes, not when the table does.

**Small example.** Stored row: `Customer(101, "Asha Rao", "ABCPR1234F", "4821", "VIP; called about loan", [50000, -1200, -800])`. Mapping by hand:

```java
new CustomerResponse(c.id(), c.name(), mask(c.pan()))
```

Real output: `{"id":101,"name":"Asha Rao","maskedPan":"XXXXXX234F"}`

| Stored field | In the DTO? | Why |
|---|---|---|
| `id`, `name` | in | the app shows them |
| `pan` | masked | the app shows the last 4 only |
| `pin` | out | a secret |
| `internalNotes` | out | for staff only |
| `transactions` | out | separate `/summary` endpoint |

**Gotchas**
- A service method should not take or return `ResponseEntity`; that couples it to HTTP.
- Exceptions are the service's way to say "no" (`DuplicateCustomerException`); the controller turns them into statuses (chapter 13 does it centrally).
- Mapping by hand is 3 lines per DTO and easy to read; add a mapper library only when it hurts.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** The DTO decides the JSON, so a new stored field stays hidden.
2. **B.** It's a business rule.
3. **C.** Plain Java, which is fast and needs no Spring.

</details>
