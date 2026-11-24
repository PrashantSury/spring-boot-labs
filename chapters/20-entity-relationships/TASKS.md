# Chapter 20 · Entity relationships

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter20CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter20CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@ManyToOne`, `@OneToMany`, `mappedBy` and the owning side | not written yet | — | checks not written yet | ⬜ |
| 2 | `@OneToOne` and `@ManyToMany` and when to avoid them | not written yet | — | checks not written yet | ⬜ |
| 3 | Bidirectional helper methods | not written yet | — | checks not written yet | ⬜ |
| 4 | JSON loops when an entity points back at itself | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- What is the owning side of a relationship, and what goes wrong if you update only the inverse side?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Without `mappedBy`, JPA creates an extra join table
- Serialising a bidirectional entity gives infinite recursion

## ❓ Checks (letters only)

1. In `Customer`–`Account`, the owning side is: A. `Customer` (the `@OneToMany`) · B. `Account` (the `@ManyToOne` with the foreign key)
2. `@OneToMany` without `mappedBy` creates: A. nothing extra · B. a join table
3. Why a helper like `addAccount`? A. style · B. it keeps both sides of the link in sync in memory

<details><summary>📖 Teaching</summary>

**Why it matters.** A customer has many accounts and each account has many transactions; the model must say so.

**The concept.** Relationships say how tables link: many accounts belong to one customer (`@ManyToOne`). In a two-way link, one side owns the foreign key and the other says `mappedBy`.

**Small example.**

```java
@Entity class Customer {
    @OneToMany(mappedBy = "customer") private List<Account> accounts = new ArrayList<>();
    void addAccount(Account a) { accounts.add(a); a.setCustomer(this); }   // keep both sides in sync
}
@Entity class Account {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customer_id") private Customer customer;
}
```

| Case | Result | In or out, and why |
|---|---|---|
| As written | `account.customer_id` foreign key | `Account` is the owning side |
| `@OneToMany` without `mappedBy` | an extra `customer_accounts` join table ❌ | JPA thinks it is a second, separate relationship |
| `customer.getAccounts().add(a)` without setting `a.customer` | `customer_id` stays null ❌ | only the owning side is written |
| Return `Customer` as JSON | infinite recursion customer → accounts → customer … | serialise DTOs instead |

**Gotchas**
- Without `mappedBy`, JPA creates an extra join table.
- Serialising a bidirectional entity gives infinite recursion.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** The side with the foreign key owns it.
2. **B.** It is treated as its own relationship.
3. **B.** Only the owning side is saved, but both are read.

</details>
