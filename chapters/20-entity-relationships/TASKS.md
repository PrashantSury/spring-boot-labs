# Chapter 20 · Entity relationships

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter20CheckTest` 12/12 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/20-entity-relationships test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/20-entity-relationships spring-boot:run` (H2 in memory, no Docker).
- Fresh stub: 1/12 green (no join table yet; it must stay that way after task 2).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@ManyToOne(LAZY, optional = false)` + `@JoinColumn(name = "customer_id")` | `src/main/java/ledgerly/Account.java` | `mvn -pl chapters/20-entity-relationships test -Popen` | task 1 checks green | ⬜ |
| 2 | `@OneToMany(mappedBy = "customer")` | `Customer.java` | same | task 2 checks green | ⬜ |
| 3 | `addAccount` sets both sides | `Customer.java` | same | task 3 checks green | ⬜ |
| 4 | `@JsonIgnore` on the back-reference | `Account.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** owning side, mappedBy, JSON loop | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- One customer, many accounts: mapping and owning side?
- What does `mappedBy` do; why a helper method?
- Why does JSON loop; the fix?

## ⚠️ Traps

- No `mappedBy` → join table.
- Inverse side alone saves nothing.
- `@ManyToOne` defaults to EAGER.

## ❓ Checks (letters only)

1. Which table holds `customer_id`? A. `customer` · B. `account`
2. `customer.getAccounts().add(acc)` only, then persist `acc`. `customer_id` is: A. set · B. null
3. `@OneToMany` without `mappedBy` next to a `@ManyToOne`: A. one relationship · B. two, plus a join table

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** Every domain model has relationships, and "owning side vs inverse side" is the JPA question that separates people who copy annotations from people who can debug them. The JSON loop is the most common production crash in a first Spring REST app.

**The concept.** A foreign key lives in one table. The entity mapped to that table owns the relationship (`@ManyToOne` + `@JoinColumn`). The other side can mirror it with `mappedBy`, but JPA ignores the mirror when writing.

**Small example.** Sample data: Asha Rao owns ACC-1 (₹52,000) and ACC-2 (₹1,000). Real rows:

| account.id | number | customer_id |
|---|---|---|
| 1 | ACC-1 | 1 |
| 2 | ACC-2 | 1 |

| What you do in Java | What lands in the database | In or out | Why |
|---|---|---|---|
| `acc.setCustomer(asha)` | `customer_id = 1` | ✅ in | the owning side is written |
| `asha.getAccounts().add(acc)` only | nothing / `customer_id` null | ❌ out | the inverse side is never written |
| `asha.addAccount(acc)` | `customer_id = 1`, list updated too | ✅ in | both sides in sync, in memory and in the DB |
| `@OneToMany` without `mappedBy` | extra `customer_accounts` table | ❌ out | JPA thinks it is a second relationship |

**When to avoid.** `@OneToOne` on the inverse side cannot be lazy in Hibernate without tricks (it must check whether the row exists). `@ManyToMany` hides a join table you cannot add columns to; model it as two `@ManyToOne`s to a link entity (`AccountHolder` with a role).

**Gotchas**
- `optional = false` lets Hibernate skip a null check and makes the column `not null`.
- A lazy relationship read after the `EntityManager` is closed throws `LazyInitializationException` (chapter 23).
- `@JsonManagedReference`/`@JsonBackReference` is the other Jackson fix; DTOs are the cleanest.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** The "many" side holds the foreign key.
2. **B.** Only the owning side (`acc.customer`) is written.
3. **B.** Without `mappedBy` JPA maps it separately with a join table.

</details>
