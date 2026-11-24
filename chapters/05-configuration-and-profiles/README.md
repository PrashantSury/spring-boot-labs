# Chapter 5: Configuration and profiles

By the end of this chapter you can read a setting with `@Value`, bind a group of settings to a validated record, switch settings and beans with profiles, and say which source wins when a key is set in two places.

## Why you need this

The same Ledgerly jar runs on Rohan's laptop (an in-memory H2 database, a fake SMS sender) and in production (Postgres, the real SMS vendor). Last month someone hard-coded the prod database URL "just for a test" and a laptop wrote into production. Settings belong outside the code, and the jar must pick the right ones without being rebuilt.

## What you will learn

- **`@Value`:** read one key, with or without a default.
- **`@ConfigurationProperties`:** bind every `ledgerly.transfer.*` key to one typed record, and validate it at startup.
- **Profile files:** `application-prod.properties` is read only when the `prod` profile is active, on top of `application.properties`.
- **`@Profile` beans:** a bean that exists only in some profiles.
- **Precedence:** command line beats environment variables beats profile files beats the base file.

## One jar, four runs

`ConfigTour` starts the app four times. This is real output from a finished lab:

```
== no profile
bank name        Ledgerly Bank
support phone    1800-123-4567
database url     jdbc:h2:mem:ledgerly
transfer rules   TransferProperties[dailyLimit=200000, feePercent=1.0, otpValidity=PT5M]
sms gateway      FakeSmsGateway
daily-limit from Config resource 'class path resource [application.properties]' via location 'optional:classpath:/'

== prod
bank name        Ledgerly Bank
database url     jdbc:postgresql://db.ledgerly.internal:5432/ledgerly
transfer rules   TransferProperties[dailyLimit=100000, feePercent=1.0, otpValidity=PT5M]
sms gateway      RealSmsGateway
daily-limit from Config resource 'class path resource [application-prod.properties]' via location 'optional:classpath:/'

== prod + command line
transfer rules   TransferProperties[dailyLimit=150000, feePercent=1.0, otpValidity=PT5M]
daily-limit from commandLineArgs
```

(Some lines are trimmed.) Read it as a story: prod **changed** two keys and **kept** `feePercent=1.0` and the bank name from the base file. Then `--ledgerly.transfer.daily-limit=150000` on the command line beat both files.

## `@Value`: one key

```java
public BranchInfo(@Value("${ledgerly.bank-name}") String bankName,
                  @Value("${ledgerly.support-phone:1800-123-4567}") String supportPhone)
```

After the colon is the default. No default and no key means startup fails with `Could not resolve placeholder 'ledgerly.bank-name'`. That failure is good: a missing setting should be loud.

## `@ConfigurationProperties`: a group of keys

```properties
ledgerly.transfer.daily-limit=200000
ledgerly.transfer.fee-percent=1.0
ledgerly.transfer.otp-validity=5m
```

```java
@ConfigurationProperties(prefix = "ledgerly.transfer")
@Validated
public record TransferProperties(@NotNull @Positive Long dailyLimit,
                                 @NotNull @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal feePercent,
                                 @NotNull Duration otpValidity) {}
```

`daily-limit` binds to `dailyLimit` (relaxed binding), `5m` becomes a `Duration`. Real output with `--ledgerly.transfer.daily-limit=0`:

```
APPLICATION FAILED TO START
Binding to target ledgerly.TransferProperties failed:
    Property: ledgerly.transfer.dailyLimit
    Value: "0"
    Origin: "ledgerly.transfer.daily-limit" from property source "commandLineArgs"
    Reason: must be greater than 0
```

**Rule of thumb:** `@Value` for one or two loose keys. `@ConfigurationProperties` for a group, a type, and validation.

## Who wins

The Environment is a list of property sources. The first one that has the key wins:

| Priority | Source | Example |
|---|---|---|
| 1 (highest) | command-line arguments | `--ledgerly.transfer.daily-limit=150000` |
| 2 | Java system properties | `-Dledgerly.transfer.daily-limit=150000` |
| 3 | OS environment variables | `LEDGERLY_TRANSFER_DAILYLIMIT=150000` |
| 4 | `application-{profile}.properties` | `application-prod.properties` |
| 5 (lowest) | `application.properties` | the base file |

## Try it

```bash
mvn -q -pl chapters/05-configuration-and-profiles compile exec:java "-Dexec.mainClass=ledgerly.ConfigTour"
mvn -pl chapters/05-configuration-and-profiles test -Popen
```

On the stub the first run prints `bank name null`, `NoSuchBeanDefinitionException` for `TransferProperties` and `found 2: fakeSmsGateway,realSmsGateway` for the gateway.

## Your turn

You edit `application.properties`, `application-dev.properties`, `application-prod.properties`, `BranchInfo.java`, `TransferProperties.java`, `LedgerlyApplication.java`, `FakeSmsGateway.java`, `RealSmsGateway.java`, `EnvironmentReport.java` and `interview-answer.md`.

1. **`@Value`.** `BranchInfo` reads `ledgerly.bank-name` (required) and `ledgerly.support-phone` (default `1800-123-4567`) through its constructor.
2. **`@ConfigurationProperties`.** Bind and validate `TransferProperties`, add the three keys, add `@ConfigurationPropertiesScan`.
3. **Profile files.** `prod` changes the database URL and the daily limit; `dev` renames the bank. Nothing else.
4. **`@Profile` beans.** `RealSmsGateway` only in `prod`; `FakeSmsGateway` everywhere else.
5. **Precedence.** `EnvironmentReport.whereFrom(key)` names the source that wins.
6. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- `@Value("${ledgerly.bank-nmae:Ledgerly}")` with a typo never fails: the default hides it. Give required keys **no** default.
- A profile file **adds**. Don't copy the whole base file into `application-prod.properties`; the check wants `feePercent` inherited.
- Task 4: `@Profile("dev")` on the fake is wrong: `uat` and "no profile" would get no gateway at all. Use `"!prod"`.
- Task 2 without `@ConfigurationPropertiesScan`: the record compiles but is not a bean.
- Task 5: skip the source called `configurationProperties`; it is a view over all the others.

**Interview question (task 6).** When do you use `@Value` and when `@ConfigurationProperties`? How does the same jar use a different database in prod without a code change? What does an active profile add, and what does it not do? The questions are repeated at the top of `interview-answer.md`.
