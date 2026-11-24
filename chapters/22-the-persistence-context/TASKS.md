# Chapter 22 · The persistence context

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter22CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter22CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Managed, detached, new and removed entity states | not written yet | — | checks not written yet | ⬜ |
| 2 | Dirty checking and flush | not written yet | — | checks not written yet | ⬜ |
| 3 | First-level cache: same id, same object | not written yet | — | checks not written yet | ⬜ |
| 4 | `save` vs `saveAndFlush` vs `merge` behaviour | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Why did the database update when nobody called `save`? Name the four entity states.
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Changing an entity inside a transaction updates it without `save`
- Detached entities from a closed transaction need `merge` or a reload

## ❓ Checks (letters only)

1. Inside a transaction you change a loaded entity and never call `save`: A. nothing is written · B. it is written at flush
2. Two `findById(7)` calls in one transaction return: A. two different objects · B. the same object
3. An entity loaded in a finished transaction is: A. managed · B. detached

<details><summary>📖 Teaching</summary>

**Why it matters.** A field changes and nobody called `save`, yet the database updated; this chapter explains why.

**The concept.** Inside a transaction, JPA tracks every entity it loaded. At flush it compares them with their loaded state and writes the changes, even without `save` (dirty checking).

**Small example.**

```java
@Transactional
public void rename(Long id, String name) {
    Customer c = repo.findById(id).orElseThrow();
    c.setName(name);                 // no save() call
    Customer again = repo.findById(id).orElseThrow();
    System.out.println(c == again);  // first-level cache
}
```

| Case | Result | In or out, and why |
|---|---|---|
| Run `rename(7, "Asha R.")` | `UPDATE customer SET name = 'Asha R.' ...` at commit | managed entity + dirty checking |
| `c == again` | `true`, and one SELECT only | same id, same object, in one persistence context |
| Same code without `@Transactional` | no UPDATE ❌ | the entity is detached right after `findById` |
| Change a detached entity, then `repo.save(it)` | UPDATE via `merge` | detached objects need `merge` or a reload |

**Gotchas**
- Changing an entity inside a transaction updates it without `save`.
- Detached entities from a closed transaction need `merge` or a reload.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Dirty checking.
2. **B.** The first-level cache.
3. **B.** Its persistence context is closed.

</details>
