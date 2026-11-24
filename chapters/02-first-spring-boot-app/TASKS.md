# Chapter 2 · Your first Spring Boot app

**Time:** ~75 min · **Status:** ✅ done (21 Nov, checks 19/19, interview 7/10) · **Done when:** `Chapter02CheckTest` 19/19 + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/02-first-spring-boot-app test` · app: `mvn -pl chapters/02-first-spring-boot-app spring-boot:run`. Don't change the `ACCOUNTS` list.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `GET /status` → 200, `Ledgerly is open` | `src/main/java/ledgerly/AccountController.java` | `mvn -pl chapters/02-first-spring-boot-app test` | task 1 checks green | ✅ |
| 2 | `GET /accounts` → JSON list of all four, in order | `AccountController.java` | same | task 2 checks green | ✅ |
| 3 | `GET /accounts/{number}/masked` → `******4567` | `AccountController.java` | same | task 3 checks green | ✅ |
| 4 | `GET /accounts/above?balance=N` → balance **more than** N | `AccountController.java` | same | task 4 checks green | ✅ |
| 5 | `GET /fee?amount=N&express=true` → 1%, at least 10, doubled if express | `AccountController.java` | same | task 5 checks green | ✅ |
| 6 | **(Interview)** make `LedgerlyApplication` a Spring Boot app | `src/main/java/ledgerly/LedgerlyApplication.java` | same | app starts + answer ≥ 7 | ✅ 7/10 |

## 🎤 Interview (task 6)

- What does `@SpringBootApplication` switch on? Name the three annotations inside it and what each does.
- What happens if the controller is in a package above the application class?

## ❓ Checks (letters only)

1. `GET /accounts/above` with no `balance`: A. your code must handle it · B. Spring answers 400 · C. 500
2. `/accounts/{number}` with `number` as `int` for `0000123456`: A. fine · B. loses the leading zeros
3. A controller in a package above the app class: A. found by scan · B. not scanned, so 404

<details><summary>📖 Teaching (recap)</summary>

**Why it matters.** Every later chapter assumes you can map a URL, read path and query values, and know what Spring answers for you.

**The concept.** `@SpringBootApplication` = `@SpringBootConfiguration` + `@EnableAutoConfiguration` + `@ComponentScan` (the class's package and below).

**Small example.** Fee for `amount=1500`:

| Request | Result | Why |
|---|---|---|
| `/fee?amount=1500` | 15 | 1% = 15, already ≥ 10 |
| `/fee?amount=500` | 10 | 1% = 5, raised to the minimum 10 |
| `/fee?amount=500&express=true` | 20 | the minimum first, then doubled |
| `/fee` | 400 | Spring: required parameter missing |

**Gotchas**
- "More than" is `>`, not `>=` (the checks include exactly 52000).
- Take the minimum of 10 **before** doubling for express.
- Scan covers only the app class's package and sub-packages; fix with `scanBasePackages` or move the class.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Missing required parameter → 400 from Spring.
2. **B.** Keep account numbers as `String`.
3. **B.** Outside component scan, so no handler → 404.

</details>
