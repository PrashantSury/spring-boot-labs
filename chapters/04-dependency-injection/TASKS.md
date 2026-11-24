# Chapter 4 · Dependency injection

**Time:** ~70 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter04CheckTest` 27/27 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/04-dependency-injection test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/04-dependency-injection compile exec:java "-Dexec.mainClass=ledgerly.InjectionTour"`.
- Fresh stub: 6/27 green. Those 6 are guards (they must stay green while you work).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Constructor injection: `(FeePolicy, AuditTrail)`, `private final`, no `@Autowired` | `src/main/java/ledgerly/TransferService.java` | `mvn -pl chapters/04-dependency-injection test -Popen` | task 1 checks green | ⬜ |
| 2 | `@Primary` on `standardFees`; `@Qualifier("zeroFees")` in the premium constructor | `StandardFeePolicy.java`, `PremiumTransferService.java` | same | task 2 checks green | ⬜ |
| 3 | `List<Notifier>` + `Map<String, Notifier>`: `notifyAll`, `notifyVia`, `channels` | `NotificationHub.java` | same | task 3 checks green | ⬜ |
| 4 | Optional `FraudScreen` via `ObjectProvider`; limit 1..2,00,000 | `LimitChecker.java` | same | task 4 checks green | ⬜ |
| 5 | Break the `AccountService` ⇄ `StatementService` cycle | `cycle/StatementService.java` | same | task 5 checks green | ⬜ |
| 6 | **(Interview)** constructor vs field, choosing a bean, cycles | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 6)

- Why is constructor injection preferred over field injection? Two reasons.
- Two `FeePolicy` beans: how does Spring choose, and what happens if it can't?
- Two services need each other through constructors: what happens at startup, and the proper fix?

## ⚠️ Traps

- Task 1 alone → startup fails with `found 2: standardFees,zeroFees`. Do task 2 next.
- Only **one** primary. Use `@Qualifier`, not a parameter named `zeroFees`.
- Task 3: no `new SmsNotifier()` in the hub; a 4th notifier must just work.
- Task 4: `200_000` allowed, `200_001` and `0` not.
- Task 5: keep the `final` field; field injection is not the fix.

## ❓ Checks (letters only)

1. Two `FeePolicy` beans, no `@Primary`, no `@Qualifier`, parameter named `fees`. At startup: A. Spring picks the first one registered · B. startup fails with `NoUniqueBeanDefinitionException` · C. `null` is injected
2. A class has exactly one constructor. Does it need `@Autowired`? A. yes, always · B. no, Spring uses the only constructor · C. only if it has 2+ parameters
3. `List<Notifier>` with `@Order(2) Email`, `@Order(1) Sms`, and `Push` with no `@Order`. The list is: A. `[Email, Sms, Push]` · B. `[Sms, Email, Push]` · C. random

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "Constructor or field injection, and why?" is asked in almost every Spring interview. The follow-up is always "two beans of the same type?". Getting both right shows you understand how Spring wires an app, not just which annotation to type.

**The concept.** A bean says what it needs as constructor parameters; Spring finds one matching bean per parameter and calls the constructor. If it finds zero (and the parameter isn't optional) or more than one (with no `@Qualifier`/`@Primary` to decide), startup fails.

**Small example.** Sample beans: `standardFees` (`@Primary`), `zeroFees`, and no `FraudScreen`.

```java
public TransferService(FeePolicy fees, AuditTrail audit)                     // A
public PremiumTransferService(@Qualifier("zeroFees") FeePolicy fees)       // B
public LimitChecker(FraudScreen screen)                                      // C
public LimitChecker(ObjectProvider<FraudScreen> screen)                      // D
```

Real output (finished lab):

```
standard transfer of 50000   Transfer[from=ACC-1, to=ACC-2, amount=50000, fee=500]
premium transfer of 50000    Transfer[from=ACC-1, to=ACC-2, amount=50000, fee=0]
allowed 50000                true
allowed 250000               false
```

| Constructor | What gets injected | Why |
|---|---|---|
| A | `standardFees` | two candidates, `@Primary` wins |
| B | `zeroFees` | `@Qualifier` beats `@Primary` |
| C | nothing: startup fails | `NoSuchBeanDefinitionException`, no `FraudScreen` bean |
| D | an empty provider | `getIfAvailable()` returns `null`, the app starts |

**Gotchas**
- `@Autowired` on a private field hides the dependency list; nine of them is a design smell you won't notice.
- `@Qualifier` names a **bean name**, not a class name: `"zeroFees"`, not `"ZeroFeePolicy"`.
- `@Lazy` on a cycle hides a design problem; say "I'd extract the shared part into a third bean".
- Spring Boot apps refuse circular references by default (`spring.main.allow-circular-references=false`), even with field injection.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** Spring will not guess between two candidates; it fails fast at startup.
2. **B.** Since Spring 4.3 a single constructor is used without `@Autowired`.
3. **B.** Ordered beans first by their number, beans without `@Order` go last.

</details>
