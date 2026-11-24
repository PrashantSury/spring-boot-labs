# Chapter 3 · Beans and the application context

**Time:** ~60 min + 25 min re-answer · **Status:** 🔴 code ✅ 15/15 (20 Nov), interview 5/10 → re-answer open · **Done when:** `Chapter03CheckTest` 15/15 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/03-beans-and-the-application-context test`.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@Component` on `FeeCalculator` | `src/main/java/ledgerly/FeeCalculator.java` | `mvn -pl chapters/03-beans-and-the-application-context test` | task 1 checks green | ✅ |
| 2 | `Clock` bean `bankClock`, zone `Asia/Kolkata` | `src/main/java/ledgerly/BankConfig.java` | same | task 2 checks green | ✅ |
| 3 | Named `InterestPolicy` beans `savingsPolicy` (3.5) and `currentPolicy` (0.0) | `BankConfig.java` | same | task 3 checks green | ✅ |
| 4 | `Receipt` is a prototype | `src/main/java/ledgerly/Receipt.java` | same | task 4 checks green | ✅ |
| 5 | `savingsPolicy` is `@Primary` | `BankConfig.java` | same | task 5 checks green | ✅ |
| 6 | **(Interview)** singleton, thread safety, `@Component` vs `@Bean` | `interview-answer.md` | graded by Lumi | ≥ 7/10 | 🔴 5/10 |

## 🎤 Interview re-answer (open)

- Rewrite answers 2 and 3 in `interview-answer.md` (4-8 sentences), then L3-L5 below them. L6-L8 in chat.

| # | Question (short) |
|---|---|
| L1 | Does Spring make a singleton bean's **fields** thread-safe? (yes / no) |
| L2 | What does Spring give you per request for free? (method locals, request-scoped beans, `@Transactional` + the DB) |
| L3 | Why `total += fee` races (field vs local), then **two real fixes**; `@Component` vs `@Bean`: the rule **and the why** |
| L4 | Singleton `TransferService` with `private BigDecimal runningTotal` drifts: why + two fixes (one survives **3 instances and a restart**); prototype `Receipt` in a singleton's constructor: new per transfer? If not, two ways |
| L5 | Floor vs cap: "1% but **at least** ₹10" → `Math.___(amount / 100, 10)` · "2% but **at most** ₹500" → `Math.___(amount * 2 / 100, 500)` |

## ❓ Checks (letters only)

6. (L6) Which change alone makes `A` correct inside **one** JVM? A. `volatile long total` · B. `AtomicLong` + `addAndGet` · C. `@Lazy` · D. make `add` `private`
7. (L7) Three instances behind a load balancer: which keeps **one** correct total? A. `synchronized add` · B. `AtomicLong` · C. one DB row updated with `total = total + ?` · D. `ThreadLocal<Long>`
8. (L8) A prototype injected into a singleton's constructor is created: A. once per call · B. once, when the singleton is built · C. once per request · D. never

<details><summary>📖 Teaching (read before L3)</summary>

**Why it matters.** "Is a singleton bean thread-safe?" is asked in nearly every senior Spring loop, usually as "our totals drift under load". Your code was 15/15; only the explanation is open.

**The concept.** One bean instance serves every request thread. Its **fields** live on the heap and are shared; **locals** live on each thread's own stack.

**Small example.** Say "races" or "safe", and why:

```java
@Service class A { private long total;          void add(long f) { total += f; } }
@Service class B { private final Clock clock;   long now() { return clock.millis(); } }   // set once in the constructor
@Service class C {                              long fee(long amt) { long f = amt / 100; return Math.max(f, 10); } }
```

| Bean | Shared field written by request threads? | Verdict |
|---|---|---|
| `A` | ? | ? |
| `B` | ? | ? |
| `C` | ? | ? |

**Gotchas**
- "Deep copies of the mutable fields" (the first answer) doesn't stop two threads writing the same field.
- One-JVM fixes (`synchronized`, `AtomicLong`) break with 3 instances; shared state then lives in the DB.
- `@Component` is found by scan on your own class; `@Bean` is a factory method you write, needed for classes you don't own (like `Clock`).

</details>

<details><summary>🔒 Answers (warm-up and checks)</summary>

- Warm-up: `A` races (one heap field, read-add-write). `B` is safe (`final`, only read). `C` is safe (`f` is a local).
- 6. **B.** Atomic read-add-write in one JVM; `volatile` gives visibility, not atomicity.
- 7. **C.** One shared row updated atomically by the database.
- 8. **B.** Injection happens once; use `ObjectProvider<Receipt>` or a lookup method for one per call.

</details>
