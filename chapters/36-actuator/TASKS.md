# Chapter 36 · Actuator

**Time:** ~45 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter36CheckTest` 12/12 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/36-actuator test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/36-actuator spring-boot:run` (port 8098; `POST /demo/gateway?up=false` takes the gateway down).
- Fresh stub: 2/12 green: the example and "liveness stays UP when the gateway is down". Both must stay green.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | expose `health,info,metrics` only; details; app info | `src/main/resources/application.properties` | `mvn -pl chapters/36-actuator test -Popen` | task 1 checks green | ⬜ |
| 2 | `HealthIndicator` for the payments gateway | `src/main/java/ledgerly/PaymentsGatewayHealthIndicator.java` | same | task 2 checks green | ⬜ |
| 3 | `ledgerly.transfers` counter, tag `outcome` | `TransferService.java` | same | task 3 checks green | ⬜ |
| 4 | gateway in the readiness group only | `application.properties` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** Actuator, probes, metrics | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- What Actuator gives; safe exposure?
- Custom health; liveness vs readiness.
- Business metrics with Micrometer.

## ⚠️ Traps

- Never expose `*`.
- Dependencies go in readiness, not liveness.
- No ids as metric tags.

## ❓ Checks (letters only)

1. The payments gateway is down. Kubernetes should: A. restart Ledgerly's pods · B. stop sending them traffic until it's back
2. Which endpoint is safe to expose publicly? A. `/actuator/health` (status only) · B. `/actuator/env`
3. Counting transfers by result: A. one counter `ledgerly.transfers` with tag `outcome` · B. a counter per account id

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "How do you monitor a Spring Boot app in production?" and "liveness vs readiness?" come up in almost every backend interview that mentions Kubernetes.

**The concept.** Actuator is the app's dashboard. Health is the warning-light panel: each indicator is a bulb, and the overall status is DOWN if any bulb is. Kubernetes asks two different questions. **Liveness:** "are you stuck, should I restart you?" **Readiness:** "should I send you customers right now?" A broken partner is a readiness problem: Ledgerly is fine, it just can't take payments. Metrics are the meters: counters that only go up and that tools like Prometheus graph over time.

**Small example.** Real results (tests and curl):

| Situation | `/actuator/health` | `/liveness` | `/readiness` | Why |
|---|---|---|---|---|
| gateway up | 200 UP, `paymentsGateway` `latencyMs: 42` | 200 | 200 | all bulbs on |
| gateway down | 503 DOWN, `reason: connection refused` | 200 UP | 503 DOWN | gateway only in readiness |
| `GET /actuator/env` | 404 | | | not exposed |
| 2 transfers OK + 1 of 75,000 | | | | `outcome=ok` +2, `outcome=rejected` +1; the 75,000 one is a 422 |

**Gotchas**
- The indicator's bean name minus `HealthIndicator` is the component name (`paymentsGateway`).
- A slow `health()` makes every probe slow; keep checks cheap or cached.
- `management.server.port=8081` puts Actuator on a port that only the cluster can reach.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** Restarting healthy pods doesn't fix the partner.
2. **A.** `env` can leak secrets.
3. **A.** One name, a small fixed set of tag values; ids would explode the number of series.

</details>
