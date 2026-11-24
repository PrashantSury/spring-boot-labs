# Chapter 40 · Interview: Spring core

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter40CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter40CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Beans, scopes, lifecycle and injection | not written yet | — | checks not written yet | ⬜ |
| 2 | Auto-configuration and `@SpringBootApplication` | not written yet | — | checks not written yet | ⬜ |
| 3 | Profiles, properties and proxies | not written yet | — | checks not written yet | ⬜ |
| 4 | AOP and transactions in one clear story | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- In 90 seconds: explain how `@Transactional` works, using a transfer, and name two ways it silently doesn't apply.
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Answer with an example from the bank, not a definition
- Say what goes wrong, not only what works

## ❓ Checks (letters only)

1. A strong answer starts with: A. a textbook definition · B. the default behaviour plus a concrete example
2. Two ways `@Transactional` silently doesn't apply: A. self-invocation; a checked exception doesn't roll back · B. a public method; an unchecked exception
3. A prototype injected into a singleton is created: A. per call · B. once, when the singleton is built

<details><summary>📖 Teaching</summary>

**Why it matters.** Rehearse the questions that a senior Java interviewer asks about Spring core.

**The concept.** Rehearse Spring core answers as short stories from the bank: what Spring does by default, what goes wrong, and how you fixed it.

**Small example.**

```text
Q: "Is a singleton bean thread-safe?"
Weak:   "No."
Strong: "No. One TransferService serves every thread, so a field like runningTotal races.
         I keep beans stateless; shared totals go to the database with an atomic UPDATE."
```

| Case | Result | In or out, and why |
|---|---|---|
| Definition only | 4-5/10 | no example, no failure mode |
| Definition + bank example | 6-7/10 | shows you've used it |
| Example + what goes wrong + fix + trade-off | 8-10/10 | the senior answer |
| Topics to cover | beans, scopes, lifecycle, injection, auto-configuration, profiles, proxies, AOP + transactions | from Chapters 3-7, 21, 34 |

**Gotchas**
- Answer with an example from the bank, not a definition.
- Say what goes wrong, not only what works.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Then the failure and the fix.
2. **A.** Both are proxy and rollback-rule traps.
3. **B.** Use `ObjectProvider` for per-call instances.

</details>
