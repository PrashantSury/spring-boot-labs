# Chapter 35 · Calling other services with RestClient

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter35CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter35CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `RestClient` basics: get, post, retrieve | not written yet | — | checks not written yet | ⬜ |
| 2 | Timeouts, error statuses and `onStatus` | not written yet | — | checks not written yet | ⬜ |
| 3 | Testing with `MockRestServiceServer` | not written yet | — | checks not written yet | ⬜ |
| 4 | A simple retry and what not to retry | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- How do you call an external sanctions service safely: timeouts, error mapping, retries, and what you never retry blindly?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- No timeout means a slow partner hangs your threads
- Retrying a POST can double a payment; use an idempotency key

## ❓ Checks (letters only)

1. A partner never answers and no timeout is set: A. `RestClient` gives up after 1 s · B. the calling thread can wait a very long time
2. Safe to retry automatically: A. a GET screening lookup · B. a POST that moves money, without an idempotency key
3. `MockRestServiceServer` is for: A. load testing · B. faking the partner in tests

<details><summary>📖 Teaching</summary>

**Why it matters.** Ledgerly checks a payee against an external sanctions service before a transfer.

**The concept.** `RestClient` calls another service over HTTP with a fluent API. Always set timeouts and decide what each error status means for you.

**Small example.**

```java
record Screening(String payee, boolean blocked) {}

Screening s = restClient.get()
        .uri("/screen?name={n}", "Ravi Kumar")
        .retrieve()
        .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> { throw new PayeeRejectedException(); })
        .body(Screening.class);
```

| Case | Result | In or out, and why |
|---|---|---|
| Partner answers 200 `{"payee":"Ravi Kumar","blocked":false}` | `s.blocked()` = false | the happy path |
| Partner answers 404 | `PayeeRejectedException` | your `onStatus` mapping |
| Partner answers 503, no `onStatus` for 5xx | `HttpServerErrorException` | the default for 5xx |
| Partner hangs, no timeout set | your request threads pile up ❌ | always set connect and read timeouts |
| Test with `MockRestServiceServer` expecting `/screen?name=Ravi%20Kumar` | no real network call | a fast, repeatable test |

**Gotchas**
- No timeout means a slow partner hangs your threads.
- Retrying a POST can double a payment; use an idempotency key.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Set timeouts explicitly.
2. **A.** Idempotent reads are safe; payments are not.
3. **B.** It intercepts the client's requests.

</details>
