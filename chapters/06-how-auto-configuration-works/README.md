# Chapter 6: How auto-configuration works

By the end of this chapter you can explain how Spring Boot decides which beans to create for you, write a tiny auto-configuration of your own, switch it with conditions, make it back off, and read the conditions report that `--debug` prints.

## Why you need this

Interviewers love "how does Spring Boot know to create a `DataSource`?". "It's magic" and "because of the starter" are both wrong answers. The right answer is a mechanism with three parts: a list of auto-configuration classes, conditions on each one, and backing off when you define your own bean.

Ledgerly's platform team also wants every Ledgerly service to get an audit sink for free, like Boot gives a `DataSource`. In this chapter you build that "audit starter".

## What you will learn

- **Where auto-configurations come from:** the file `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Not component scanning.
- **Conditions:** `@ConditionalOnProperty`, `@ConditionalOnClass`, `@ConditionalOnMissingBean`.
- **Backing off:** your own bean of the same type makes the default disappear.
- **The conditions report:** `--debug` (or `ConditionEvaluationReport`) says what matched and why.

## The mechanism in one picture

```
@SpringBootApplication
  └─ @EnableAutoConfiguration
       └─ reads every AutoConfiguration.imports file on the classpath
            └─ LedgerlyAuditAutoConfiguration          @ConditionalOnProperty(ledgerly.audit.enabled, matchIfMissing)
                 ├─ auditSink()      @ConditionalOnMissingBean(AuditSink)  → ConsoleAuditSink
                 └─ siemForwarder()  @ConditionalOnClass("ledgerly.siem.SiemClient")
```

Boot's own `DataSourceAutoConfiguration` is the same shape: on the classpath only if JDBC classes are there, backing off if you made a `DataSource`.

## Real output

`AutoConfigTour` starts the app twice. Real output from a finished lab:

```
== default
audit sink           console: login ACC-1
siem forwarder       siem: login ACC-1
config class         matched
auditSink method     matched

== audit switched off
audit sink           NoSuchBeanDefinitionException: No qualifying bean of type 'ledgerly.audit.AuditSink' available
siem forwarder       NoSuchBeanDefinitionException: No qualifying bean of type 'ledgerly.audit.SiemForwarder' available
config class         did not match
auditSink method     not evaluated
```

When the class does not match, its methods are never looked at: `not evaluated`.

The same answer from `--debug`, real output:

```
   LedgerlyAuditAutoConfiguration:
      Did not match:
         - @ConditionalOnProperty (ledgerly.audit.enabled=true) found different value in property 'enabled' (OnPropertyCondition)
```

## Testing an auto-configuration

Boot ships `ApplicationContextRunner` for exactly this. The checks use it:

```java
new ApplicationContextRunner()
    .withConfiguration(AutoConfigurations.of(LedgerlyAuditAutoConfiguration.class))
    .withUserConfiguration(OwnSinkConfig.class)            // the app brings its own AuditSink
    .withClassLoader(new FilteredClassLoader("ledgerly.siem")) // pretend the vendor jar is missing
    .run(ctx -> assertThat(ctx).hasSingleBean(AuditSink.class));
```

## Try it

```bash
mvn -q -pl chapters/06-how-auto-configuration-works compile exec:java "-Dexec.mainClass=ledgerly.app.AutoConfigTour"
mvn -q -pl chapters/06-how-auto-configuration-works exec:java "-Dexec.mainClass=ledgerly.app.LedgerlyApplication" "-Dexec.args=--debug"
mvn -pl chapters/06-how-auto-configuration-works test -Popen
```

On the stub the tour prints `NoSuchBeanDefinitionException` for both beans and `null` for the outcomes: the starter is never found.

## Your turn

You edit `autoconfigure/LedgerlyAuditAutoConfiguration.java`, `META-INF/spring/...AutoConfiguration.imports`, `app/ConditionsReader.java` and `interview-answer.md`.

1. **Register it.** `@AutoConfiguration` on the class and one line in the imports file.
2. **Back off.** `@ConditionalOnMissingBean(AuditSink.class)` on `auditSink()`.
3. **On/off switch.** `@ConditionalOnProperty`: on when `ledgerly.audit.enabled=true` or the key is missing.
4. **Classpath.** `siemForwarder()` only when `ledgerly.siem.SiemClient` is on the classpath.
5. **Read the report.** `ConditionsReader.outcome(ctx, source)`: `matched`, `did not match` or `not evaluated`.
6. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- `@Import(LedgerlyAuditAutoConfiguration.class)` on the app "works", but that is not how a starter is found. The check forbids it.
- Don't add `@Component`/`@Configuration` and hope scanning finds it: the app scans `ledgerly.app` only, and Boot excludes auto-configurations from scanning anyway.
- Task 2 on the **class** would also remove `siemForwarder`. Put it on the method.
- Task 3: `matchIfMissing = true`, or a fresh app without the key loses its audit sink.
- Task 4: use `name = "ledgerly.siem.SiemClient"` (a String). A `Class` reference fails to load when the jar is really missing.

**Interview question (task 6).** How does Spring Boot know to create a `DataSource`? Where does it find auto-configurations, which conditions decide, what happens when you define your own, and how do you check why? The questions are repeated at the top of `interview-answer.md`.
