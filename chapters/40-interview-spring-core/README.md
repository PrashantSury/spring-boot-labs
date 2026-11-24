# Chapter 40: Interview: Spring core

This chapter is a rehearsal for the Spring core round of a senior Java interview. You prove four classic answers in small pieces of Ledgerly code, then write the spoken answers with a bank example each time.

## Why you need this

Interviewers don't want definitions. "A prototype bean is a new instance per lookup" is worth very little. "Our statement printer was a singleton holding a prototype buffer, so every customer's statement contained the previous customer's lines; we fixed it with an `ObjectProvider`" gets you the job. Each task below gives you one of those stories, with code you made fail and then fixed.

## What you will rehearse

- **Beans, scopes, injection:** a prototype inside a singleton.
- **Auto-configuration and `@SpringBootApplication`:** writing a tiny auto-configuration with `@ConditionalOnMissingBean`, registered like Boot's own.
- **Profiles and properties:** `@ConfigurationProperties` with defaults, overridden by `application-prod.properties`.
- **Proxies and transactions in one story:** `REQUIRES_NEW` ignored on self-invocation, and the fix.

## Real output

`mvn -q -pl chapters/40-interview-spring-core spring-boot:run` (finished code):

```
1 scopes : ACC-1 #1: rent 15000 | salary 90000
1 scopes : ACC-2 #2: fd interest 1200
2 auto   : FxRates = FixedFxRates, USD 83.12
3 props  : profiles [], limits LimitsProperties[perTransfer=50000, transfersPerDay=20]
4 tx     : IllegalStateException: over the limit: 75000
4 tx     : transfers rows 0, audit rows 1
```

With `-Dspring-boot.run.profiles=prod`:

```
3 props  : profiles [prod], limits LimitsProperties[perTransfer=200000, transfersPerDay=20]
```

On the stub, every line is a bug you can explain:

```
1 scopes : ACC-1 #1: rent 15000 | salary 90000
1 scopes : ACC-2 #1: rent 15000 | salary 90000 | fd interest 1200
2 auto   : FxRates = none
3 props  : profiles [], limits null
4 tx     : IllegalStateException: over the limit: 75000
4 tx     : transfers rows 0, audit rows 0
```

## Try it

```bash
mvn -q -pl chapters/40-interview-spring-core spring-boot:run
mvn -pl chapters/40-interview-spring-core test -Popen
```

H2 in memory; no Docker.

## Your turn

You edit `StatementPrinter.java`, `dev/ledgerly/fx/FxAutoConfiguration.java` + the `AutoConfiguration.imports` file, `LimitsProperties.java` + `LedgerlyApplication.java` + `application-prod.properties`, `AuditWriter.java` + `TransferService.java`, and `interview-answer.md`.

1. **Scopes.** A fresh `StatementBuffer` per statement via `ObjectProvider`.
2. **Auto-configuration.** `@AutoConfiguration`, `@Bean @ConditionalOnMissingBean`, listed in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
3. **Properties and profiles.** `@ConfigurationProperties("ledgerly.limits")` with `@DefaultValue`s, `@ConfigurationPropertiesScan`, prod override.
4. **Transactions.** The audit line survives a rollback: `AuditWriter.write` with `REQUIRES_NEW`, called from `TransferService`.
5. **(Interview)** Four spoken answers in `interview-answer.md`: example first, then what goes wrong, then the fix.

**Traps**

- A prototype injected into a singleton's constructor is created once.
- An auto-configuration class inside your component-scan package is picked up as a normal `@Configuration`, conditions and ordering included; keep it in its own package and list it in the imports file.
- `@ConditionalOnMissingBean` only works reliably in auto-configurations, which run after your own beans are registered.
- `this.audit(...)` inside the same bean ignores `REQUIRES_NEW` (and every other `@Transactional` setting).
- By default a checked exception does **not** roll back; only unchecked ones do (`rollbackFor` changes that).

**Interview question (task 5).** Scopes and a prototype in a singleton. How auto-configuration decides and how to override it. Profiles plus `@ConfigurationProperties`. 90 seconds on `@Transactional` in a transfer and two ways it silently doesn't apply. The questions are repeated at the top of `interview-answer.md`.
