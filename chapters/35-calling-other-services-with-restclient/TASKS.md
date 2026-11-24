# Chapter 35 · Calling other services with RestClient

**Time:** ~50 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter35CheckTest` 15/15 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/35-calling-other-services-with-restclient test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/35-calling-other-services-with-restclient spring-boot:run` (fake partner on port 8097, every case printed).
- Fresh stub: 1/15 green (the `MockRestServiceServer` example; must stay green). No Docker needed.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `screen`: GET + map to `ScreenResult` | `src/main/java/ledgerly/SanctionsClient.java` | `mvn -pl chapters/35-calling-other-services-with-restclient test -Popen` | task 1 checks green | ⬜ |
| 2 | `onStatus`: 400 / 5xx → own exceptions | `SanctionsClient.java` | same | task 2 checks green | ⬜ |
| 3 | timeouts (2 s) + timeout → `SanctionsUnavailable` | `src/main/resources/application.properties`, `SanctionsClient.java` | same | task 3 checks green | ⬜ |
| 4 | retry GET ×3; `report` with `Idempotency-Key`, no retry | `SanctionsClient.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** RestClient, timeouts, retries, testing | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Calling a service; mapping its errors.
- Timeouts; what is safe to retry?
- Testing without the partner.

## ⚠️ Traps

- No timeout = a slow partner takes your threads.
- Never retry a 400 or a money POST.
- Use `{name}` URI variables, not string gluing.

## ❓ Checks (letters only)

1. The partner answers 503 to a GET `/screen`. Reasonable: A. retry a few times · B. never retry
2. A POST `/reports` times out. Should Ledgerly resend it straight away without a key? A. yes · B. no, it may already have arrived
3. Which test tool fakes the partner without opening a port? A. `MockRestServiceServer` · B. `FakeSanctions`

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** Every real service calls others, and interviewers love the follow-ups: "what if it's slow?", "what if it fails?", "can you retry?". The safe answers are timeouts, mapped errors, careful retries and idempotency keys.

**The concept.** Calling a partner is like phoning another bank's branch. You hang up if nobody picks up within 2 s (connect timeout) or if they go silent for 2 s (read timeout). "Your request makes no sense" (400) means fix it, don't call again. "We're busy, call back" (503) means a couple more tries are fine *for a question*. For an instruction ("file this report") a lost answer means you don't know if they did it, so you quote a reference number (the idempotency key) and let them spot a repeat.

**Small example.** Real results from the tests:

| Call | Partner sees | Result | Why |
|---|---|---|---|
| `screen("Acme Traders")` | 1 GET → 200 | `blocked=false` | happy path |
| `screen("Evil Corp")` | 1 GET → 200 | `blocked=true` | on the list |
| `screen(" ")` | 1 GET → 400 | `PayeeRejected` | not retried |
| two 503s, then `screen` | 3 GETs | `blocked=false` | retry worked |
| 503 forever, then `screen` | 3 GETs | `SanctionsUnavailable: partner answered 503` | gave up |
| `screen("Slow Co")` (answers after 4 s) | 1 GET | `partner timed out` after ~2 s | read timeout, no retry |
| `report("TX-7", …)` | `POST /reports key=TX-7 → 202` | ok | key sent |
| 503, then `report("TX-8", …)` | 1 POST | `SanctionsUnavailable` | POST never retried |

**Gotchas**
- `retrieve()` throws `HttpClientErrorException` / `HttpServerErrorException` by default; `onStatus` replaces that for the statuses you match.
- `MockRestServiceServer` replaces the request factory, so it can't test real timeouts; use a real local server for that.
- Spring Framework 7 also has `@Retryable` with `@EnableResilientMethods`; the rules for *what* to retry stay the same.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **A.** A GET is safe to repeat; cap the attempts.
2. **B.** The partner may have filed it; resend only with the same idempotency key.
3. **A.** `FakeSanctions` is a real server on a port.

</details>
