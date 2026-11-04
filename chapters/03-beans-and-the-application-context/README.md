# Chapter 3: Beans and the application context

By the end of this chapter you can say what a bean is, put one in the application context in two different ways, give it a name, control how many copies exist, and fix the "found 2 beans" error.

## Why you need this

Ledgerly now has a fee calculator, interest rules, receipts and a clock. Rohan's first version creates them with `new` wherever they are needed. Then two things go wrong in one week: the fee calculator is created 40 times per second for no reason, and a unit test cannot replace the real clock with a fixed one, because `Clock.systemDefaultZone()` is typed into five different classes.

With plain Java you would write a "factory" class that makes each object once, hands it out, and lets tests swap it. Spring does exactly that, for every object, from one place: the **application context**.

## What you will learn

- **Beans and the context:** a bean is an object Spring creates, keeps and hands out. The context is where they live.
- **Two ways to make a bean:** a stereotype annotation like `@Component` on your class, and a `@Bean` method for classes you can't annotate.
- **Names and scope:** every bean has a name. By default there is one shared copy (singleton). `prototype` gives a new copy each time.
- **Choosing between beans:** `@Primary` when two beans have the same type.

## The context holds your beans

Run the finished demo in this chapter (see *Try it*). It builds a context by scanning package `ledgerly` and lists what is inside. This is real output from a finished lab:

```
Beans in the context (Spring's own are hidden):
  auditLog  ->  AuditLog
  bankConfig  ->  BankConfig
  demoConfig  ->  DemoConfig
  feeCalculator  ->  FeeCalculator
  otpToken  ->  OtpToken
  receipt  ->  Receipt
  bankClock  ->  SystemClock
  savingsPolicy  ->  InterestPolicy
  currentPolicy  ->  InterestPolicy
  rupeeFormat  ->  DecimalFormat
  branchCodes  ->  ListN
```

The left column is the **bean name**. The right column is its type. In the stub (before your tasks), `feeCalculator`, `bankClock`, `savingsPolicy` and `currentPolicy` are missing. Those are your tasks.

## Way 1: `@Component` (your own class)

`AuditLog` in this chapter is a finished demo:

```java
@Component
public class AuditLog { ... }
```

`@Component` (and its cousins `@Service`, `@Repository`, `@RestController`) says: "Spring, create one of me." Component scanning finds it. The default bean name is the class name with a lower-case first letter: `auditLog`.

## Way 2: `@Bean` (a class you don't own, or want to build yourself)

You cannot put `@Component` on `NumberFormat`. It is from the JDK. So write a method in a `@Configuration` class and annotate it. `DemoConfig` in this chapter is a finished demo:

```java
@Configuration
public class DemoConfig {

  @Bean
  NumberFormat rupeeFormat() {
    return NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));
  }

  @Bean("branchCodes")
  List<String> codes() {
    return List.of("BLR01", "DEL01", "MUM01");
  }
}
```

The bean name is the **method name** (`rupeeFormat`), unless you write one in the annotation (`"branchCodes"`, even though the method is called `codes`). Ask the context for it:

```
rupeeFormat formats 52000      ₹52,000.00
branchCodes                    [BLR01, DEL01, MUM01]
```

**Rule of thumb:** `@Component` when the class is yours. `@Bean` when it isn't, or when you need code to build it.

## Scope: how many copies?

By default Spring makes **one** instance per context and shares it. That is the *singleton* scope. Ask for it twice, get the same object:

```
auditLog twice, same object?   true
```

Add `@Scope("prototype")` and every request for the bean makes a new one. `OtpToken` is a finished demo:

```java
@Component
@Scope("prototype")
public class OtpToken { ... }
```

```
otpToken twice, same object?   false
a token value                  df9482
```

Two warnings, both interview favourites:

- Singleton here means **one per application context**, not one per JVM. It is not the Gang-of-Four singleton pattern.
- A singleton is **not thread-safe by magic**. Many requests share the same object at once. Keep singleton beans without changing fields, or protect them.

## Two beans of the same type

Task 3 creates two `InterestPolicy` beans: savings and current. Now ask for "the" `InterestPolicy` by type. Spring will not guess. Real output:

```
the InterestPolicy             NoUniqueBeanDefinitionException: No qualifying bean of type 'ledgerly.InterestPolicy' available: expected single matching bean but found 2: savingsPolicy,currentPolicy
```

Fix it by marking one as `@Primary`. When two beans match a type and one is primary, the primary wins:

```
the InterestPolicy             InterestPolicy[name=savings, annualRatePercent=3.5]
```

(Chapter 4 shows the other fix: choose by name with `@Qualifier`.)

## Try it

Look inside the context. This works on the stub, and the error lines turn into answers as you finish the tasks:

```bash
mvn -q -pl chapters/03-beans-and-the-application-context compile exec:java "-Dexec.mainClass=ledgerly.BeanTour"
```

Run the checks:

```bash
mvn -pl chapters/03-beans-and-the-application-context test
```

The stub's tour ends with this, real output:

```
Your tasks (these print an error until the task is done):
feeCalculator                  NoSuchBeanDefinitionException: No qualifying bean of type 'ledgerly.FeeCalculator' available
bankClock zone                 NoSuchBeanDefinitionException: No bean named 'bankClock' available
the InterestPolicy             NoSuchBeanDefinitionException: No qualifying bean of type 'ledgerly.InterestPolicy' available
receipt twice, same object?    true
```

After the five code tasks it prints `50`, `Asia/Kolkata`, the savings policy and `false`.

## Your turn

You edit `FeeCalculator.java`, `BankConfig.java`, `Receipt.java` and `interview-answer.md`. Each has a `// Task N` comment.

1. **`@Component`.** Make `FeeCalculator` a bean found by component scanning.
2. **`@Bean` for a JDK class.** In `BankConfig`, make a `Clock` bean named `bankClock` for the zone `Asia/Kolkata`.
3. **Named beans.** In `BankConfig`, make two `InterestPolicy` beans with names written in the annotation: `savingsPolicy` (`"savings"`, 3.5) and `currentPolicy` (`"current"`, 0.0).
4. **Scope.** Make `Receipt` a prototype so each transfer gets its own receipt.
5. **`@Primary`.** Make `savingsPolicy` the winner when someone asks for an `InterestPolicy` by type.
6. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- A method in a `@Configuration` class is **not** a bean until you annotate it. An un-annotated method is just a method.
- Task 3: the name goes in the annotation. Don't rely on the method names (`savings`, `current`). They are not the bean names the check looks for.
- Task 3 alone makes the Task 5 check fail with "found 2". That is expected. Tasks 3 and 5 belong together.
- Task 5: only **one** bean may be primary. Don't mark both.
- Task 4: change only `Receipt`. If `FeeCalculator` becomes a prototype too, the "stays a singleton" check fails.
- Don't put `@Component` on `BankConfig`'s methods. `@Component` goes on classes.
- Task 6: the check cannot judge your answer, only that it exists, is real and covers the ideas. Write it like you'd say it to an interviewer.

**Interview question (task 6).** Spring beans are singletons by default. What does that mean, is it the same as the singleton pattern, does it make a bean thread-safe, and what is the difference between `@Component` and `@Bean`? The questions are repeated at the top of `interview-answer.md`.
