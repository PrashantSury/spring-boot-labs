# Chapter 22 · The persistence context

**Time:** ~50 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter22CheckTest` 11/11 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/22-the-persistence-context test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/22-the-persistence-context spring-boot:run` (H2 in memory, no Docker).
- Fresh stub: 1/11 green (changing to a free number works; must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@Transactional` on `rename`, no `save` | `src/main/java/ledgerly/AccountService.java` | `mvn -pl chapters/22-the-persistence-context test -Popen` | task 1 checks green | ⬜ |
| 2 | `@Transactional(readOnly = true)` on `loadTwiceIsSameObject` | `AccountService.java` | same | task 2 checks green | ⬜ |
| 3 | `updateDetached` → `accounts.save(detached)` | `AccountService.java` | same | task 3 checks green | ⬜ |
| 4 | `accounts.flush()` inside the `try` | `AccountService.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** states, dirty checking, cache, merge, flush | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Entity states; why no `save` for a managed entity?
- First-level cache?
- Detached, `merge`/`save`; when is the flush?

## ⚠️ Traps

- Use what `merge` returns.
- Constraint errors may come at commit.
- `readOnly` = no dirty checking.

## ❓ Checks (letters only)

1. In a `@Transactional` method you load ACC-1 and call `setOwner("X")`, no `save`. At commit: A. nothing · B. one `UPDATE`
2. `findById(1)` twice in one transaction sends: A. 1 select · B. 2 selects
3. `Account m = repo.save(detached); detached.setOwner("Y");` in the same transaction. "Y" is: A. saved · B. not saved

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "Why did my update not save?" and "why did my update save when I didn't ask?" are the two most common JPA questions on Stack Overflow, and interviewers use entity states to see whether you know what happens below the repository.

**The concept.** The persistence context is a per-transaction map from id to entity plus a snapshot of each entity as loaded. At flush Hibernate compares objects with snapshots and writes the differences.

**Small example.** Sample data: ACC-1 Asha Rao, ACC-2 Vikram Shah. Real results:

| Code | State of the object | SQL sent | Saved? | Why |
|---|---|---|---|---|
| `rename` with `@Transactional` | managed | select + update | ✅ | dirty checking at commit |
| `rename` without a transaction | detached right after load | select only | ❌ | no context left to notice the change |
| `findById(1)` × 2 in one tx | same managed object | 1 select | — | first-level cache |
| change a detached copy, `save(copy)` | copy stays detached, a managed twin is returned | select + update | ✅ | merge copies the state onto the managed twin |
| `changeNumber` with `flush()` | managed | update inside the `try` | — | the unique error is caught and translated |

**Gotchas**
- A JPQL query in the same transaction triggers an automatic flush first (so it sees your changes).
- `@Transactional` tests roll back, so they never flush: a constraint bug can hide in a test that passes.
- `getReferenceById` returns a proxy without a `select`; touching a field later loads it.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** Dirty checking writes the change.
2. **A.** The second call is served from the context.
3. **B.** `detached` is still detached; only `m` is managed.

</details>
