# Chapter 22: The persistence context

By the end of this chapter you can explain, and use, what Hibernate does between "load" and "commit": entity states, dirty checking, the first-level cache, merging a detached entity, and when a flush happens.

## Why you need this

Two Ledgerly code reviews went wrong in opposite ways. One service changed an account's owner and never called `save`; the reviewer said "this does nothing", and it worked. Another took an `Account` back from an edit form, changed it, and the change silently vanished. And a "number is taken" check never fired because the database error arrived after the method had already returned. All three are the persistence context.

## What you will learn

- **Entity states:** new, managed, detached, removed.
- **Dirty checking:** a managed entity's changes become an `UPDATE` at flush, no `save` needed.
- **First-level cache:** same id in one transaction → same object, one `select`.
- **`save` = `persist` or `merge`:** merge copies a detached entity onto a managed one and returns the copy.
- **Flush:** at commit (or before a query) by default; `flush()`/`saveAndFlush()` forces it now.

## Real output

`mvn -q -pl chapters/22-the-persistence-context spring-boot:run` on the finished code:

```
== 1. rename ACC-1 (no save call)
Hibernate: select a1_0.id,a1_0.number,a1_0.owner from account a1_0 where a1_0.number=?
Hibernate: update account set number=?,owner=? where id=?
owner now: Asha R. Kulkarni
== 2. load id 1 twice
Hibernate: select a1_0.id,a1_0.number,a1_0.owner from account a1_0 where a1_0.id=?
same object? true
== 3. change a detached copy
Hibernate: select a1_0.id,a1_0.number,a1_0.owner from account a1_0 where a1_0.id=?
Hibernate: update account set number=?,owner=? where id=?
returned the same object? false
owner now: Vikram S. Shah
== 4. change ACC-2 to ACC-1
Hibernate: update account set number=?,owner=? where id=?
caught DuplicateAccountException: Account number ACC-1 is taken
```

The same tour on the stub (real):

```
owner now: Asha Rao                         <- change lost: no transaction, entity detached at once
same object? false                          <- two persistence contexts, two selects
returned the same object? true / owner now: Vikram Shah     <- detached change never merged
caught DataIntegrityViolationException: could not execute statement [Unique index or primary key violation ...
```

## Try it

```bash
mvn -q -pl chapters/22-the-persistence-context spring-boot:run
mvn -pl chapters/22-the-persistence-context test -Popen
```

## Your turn

You edit `AccountService.java` and `interview-answer.md`.

1. **Dirty checking.** `@Transactional` on `rename`; no `save`.
2. **First-level cache.** `@Transactional(readOnly = true)` on `loadTwiceIsSameObject`.
3. **Merge.** `updateDetached` returns `accounts.save(detached)`.
4. **Flush.** `accounts.flush()` inside the `try` of `changeNumber`.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- Calling `save()` on a managed entity is harmless but shows you don't trust dirty checking; forgetting the transaction is the real bug.
- `merge` returns the managed copy; keep using the returned object, not the one you passed.
- A constraint error can surface at commit, outside your `try/catch`.
- `readOnly = true` turns off dirty checking: changes made there are NOT saved.
- Huge loops in one transaction keep every entity in the context; `clear()` in batches.

**Interview question (task 5).** Entity states, and why a managed change needs no save? The first-level cache? Detached entities, merge/save, and when a flush happens? The questions are repeated at the top of `interview-answer.md`.
