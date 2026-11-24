# Chapter 12 · Validating input

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter12CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter12CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Bean Validation: `@NotBlank`, `@Positive`, `@Size`, `@Pattern`, `@Email` | not written yet | — | checks not written yet | ⬜ |
| 2 | `@Valid` on `@RequestBody` and the 400 it produces | not written yet | — | checks not written yet | ⬜ |
| 3 | Validating `@RequestParam` and `@PathVariable` with `@Validated` | not written yet | — | checks not written yet | ⬜ |
| 4 | Custom constraint annotation for an account number | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- What happens between a JSON body arriving and a 400 coming back? Which exception for a body, and which for a path variable?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- `@Valid` on a nested object does not cascade unless the field is also `@Valid`
- Validation on a path variable throws a different exception from a body

## ❓ Checks (letters only)

1. `@Positive amount`, body sends -500, `@Valid` present: A. the method runs · B. 400 before the method
2. A nested `Address address` field has constraints but no `@Valid`: A. they are checked · B. they are skipped
3. Body validation failure throws: A. `MethodArgumentNotValidException` · B. `NullPointerException` · C. nothing

<details><summary>📖 Teaching</summary>

**Why it matters.** A transfer of -500 rupees or to a blank account number must be refused with a clear message, before any money moves.

**The concept.** Put constraints like `@NotBlank` and `@Positive` on the request DTO, and `@Valid` on the parameter. Spring rejects bad input with a 400 before your method runs.

**Small example.**

```java
record TransferRequest(
    @NotBlank @Pattern(regexp = "\\d{10}") String toAccount,
    @Positive BigDecimal amount) {}

@PostMapping("/transfers")
ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest req) { ... }
```

| Case | Result | In or out, and why |
|---|---|---|
| `{"toAccount":"0000123456","amount":500}` | your method runs | both constraints pass |
| `"amount": -500` | 400, violation on `amount` | `@Positive` fails; no money moves |
| `"toAccount": ""` | 400, violations on `toAccount` | `@NotBlank` (and `@Pattern`) fail |
| Same DTO without `@Valid` on the parameter | your method runs with -500 ❌ | constraints are only checked when asked |
| `@Positive` on a `@PathVariable` | 400, but through a different exception | method validation, not `MethodArgumentNotValidException` |

**Gotchas**
- `@Valid` on a nested object does not cascade unless the field is also `@Valid`.
- Validation on a path variable throws a different exception from a body, so your error handler needs both.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Validation runs during argument resolution.
2. **B.** Cascading needs `@Valid` on the field.
3. **A.** That is the exception the 400 handler sees.

</details>
