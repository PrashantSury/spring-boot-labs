# Chapter 34 · AOP

**Time:** ~45 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter34CheckTest` 14/14 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/34-aop test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/34-aop spring-boot:run` (prints the proxy class, audit trail and timings).
- Fresh stub: 1/14 green (the `final` method example; must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@Around` timing of every `TransferService` method | `src/main/java/ledgerly/TimingAspect.java` | `mvn -pl chapters/34-aop test -Popen` | task 1 checks green | ⬜ |
| 2 | `@AfterReturning` audit line for `@Audited` | `AuditAspect.java` | same | task 2 checks green | ⬜ |
| 3 | `@AfterThrowing` audit line, exception not swallowed | `AuditAspect.java` | same | task 3 checks green | ⬜ |
| 4 | fix self-invocation in `payroll` | `TransferService.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** aspects, proxies, traps | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Aspect, pointcut, advice; when `@Around`?
- How Spring applies it; JDK vs CGLIB.
- Self-invocation and `final`; who else shares it?

## ⚠️ Traps

- `@Aspect` needs `@Component` too.
- `@Around` must return `proceed()` and let exceptions out.
- `this.transfer()` skips the proxy.

## ❓ Checks (letters only)

1. Which advice can change the return value or skip the method? A. `@AfterReturning` · B. `@Around`
2. `payroll` calls `this.transfer(...)` and `transfer` is `@Audited`. Audit lines written: A. one per transfer · B. none
3. Spring Boot advises a class with no interface using: A. a CGLIB subclass · B. a JDK dynamic proxy

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "How does `@Transactional` work?" is one of the most common Spring interview questions, and the honest answer is "AOP through a proxy". Knowing that explains half the "my annotation does nothing" bugs.

**The concept.** The bean you inject is not your object; it is a receptionist (the proxy) standing in front of it. Every call from outside goes to the receptionist first. The receptionist checks its list (pointcuts), runs the extra steps (advice: start a timer, write an audit line) and then passes the call on. A call your object makes to itself never passes the receptionist, so nothing extra happens.

**Small example.** Real results from the tests (ACC-1 = 1000, ACC-2 = 500, ACC-3 = 0):

| Call | Audit trail | `transfer` timed | Why |
|---|---|---|---|
| `transfer(ACC-1, ACC-2, 100)` | `OK transfer [ACC-1, ACC-2, 100] -> TX-1` | 1 | through the proxy |
| `transfer(ACC-1, ACC-2, 5000)` | `FAILED transfer [ACC-1, ACC-2, 5000]: insufficient funds in ACC-1` | 1 | `@AfterThrowing`; the caller still gets the exception |
| `balance(ACC-1)` | nothing | 0 (`balance` = 1) | not `@Audited`, but every method is timed |
| `payroll(ACC-1, {ACC-2: 10, ACC-3: 20})` with `this.transfer` | nothing | 0 | self-invocation |
| same, through `self.getObject()` | 2 OK lines, TX-1, TX-2 | 2 | through the proxy |
| `feeFor(100)` | nothing | 0 | `final`: no override, no advice |

**Gotchas**
- Bind annotation values by parameter name: `@annotation(audited)` + a parameter `Audited audited`.
- Order of several aspects: `@Order` on the aspect class; lower runs first (outermost).
- `ObjectProvider<Self>` or `@Lazy` self-injection fixes self-invocation; often moving the method to another bean is cleaner.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** Only `@Around` controls `proceed()`.
2. **B.** `this.` never reaches the proxy.
3. **A.** Boot sets `proxyTargetClass=true`; JDK proxies need an interface.

</details>
