# ✅ Tasks to complete

**Now:** the banking app is the active Spring work. These labs are **paused at Chapter 3**: one written re-answer is open, then Chapter 4 waits.
Stuck 15 min → ask Lumi.

| # | Task | Where | Time | Done when | Status |
|---|---|---|---|---|---|
| 1 | 🏦 Banking app, Module 1 (AuthN / AuthZ), 8 tasks | [java-switch-journey `banking-app/TASKS.md`](https://github.com/PrashantSury/java-switch-journey/blob/main/banking-app/TASKS.md) | ~45 min a day | Module 1 tests green + interview ≥ 7 | 🟡 active |
| 2 | 🌱 Ch 3 interview re-answer (L1-L5) | `chapters/03-beans-and-the-application-context/interview-answer.md` | ~25 min | graded ≥ 7 (now 5/10) | 🔴 open |
| 3 | 🌱 Ch 3 checks (L6-L8) | chat, letters only | 3 min | 3 answers | ⬜ |
| 4 | 🌱 Chapter 4 onward | each chapter's `TASKS.md` (table below) | — | — | ⏸️ paused |

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

## 📚 All chapters (start each chapter with its `TASKS.md`, then the README)

- Chapters 4+ are open: run their checks with `-Popen`, e.g. `mvn -pl chapters/04-dependency-injection test -Popen`. A plain root `mvn test` skips them and stays green.

| Chapter | Title | Status | Link |
|---|---|---|---|
| 1 | Maven and the pom.xml | ✅ 19/19 · 7/10 | [TASKS](chapters/01-maven-and-the-pom/TASKS.md) |
| 2 | Your first Spring Boot app | ✅ 19/19 · 7/10 | [TASKS](chapters/02-first-spring-boot-app/TASKS.md) |
| 3 | Beans and the application context | 🔴 code 15/15 · interview 5/10 | [TASKS](chapters/03-beans-and-the-application-context/TASKS.md) |
| 4 | Dependency injection | ⬜ paused | [TASKS](chapters/04-dependency-injection/TASKS.md) |
| 5 | Configuration and profiles | ⬜ paused | [TASKS](chapters/05-configuration-and-profiles/TASKS.md) |
| 6 | How auto-configuration works | ⬜ paused | [TASKS](chapters/06-how-auto-configuration-works/TASKS.md) |
| 7 | The bean lifecycle | ⬜ paused | [TASKS](chapters/07-the-bean-lifecycle/TASKS.md) |
| 8 | How Spring handles requests | ⬜ paused | [TASKS](chapters/08-how-spring-handles-requests/TASKS.md) |
| 9 | Requests and responses | ⬜ paused | [TASKS](chapters/09-requests-and-responses/TASKS.md) |
| 10 | Layers and DTOs | ⬜ paused | [TASKS](chapters/10-layers-and-dtos/TASKS.md) |
| 11 | Updating and deleting | ⬜ paused | [TASKS](chapters/11-updating-and-deleting/TASKS.md) |
| 12 | Validating input | ⬜ paused | [TASKS](chapters/12-validating-input/TASKS.md) |
| 13 | Handling errors | ⬜ paused | [TASKS](chapters/13-handling-errors/TASKS.md) |
| 14 | Filters and interceptors | ⬜ paused | [TASKS](chapters/14-filters-and-interceptors/TASKS.md) |
| 15 | Plain SQL with JdbcClient | ⬜ paused | [TASKS](chapters/15-plain-sql-with-jdbcclient/TASKS.md) |
| 16 | JPA entities | ⬜ paused | [TASKS](chapters/16-jpa-entities/TASKS.md) |
| 17 | Repositories and query methods | ⬜ paused | [TASKS](chapters/17-repositories-and-query-methods/TASKS.md) |
| 18 | Pagination and sorting | ⬜ paused | [TASKS](chapters/18-pagination-and-sorting/TASKS.md) |
| 19 | Projections and aggregations | ⬜ paused | [TASKS](chapters/19-projections-and-aggregations/TASKS.md) |
| 20 | Entity relationships | ⬜ paused | [TASKS](chapters/20-entity-relationships/TASKS.md) |
| 21 | Cascading and transactions | ⬜ paused | [TASKS](chapters/21-cascading-and-transactions/TASKS.md) |
| 22 | The persistence context | ⬜ paused | [TASKS](chapters/22-the-persistence-context/TASKS.md) |
| 23 | Fetching and the N+1 problem | ⬜ paused | [TASKS](chapters/23-fetching-and-the-n-plus-1-problem/TASKS.md) |
| 24 | Spring Security basics | ⬜ paused | [TASKS](chapters/24-spring-security-basics/TASKS.md) |
| 25 | Users from the database | ⬜ paused | [TASKS](chapters/25-users-from-the-database/TASKS.md) |
| 26 | JWT fundamentals | ⬜ paused | [TASKS](chapters/26-jwt-fundamentals/TASKS.md) |
| 27 | JWT login and the JWT filter | ⬜ paused | [TASKS](chapters/27-jwt-login-and-the-jwt-filter/TASKS.md) |
| 28 | Roles and authorities | ⬜ paused | [TASKS](chapters/28-roles-and-authorities/TASKS.md) |
| 29 | Method security | ⬜ paused | [TASKS](chapters/29-method-security/TASKS.md) |
| 30 | Access and refresh tokens | ⬜ paused | [TASKS](chapters/30-access-and-refresh-tokens/TASKS.md) |
| 31 | Security errors | ⬜ paused | [TASKS](chapters/31-security-errors/TASKS.md) |
| 32 | Caching | ⬜ paused | [TASKS](chapters/32-caching/TASKS.md) |
| 33 | Transactions and locking | ⬜ paused | [TASKS](chapters/33-transactions-and-locking/TASKS.md) |
| 34 | AOP | ⬜ paused | [TASKS](chapters/34-aop/TASKS.md) |
| 35 | Calling other services with RestClient | ⬜ paused | [TASKS](chapters/35-calling-other-services-with-restclient/TASKS.md) |
| 36 | Actuator | ⬜ paused | [TASKS](chapters/36-actuator/TASKS.md) |
| 37 | Events and scheduling | ⬜ paused | [TASKS](chapters/37-events-and-scheduling/TASKS.md) |
| 38 | Project: the transfers service | ⬜ paused | [TASKS](chapters/38-project-the-transfers-service/TASKS.md) |
| 39 | Project: secure transfers | ⬜ paused | [TASKS](chapters/39-project-secure-transfers/TASKS.md) |
| 40 | Interview: Spring core | ⬜ paused | [TASKS](chapters/40-interview-spring-core/TASKS.md) |
| 41 | Interview: data and web | ⬜ paused | [TASKS](chapters/41-interview-data-and-web/TASKS.md) |
