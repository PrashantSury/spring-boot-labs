# Chapter 4: Dependency injection

By the end of this chapter you can take collaborators through a constructor, choose between two beans of one type, inject every bean of a type as a `List` or `Map`, ask for a bean that may not exist, and break a circular dependency.

## Why you need this

Ledgerly's `TransferService` gets its fee policy through a field marked `@Autowired`. A tester wants to check one transfer with a fake fee of 7. She can't: the class has no constructor that takes a fee policy, and the field is private. A month later the class has nine `@Autowired` fields and nobody noticed.

Chapter 3 put beans **into** the context. This chapter is about how beans get **into each other**. That is dependency injection: a class says what it needs, and Spring hands it in.

## What you will learn

- **Constructor injection:** the class lists what it needs as constructor parameters. Fields can be `final`, and a unit test can call `new` with fakes.
- **Choosing between two beans:** `@Primary` sets the default; `@Qualifier("name")` picks one by name at the injection point.
- **Many beans at once:** `List<Notifier>` (sorted by `@Order`) and `Map<String, Notifier>` (keyed by bean name).
- **Optional beans:** `ObjectProvider<T>` (or `Optional<T>`) when a bean may not exist.
- **Circular dependencies:** why they fail at startup, the quick fix (`@Lazy`), and the real fix (redesign).

## How Spring picks a bean for a parameter

For `TransferService(FeePolicy fees, ...)` Spring needs exactly one `FeePolicy`. This chapter has two: `standardFees` (1%, at least 10) and `zeroFees` (0). Spring decides in this order:

| Step | Rule | In this chapter |
|---|---|---|
| 1 | Only one bean of the type? Use it. | no, there are two |
| 2 | `@Qualifier("x")` on the parameter? Use bean `x`. | `PremiumTransferService` (task 2) |
| 3 | Exactly one bean is `@Primary`? Use it. | `standardFees` (task 2) |
| 4 | A bean whose name equals the parameter name? Use it. | not used here; fragile |
| 5 | Otherwise: startup fails. | real output below |

Real output when task 1 is done but task 2 is not:

```
context did not start: NoUniqueBeanDefinitionException: No qualifying bean of type 'ledgerly.FeePolicy' available: expected single matching bean but found 2: standardFees,zeroFees
```

## Constructor vs field injection

```java
// field injection (the stub)              // constructor injection (task 1)
@Service                                    @Service
public class TransferService {              public class TransferService {
  @Autowired                                  private final FeePolicy fees;
  private StandardFeePolicy fees;             private final AuditTrail audit;
  @Autowired
  private AuditTrail audit;                   public TransferService(FeePolicy fees, AuditTrail audit) {
  ...                                           this.fees = fees;
}                                               this.audit = audit;
                                              }
                                            }
```

The right-hand class needs no Spring in a test: `new TransferService(amount -> 7, new AuditTrail())`. A class with **one** constructor does not need `@Autowired`; Spring uses it.

## All beans of a type

Three notifiers exist: `SmsNotifier` (`@Order(1)`), `EmailNotifier` (`@Order(2)`), `PushNotifier` (`@Order(3)`). Ask for all of them:

```java
public NotificationHub(List<Notifier> all, Map<String, Notifier> byName) { ... }
```

Real output from the finished lab:

```
channels                     [emailNotifier, pushNotifier, smsNotifier]
notifyAll                    [SMS: Salary credited, EMAIL: Salary credited, PUSH: Salary credited]
notifyVia emailNotifier      EMAIL: OTP 4821
```

The list follows `@Order`; the map's keys are bean names. Add a fourth `Notifier` bean and the hub uses it with no code change.

## A circular dependency

`AccountService(StatementService)` and `StatementService(AccountService)`. Spring starts building one, needs the other, which needs the first. Real output on the stub:

```
context did not start: BeanCurrentlyInCreationException: Error creating bean with name 'accountService': Requested bean is currently in creation: Is there an unresolvable circular reference or an asynchronous initialization dependency?
```

`@Lazy` on one constructor parameter injects a stand-in (a proxy) that finds the real bean on first use. That starts the app. The better fix is to move what both need (here, the balance lookup) into a third bean.

## Try it

```bash
mvn -q -pl chapters/04-dependency-injection compile exec:java "-Dexec.mainClass=ledgerly.InjectionTour"
mvn -pl chapters/04-dependency-injection test -Popen
```

`-Popen` runs this chapter's checks. Without it a root build skips them, so `mvn test` from the root stays green while the chapter is open. On the stub the tour prints `channels []`, `allowed 50000 false` and the circular-dependency error above. Finished, it prints the lines shown in this README.

## Your turn

You edit `TransferService.java`, `StandardFeePolicy.java`, `PremiumTransferService.java`, `NotificationHub.java`, `LimitChecker.java`, `cycle/StatementService.java` and `interview-answer.md`. Each has a `// Task N` comment.

1. **Constructor injection.** `TransferService` takes `(FeePolicy, AuditTrail)` in one constructor, two `private final` fields, no `@Autowired`.
2. **`@Primary` and `@Qualifier`.** `standardFees` is primary. `PremiumTransferService` takes `@Qualifier("zeroFees") FeePolicy` through its constructor.
3. **`List` and `Map`.** `NotificationHub` gets all notifiers and finishes `notifyAll`, `notifyVia` and `channels`.
4. **Optional bean.** `LimitChecker` works with or without a `FraudScreen` bean: amount in 1..2,00,000, and blocked accounts refused when a screen exists.
5. **Break the cycle.** `StatementService` keeps its constructor and final field, and the two services start.
6. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- Task 1 alone breaks startup with "found 2: standardFees,zeroFees". Tasks 1 and 2 belong together.
- Task 2: don't make **both** policies primary. And don't rename the constructor parameter to `zeroFees` instead of using `@Qualifier`: it works until someone renames the parameter.
- Task 3: don't write `new SmsNotifier()` inside the hub. The check adds a fourth notifier and expects the hub to pick it up.
- Task 4: a plain `FraudScreen` parameter makes startup fail when no screen exists. `200_000` is allowed; `0` is not.
- Task 5: switching to field injection also "fixes" the cycle, but the check wants the final field kept.

**Interview question (task 6).** Why is constructor injection preferred over field injection? With two beans of one type, how does Spring choose, and what if it can't? What happens with a circular constructor dependency, and what is the proper fix? The questions are repeated at the top of `interview-answer.md`.
