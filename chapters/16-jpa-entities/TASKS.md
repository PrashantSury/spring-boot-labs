# Chapter 16 · JPA entities

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter16CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter16CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@Entity`, `@Id`, `@GeneratedValue`, `@Column` | not written yet | — | checks not written yet | ⬜ |
| 2 | Why JPA needs a no-arg constructor, and records vs classes | not written yet | — | checks not written yet | ⬜ |
| 3 | `ddl-auto` options and Flyway as the real answer | not written yet | — | checks not written yet | ⬜ |
| 4 | `BigDecimal` for money and why never `double` | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Why `BigDecimal` and never `double` for money? Why `ddl-auto=validate` with Flyway in production?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- `equals`/`hashCode` on entities with generated ids
- Using an enum without `@Enumerated(STRING)` stores its ordinal

## ❓ Checks (letters only)

1. `0.1 + 0.2` as `double` prints: A. 0.3 · B. 0.30000000000000004
2. An enum field without `@Enumerated(EnumType.STRING)` is stored as: A. its name · B. its ordinal number
3. Why can't an entity be a Java record? A. records are slow · B. JPA needs a no-arg constructor and mutable fields

<details><summary>📖 Teaching</summary>

**Why it matters.** Writing the mapping from rows to `Account` objects by hand repeats everywhere; JPA does it from annotations.

**The concept.** An `@Entity` class maps to a table and each field to a column. JPA builds objects from rows using a no-arg constructor, so entities are classes, not records.

**Small example.**

```java
@Entity
class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 10) private String number;
    @Column(precision = 19, scale = 4) private BigDecimal balance;
    @Enumerated(EnumType.STRING) private AccountType type;     // SAVINGS, CURRENT
    protected Account() {}                                      // for JPA
}
```

| Case | Result | In or out, and why |
|---|---|---|
| `balance` as `BigDecimal`: 0.10 + 0.20 | 0.30 | exact decimal ✅ |
| `balance` as `double`: 0.1 + 0.2 | 0.30000000000000004 | binary floating point drifts ❌ |
| `type` without `@Enumerated(STRING)` | column stores 0 / 1 | the ordinal; reordering the enum corrupts data ❌ |
| `ddl-auto=update` in production | Hibernate changes the schema on its own ❌ | use Flyway + `validate` |

**Gotchas**
- `equals`/`hashCode` on entities with generated ids: the id is `null` until saved, so don't base them on it naively.
- Using an enum without `@Enumerated(STRING)` stores its ordinal.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** That drift is why money is `BigDecimal`.
2. **B.** Ordinal is the default.
3. **B.** Records are final and immutable.

</details>
