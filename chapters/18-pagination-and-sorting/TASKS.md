# Chapter 18 · Pagination and sorting

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter18CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter18CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `Pageable`, `Page`, `Slice` | not written yet | — | checks not written yet | ⬜ |
| 2 | `PageRequest.of` and `Sort` | not written yet | — | checks not written yet | ⬜ |
| 3 | Exposing `page`, `size` and `sort` in a controller | not written yet | — | checks not written yet | ⬜ |
| 4 | Counting queries and when a `Slice` is cheaper | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- `Page` vs `Slice`: what extra query does `Page` run, and when is it not worth it?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Sorting by a user-supplied property name must be checked against a whitelist
- Page numbers start at 0

## ❓ Checks (letters only)

1. The first page is: A. page=0 · B. page=1
2. Which avoids the count query? A. `Page` · B. `Slice`
3. A client-supplied sort property should be: A. passed straight through · B. checked against allowed fields

<details><summary>📖 Teaching</summary>

**Why it matters.** A customer with 20,000 transactions cannot be returned in one response.

**The concept.** Ask for one page of rows instead of all of them. `Pageable` carries page, size and sort; `Page` adds the total count, and `Slice` skips it.

**Small example.**

```java
@GetMapping("/accounts/{id}/transactions")
Page<TxnView> list(@PathVariable Long id, @RequestParam(defaultValue = "0") int page) {
    Pageable p = PageRequest.of(page, 20, Sort.by("postedAt").descending());
    return repo.findByAccountId(id, p).map(TxnView::of);
}
```

| Case | Result | In or out, and why |
|---|---|---|
| 20,000 transactions, `page=0` | 20 newest rows, `totalElements` 20000, `totalPages` 1000 | pages start at 0 |
| `page=1` | rows 21-40 | the second page |
| Return `Slice` instead of `Page` | no `COUNT(*)` query; only `hasNext` | cheaper for "load more" |
| `sort` taken straight from the client, e.g. `sort=pin` | sorts by a hidden column, or 500 for an unknown one ❌ | check it against a whitelist |

**Gotchas**
- Sorting by a user-supplied property name must be checked against a whitelist.
- Page numbers start at 0.

</details>

<details><summary>🔒 Answers</summary>

1. **A.** Spring Data pages are zero-based.
2. **B.** A `Slice` only knows if there is a next one.
3. **B.** Otherwise clients can sort by fields you never meant to expose.

</details>
