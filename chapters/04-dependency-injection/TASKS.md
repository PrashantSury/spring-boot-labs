# Chapter 4 · Dependency injection

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter04CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter04CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Constructor injection vs field injection, and why constructors win | not written yet | — | checks not written yet | ⬜ |
| 2 | Injecting an interface with two implementations: `@Qualifier` and `@Primary` | not written yet | — | checks not written yet | ⬜ |
| 3 | Injecting lists, maps and optional beans | not written yet | — | checks not written yet | ⬜ |
| 4 | Circular dependencies and how to break them | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Why is constructor injection preferred over field injection, and how does Spring choose between two beans of the same type?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Field injection lets a class collect ten dependencies unnoticed, and it cannot be built in a plain unit test
- Two beans of one type and no `@Primary` gives `NoUniqueBeanDefinitionException`

## ❓ Checks (letters only)

1. Two `FeePolicy` beans, no `@Primary`, no `@Qualifier`, parameter named `fees`. At startup: A. Spring picks the first one registered · B. startup fails with `NoUniqueBeanDefinitionException` · C. `null` is injected
2. A class has exactly one constructor. Does it need `@Autowired`? A. yes, always · B. no, Spring uses the only constructor
3. The strongest reason to prefer constructor injection: A. it is faster at runtime · B. `final` fields and a plain `new` in unit tests · C. it needs fewer imports

<details><summary>📖 Teaching</summary>

**Why it matters.** A transfer service that builds its own fee calculator with `new` cannot be tested or changed; let Spring hand its collaborators in.

**The concept.** Spring builds each bean and hands it the collaborators it needs, through the constructor. A class with one constructor needs no `@Autowired`.

**Small example.**

```java
interface FeePolicy { long fee(long amount); }

@Component class StandardFee implements FeePolicy {
    public long fee(long amount) { return Math.max(amount / 100, 10); }
}
@Component @Primary class PremiumFee implements FeePolicy {
    public long fee(long amount) { return 0; }
}

@Service class TransferService {
    private final FeePolicy fees;
    TransferService(FeePolicy fees) { this.fees = fees; }   // one constructor: no @Autowired
    long charge(long amount) { return fees.fee(amount); }
}
```

| Case | Result | In or out, and why |
|---|---|---|
| Only `StandardFee` exists | `charge(50_000)` = 500 | one candidate, Spring injects it |
| Both exist, `PremiumFee` is `@Primary` | `charge(50_000)` = 0 | `@Primary` wins a by-type tie |
| Both exist, no `@Primary` | startup fails: `NoUniqueBeanDefinitionException` | two candidates, no rule to pick one |
| Constructor takes `@Qualifier("standardFee") FeePolicy fees` | `charge(50_000)` = 500 | the qualifier names the bean; it beats `@Primary` |
| Plain unit test: `new TransferService(a -> 7).charge(100)` | 7 | constructor injection needs no Spring in a test |

**Gotchas**
- If a constructor parameter's name matches a bean name (e.g. `standardFee`), Spring uses it to break the tie. It works, but it hides the choice.
- Constructor cycles (A needs B, B needs A) fail at startup; Spring Boot forbids circular references by default.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Two candidates and no tie-breaker: Spring refuses to guess.
2. **B.** Since Spring 4.3 a single constructor is used automatically.
3. **B.** Dependencies are explicit, immutable and testable without a context.

</details>
