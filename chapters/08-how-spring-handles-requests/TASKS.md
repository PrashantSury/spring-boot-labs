# Chapter 8 · How Spring handles requests

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter08CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter08CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | The `DispatcherServlet` flow: handler mapping, adapter, argument resolvers, converters | not written yet | — | checks not written yet | ⬜ |
| 2 | How a handler method is chosen when several patterns match | not written yet | — | checks not written yet | ⬜ |
| 3 | Built-in answers: 400, 404, 405, 406, 415, 500, and which exception makes each | not written yet | — | checks not written yet | ⬜ |
| 4 | `@RequestMapping` options: `consumes`, `produces`, `params`, `headers` | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Walk a request for `/accounts/42` through the `DispatcherServlet`. When do you get 404, 405, 406 and 415?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- A more specific mapping beats a pattern: `/accounts/above` vs `/accounts/{number}`
- 405 means the path exists; 404 means it does not

## ❓ Checks (letters only)

1. `DELETE` on a path that only has `@GetMapping`: A. 404 · B. 405 · C. 500
2. `/accounts/above` and `/accounts/{number}` both match `/accounts/above`. Which runs? A. the first one declared · B. the more specific literal path · C. startup fails
3. `?balance=abc` for a `long balance` parameter: A. 400 · B. 500 · C. the method gets 0

<details><summary>📖 Teaching</summary>

**Why it matters.** A request for `/accounts/42` passes through many steps before your method runs; knowing them is how you debug a surprise 404 or 500.

**The concept.** Every request goes through the `DispatcherServlet`: a handler mapping picks the method, an adapter resolves the arguments, the method runs, a converter writes the result, and exception resolvers turn failures into 4xx/5xx.

**Small example.**

```java
@RestController
class AccountController {
    @GetMapping("/accounts/{number}") Account one(@PathVariable String number) { ... }
    @GetMapping("/accounts/above")    List<Account> above(@RequestParam long balance) { ... }
}
```

| Case | Result | In or out, and why |
|---|---|---|
| `GET /accounts/above?balance=100` | 200, `above()` runs | a literal path beats a `{pattern}` |
| `GET /accounts/above` | 400 | required `balance` missing (`MissingServletRequestParameterException`) |
| `GET /accounts/above?balance=abc` | 400 | `abc` is not a long (`MethodArgumentTypeMismatchException`) |
| `DELETE /accounts/0000123456` | 405 with an `Allow: GET` header | the path exists, the method doesn't |
| `GET /cards` | 404 | no handler matches the path |

**Gotchas**
- A more specific mapping beats a pattern: `/accounts/above` vs `/accounts/{number}`.
- 405 means the path exists; 404 means it does not.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** The path is known, the method is not.
2. **B.** Spring ranks matches by specificity.
3. **A.** The type conversion fails before your method runs.

</details>
