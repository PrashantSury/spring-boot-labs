# ✅ Tasks to complete

**Now:** the banking app is the active Spring work. These labs are **paused at Chapter 3**: one written re-answer is open, then Chapter 4 waits.
Stuck 15 min → ask Lumi.

| # | Task | Where | Time | Done when | Status |
|---|---|---|---|---|---|
| 1 | 🏦 Banking app, Module 1 (AuthN / AuthZ), 8 tasks | [java-switch-journey `banking-app/TASKS.md`](https://github.com/PrashantSury/java-switch-journey/blob/main/banking-app/TASKS.md) | ~45 min a day | Module 1 tests green + interview ≥ 7 | 🟡 active |
| 2 | 🌱 Ch 3 interview re-answer (L1-L5) | `chapters/03-beans-and-the-application-context/interview-answer.md` | ~25 min | graded ≥ 7 (now 5/10) | 🔴 open |
| 3 | 🌱 Ch 3 checks (L6-L8) | chat, letters only | 3 min | 3 answers | ⬜ |
| 4 | 🌱 Chapter 4 onward | `chapters/04-…` | — | — | ⏸️ paused |

## 1. 🏦 Banking app (active)

- **Plan, tasks and teaching:** [banking-app/TASKS.md](https://github.com/PrashantSury/java-switch-journey/blob/main/banking-app/TASKS.md) in java-switch-journey.
- **Today's task** is in that repo's `tasks/dayNN.md`; progress is in its `PROGRESS.md`, section 🏦 Banking app.
- **Why there:** the banking app lives at `java-switch-journey/banking-app/` (Spring Boot 4.1, Java 21, Postgres in Docker on 5433, Flyway).
- Lab chapters that teach the same ideas, if you want a refresher: 24-31 (security, JWT, method security, security errors), 12-13 (validation, errors).

## 2. 🌱 Labs (paused at Chapter 3)

- **Goal:** explain why a singleton bean's field races, give two real fixes, and say `@Component` vs `@Bean` with the why.
- **File:** rewrite answers 2 and 3 in `chapters/03-beans-and-the-application-context/interview-answer.md` (4-8 sentences), plus L3-L5 below it.
- **Done when:** graded ≥ 7, then Chapter 4 can open (only if you un-pause the labs).
- **First (1 min):** ask what Spring already does for you (L1-L2).

| # | Question (short) |
|---|---|
| L1 | Does Spring make a singleton bean's **fields** thread-safe? (yes / no) |
| L2 | What does Spring give you per request for free? (method locals, request-scoped beans, `@Transactional` + the DB) |
| L3 | Q1 part 2: *why* `total += fee` races (where a field lives vs a local), then **two real fixes**. Part 3: `@Component` vs `@Bean`, the rule **and the why** |
| L4 | Q2: singleton `TransferService` with `private BigDecimal runningTotal` drifts under load. Why, plus two fixes with trade-offs; one must survive **3 instances and a restart**. Q3: a prototype `Receipt` injected through a singleton's constructor: new receipt per transfer? If not, two ways to get one per call |
| L5 | Floor vs cap: (a) "1% but **at least** ₹10" → `Math.___(amount / 100, 10)` · (b) "2% but **at most** ₹500" → `Math.___(amount * 2 / 100, 500)` |
| L6 | Which change alone makes `A` correct inside **one** JVM? A. `volatile long total` · B. `AtomicLong` + `addAndGet` · C. `@Lazy` · D. make `add` `private` |
| L7 | Three instances behind a load balancer: which keeps **one** correct total? A. `synchronized add` · B. `AtomicLong` · C. one DB row updated with `total = total + ?` · D. `ThreadLocal<Long>` |
| L8 | A prototype bean injected into a singleton's constructor is created: A. once per call · B. once, when the singleton is built · C. once per request · D. never |

<details><summary>📖 Warm-up and teaching (read before L3)</summary>

**Why it matters.** "Is a singleton bean thread-safe?" comes up in nearly every senior Spring interview, usually disguised as "our totals drift under load". Your Chapter 3 code was 15/15. Only the explanation is open.

**The concept.** One bean instance is shared by every request thread. Its **fields** live on the heap, so all threads share them. **Locals** live on each thread's own stack, so they're private to that thread.

**Small example.** Say "races" or "safe" for each, and why:

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
- "Make deep copies of the mutable fields" (your first answer) doesn't stop two threads writing the same field.
- A fix that works in one JVM (`synchronized`, `AtomicLong`) breaks with 3 instances. Shared state then has to live somewhere shared, like the DB.

</details>

<details><summary>🔒 Check the warm-up after you fill the table</summary>

`A` races: there is one `total` on the heap, and every thread does read-add-write on it. `B` is safe: the field is `final` and only read. `C` is safe: `f` is a local on each thread's own stack.

</details>

**Chapter 4 onward (paused):** `chapters/04-dependency-injection` → `41-interview-data-and-web`. Each chapter keeps its own `README.md`, stubs and `ChapterNNCheckTest`; nothing is assigned until the labs are un-paused.
