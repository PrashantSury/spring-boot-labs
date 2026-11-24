# Chapter 9 · Requests and responses

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter09CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter09CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@RequestBody`, `@PathVariable`, `@RequestParam`, `@RequestHeader` | not written yet | — | checks not written yet | ⬜ |
| 2 | `ResponseEntity`, status codes 200/201/204 and the `Location` header | not written yet | — | checks not written yet | ⬜ |
| 3 | Content negotiation: `Accept` and `Content-Type`, and 415/406 answers | not written yet | — | checks not written yet | ⬜ |
| 4 | Jackson basics: field names, dates, ignoring unknown fields | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- When do you return 200, 201 and 204? When is `ResponseEntity` worth it over `@ResponseStatus`?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- A POST with the wrong `Content-Type` answers 415 before your method runs
- A body that is not valid JSON answers 400 with a parse message, not 500

## ❓ Checks (letters only)

1. A new customer was created. Best answer: A. 200 + body · B. 201 + `Location` header · C. 204
2. JSON endpoint, client sends `text/plain`: A. 400 · B. 415 · C. 500
3. A successful DELETE with no body: A. 200 · B. 204 · C. 404

<details><summary>📖 Teaching</summary>

**Why it matters.** The mobile app will POST a new customer as JSON and expect a proper status and a `Location` header back.

**The concept.** `@RequestBody` turns JSON into an object and the return value turns back into JSON. `ResponseEntity` lets you choose the status and headers, like 201 with a `Location`.

**Small example.**

```java
@PostMapping("/customers")
ResponseEntity<CustomerView> create(@RequestBody NewCustomer body) {
    CustomerView saved = service.create(body);          // id 7
    URI location = URI.create("/customers/" + saved.id());
    return ResponseEntity.created(location).body(saved);
}
```

| Case | Result | In or out, and why |
|---|---|---|
| Valid JSON, `Content-Type: application/json` | `201`, `Location: /customers/7`, body `{"id":7,"name":"Asha Rao"}` | the happy path |
| `Content-Type: text/plain` | 415, the method never runs | no converter reads text into `NewCustomer` |
| Body `{"name":` | 400 with a parse message | not valid JSON; still not a 500 |
| `Accept: application/xml` | 406 | no converter can write XML |
| Delete that succeeds: `ResponseEntity.noContent().build()` | 204, no body | nothing to return |

**Gotchas**
- A POST with the wrong `Content-Type` answers 415 before your method runs.
- Invalid JSON gives 400 with a parse message, not 500.
- `ResponseEntity` decides the status at runtime; `@ResponseStatus` fixes one status on the method or exception.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** 201 says something new exists and where.
2. **B.** Unsupported media type, decided before the method.
3. **B.** No content to return.

</details>
