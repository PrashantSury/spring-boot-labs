# Chapter 12 · Validating input

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter12CheckTest` 20/20 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/12-validating-input test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/12-validating-input spring-boot:run` (port 8086).
- Fresh stub: 4/20 green (valid requests and `null`, which must stay green). There is no task 2.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Constraints on `TransferRequest` + `@Valid @RequestBody` | `src/main/java/ledgerly/TransferRequest.java`, `TransferController.java` | `mvn -pl chapters/12-validating-input test -Popen` | task 1 checks green | ⬜ |
| 3 | `Beneficiary` rules + `@NotNull @Valid` cascade | `Beneficiary.java`, `TransferRequest.java` | same | task 3 checks green | ⬜ |
| 4 | `AccountNumberValidator`: `ACC-` + 1-8 digits, null valid | `AccountNumberValidator.java` | same | task 4 checks green | ⬜ |
| 5 | `@AccountNumber` path variable, `@Min(1) @Max(12)` months | `TransferController.java` | same | task 5 checks green | ⬜ |
| 6 | **(Interview)** body vs parameter validation, cascade, null | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 6)

- Reject a bad body: how, which status, which exception?
- Why don't nested rules run by default, and the fix?
- Validate a path variable; why does a custom validator return true for null?

## ⚠️ Traps

- No `@Valid` → no validation.
- Numbers: `@Positive`, not `@NotBlank`.
- No `@Validated` on the controller class.

## ❓ Checks (letters only)

1. `@Positive long amount` but the parameter has no `@Valid`. `amount: 0` gives: A. 400 · B. the method runs with 0
2. `@Max(200_000)` and `amount: 200000`: A. valid · B. 400
3. A body error and a `@RequestParam` error throw: A. the same exception · B. `MethodArgumentNotValidException` vs `HandlerMethodValidationException`

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** Input validation is the first security control in a banking API and a standard interview topic ("how do you validate a request in Spring?"). A wrong answer here usually means 500s in production.

**The concept.** Constraints describe the rules; `@Valid` (or constraints on parameters) asks Spring to check them before the method runs. Every failure becomes a field error, and the request is answered with 400.

**Small example.** Sample body: `{"from":"SAV-9","amount":0,"remark":null,"beneficiary":{"name":"Vikram Shah","ifsc":"HDFC1234"}}`

Real result: `400`, `MethodArgumentNotValidException ... with 3 errors`.

| Field | Value | Error? | Why |
|---|---|---|---|
| `from` | `SAV-9` | yes | `@AccountNumber` wants `ACC-` + digits |
| `amount` | `0` | yes | `@Positive` excludes 0 |
| `remark` | `null` | no | `@Size` ignores null |
| `beneficiary.ifsc` | `HDFC1234` | yes | the 5th character must be `0`, and 11 in total; checked because of `@Valid` |
| `beneficiary.name` | `Vikram Shah` | no | not blank |

**Gotchas**
- Almost every constraint treats `null` as valid; add `@NotNull`/`@NotBlank` when a value is required.
- `@Size` on a `String` counts characters; on a `List` it counts elements.
- Validation runs **after** JSON parsing: `"amount":"abc"` is a 400 from Jackson, not from `@Positive`.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** The rules are ignored without `@Valid`.
2. **A.** `@Max` is inclusive.
3. **B.** Two different exceptions; chapter 13 handles both.

</details>
