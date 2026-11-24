# Chapter 36: Actuator

By the end of this chapter the running Ledgerly app can tell operations whether it is healthy, which version it is, how many transfers it has done, and whether Kubernetes should send it traffic right now, without exposing anything an attacker would love.

## Why you need this

Friday evening the payments gateway went down. Kubernetes saw Ledgerly's health turn DOWN, decided the app itself was broken and restarted every pod, again and again, which turned a partner outage into a full Ledgerly outage. Meanwhile nobody could say which version was deployed or how many transfers had failed. Spring Boot Actuator answers all of that, if you configure it on purpose.

## What you will learn

- **Actuator endpoints:** `health`, `info`, `metrics`, and which ones are dangerous (`env`, `beans`, `heapdump`, `configprops`).
- **Exposure:** `management.endpoints.web.exposure.include` and why "`*`" is a bad idea.
- **A custom `HealthIndicator`** with details.
- **Liveness vs readiness** probes and health groups.
- **Micrometer counters** with tags for business metrics.

## Real output

The finished app on port 8098:

```
$ curl -X POST localhost:8098/transfers?amount=100
{"status":"ok"} [200]
$ curl -X POST localhost:8098/transfers?amount=75000
{"timestamp":"2026-11-24T11:07:31.717Z","status":422,"error":"Unprocessable Content","path":"/transfers"} [422]
$ curl localhost:8098/actuator/info
{"app":{"name":"Ledgerly","version":"1.4.0"}} [200]
$ curl localhost:8098/actuator/env
{"timestamp":"2026-11-24T11:07:31.794Z","status":404,"error":"Not Found","path":"/actuator/env"} [404]
$ curl localhost:8098/actuator/metrics/ledgerly.transfers?tag=outcome:ok
{"availableTags":[],"measurements":[{"statistic":"COUNT","value":1.0}],"name":"ledgerly.transfers"} [200]
$ curl -X POST localhost:8098/demo/gateway?up=false
{"reachable":false} [200]
$ curl localhost:8098/actuator/health        (diskSpace, ping and ssl components trimmed)
{"components":{...,"livenessState":{"status":"UP"},"paymentsGateway":{"details":{"reason":"connection refused"},"status":"DOWN"},...,"readinessState":{"status":"UP"},...},"groups":["liveness","readiness"],"status":"DOWN"} [503]
$ curl localhost:8098/actuator/health/liveness
{"status":"UP"} [200]
$ curl localhost:8098/actuator/health/readiness
{"components":{"paymentsGateway":{"details":{"reason":"connection refused"},"status":"DOWN"},"readinessState":{"status":"UP"}},"status":"DOWN"} [503]
```

(`[200]` is the HTTP status, printed by `curl -w`.) With the gateway down, readiness says "no traffic, please" while liveness says "don't restart me". On the stub, `info`, `metrics` and `/actuator/health/paymentsGateway` are 404 and readiness stays UP.

## The shape of it

```java
@Component
class PaymentsGatewayHealthIndicator implements HealthIndicator {
  public Health health() {
    return gateway.ping() ? Health.up().withDetail("latencyMs", 42).build()
                          : Health.down().withDetail("reason", "connection refused").build();
  }
}

ok = Counter.builder("ledgerly.transfers").tag("outcome", "ok").register(registry);
```

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.group.readiness.include=readinessState,paymentsGateway
```

## Try it

```bash
mvn -pl chapters/36-actuator spring-boot:run
curl localhost:8098/actuator/health
mvn -pl chapters/36-actuator test -Popen
```

## Your turn

You edit `application.properties`, `PaymentsGatewayHealthIndicator.java`, `TransferService.java` and `interview-answer.md`.

1. **Expose safely.** `health,info,metrics` only; health details; `info.app.name` / `info.app.version`.
2. **Health indicator.** UP with `latencyMs`, DOWN with `reason`.
3. **Counters.** `ledgerly.transfers` with tag `outcome=ok|rejected`.
4. **Readiness.** Put `paymentsGateway` in the readiness group, and keep it out of liveness.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- `exposure.include=*` publishes `env`, `configprops` and `heapdump`: passwords and memory contents.
- `show-details=always` is fine on a private management port; on a public port use `when-authorized` with security.
- Putting a downstream dependency in liveness makes Kubernetes restart healthy pods during a partner outage.
- Health UP only means the checks you configured passed. It says nothing about checks you never wrote.
- Never tag a metric with an account id or user id: every value becomes its own time series.

**Interview question (task 5).** What Actuator gives you and how to expose it safely. Custom health checks; liveness vs readiness. Business metrics with Micrometer. The questions are repeated at the top of `interview-answer.md`.
