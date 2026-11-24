# Chapter 36 · Actuator

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter36CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter36CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Health, info and metrics endpoints | not written yet | — | checks not written yet | ⬜ |
| 2 | Exposing endpoints safely | not written yet | — | checks not written yet | ⬜ |
| 3 | Custom `HealthIndicator` | not written yet | — | checks not written yet | ⬜ |
| 4 | Micrometer counters for transfers | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- What does `/actuator/health` UP really tell you? Which endpoints would you expose in production, and how?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Don't expose every endpoint to the public internet
- Health 'UP' only means the checks you configured passed

## ❓ Checks (letters only)

1. Exposed over HTTP by default: A. `health` · B. every endpoint
2. Health is `DOWN`. The HTTP status is: A. 200 · B. 503
3. "UP" proves: A. the whole system works · B. the configured health checks passed

<details><summary>📖 Teaching</summary>

**Why it matters.** Operations need to ask the running app if it is healthy, what version it is and how it is doing.

**The concept.** Actuator adds endpoints like `/actuator/health` and `/actuator/metrics` so operations can ask the running app how it is. Only `health` is exposed over HTTP by default.

**Small example.**

```java
@Component
class CoreBankingHealth implements HealthIndicator {
    public Health health() {
        return gateway.ping() ? Health.up().withDetail("gateway", "reachable").build()
                              : Health.down().withDetail("gateway", "timeout").build();
    }
}
// counter: meterRegistry.counter("ledgerly.transfers", "result", "ok").increment();
```

| Case | Result | In or out, and why |
|---|---|---|
| `GET /actuator/health`, gateway reachable | `{"status":"UP"}` | details hidden unless you show them |
| Gateway down | `{"status":"DOWN"}` with HTTP 503 | health groups map DOWN to 503 |
| `GET /actuator/env` with defaults | 404 | not exposed over HTTP by default |
| `management.endpoints.web.exposure.include=*` on a public port | config and heap dumps exposed ❌ | expose only what you need, behind auth |
| `GET /actuator/metrics/ledgerly.transfers` (if `metrics` is exposed) | a count by `result` tag | your Micrometer counter |

**Gotchas**
- Don't expose every endpoint to the public internet.
- Health 'UP' only means the checks you configured passed.

</details>

<details><summary>🔒 Answers</summary>

1. **A.** You opt in to the rest.
2. **B.** So load balancers can react.
3. **B.** Nothing more.

</details>
