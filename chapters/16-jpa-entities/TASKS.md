# Chapter 16 · JPA entities

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter16CheckTest` 13/13 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/16-jpa-entities test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/16-jpa-entities spring-boot:run` (H2 in memory, no Docker).
- Fresh stub: 1/13 green (two unsaved accounts are not equal; must stay green).

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@Entity`, `@Table`, `@Id @GeneratedValue`, column rules, protected no-arg constructor | `src/main/java/ledgerly/Account.java` | `mvn -pl chapters/16-jpa-entities test -Popen` | task 1 checks green | ⬜ |
| 2 | `@Enumerated(EnumType.STRING)` | `Account.java` | same | task 2 checks green | ⬜ |
| 3 | `balance`: precision 19, scale 2, not null | `Account.java` | same | task 3 checks green | ⬜ |
| 4 | id-based `equals`, constant `hashCode` | `Account.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** entity, enum, money, equals | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- What makes a class an entity? Why a no-arg constructor?
- Why `EnumType.STRING`? Why `BigDecimal`?
- equals/hashCode with a generated id?

## ⚠️ Traps

- Enum without `STRING` stores the ordinal.
- Never `double` for money.
- hashCode must not change when the id is set.

## ❓ Checks (letters only)

1. `AccountType { SAVINGS, CURRENT }` with no `@Enumerated`. A CURRENT account's column holds: A. `CURRENT` · B. `1`
2. `hashCode()` returns `Objects.hash(id)`. New account in a `HashSet`, then `persist`. `set.contains(account)`: A. true · B. false
3. Which survives `0.10 + 0.20 == 0.30`? A. `double` · B. `BigDecimal` with `add`

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** Almost every Spring job uses JPA, and these three bugs (ordinal enums, `double` money, broken `hashCode`) are favourite interview questions precisely because they pass all the happy-path tests and break in production.

**The concept.** An entity is a class whose instances JPA can store as rows. The annotations tell Hibernate the table, the key and the column rules; Hibernate generates the SQL and turns rows back into objects through the no-arg constructor.

**Small example.** Sample data: one account `ACC-1`, Asha Rao, SAVINGS, ₹52,000.00, then deposits of 0.10 and 0.20. Real row after commit: `[ACC-1, SAVINGS, 52000.30]`.

| Mapping choice | What lands in the column | In or out | Why |
|---|---|---|---|
| `@Enumerated(EnumType.STRING)` | `SAVINGS` | ✅ in | readable, survives reordering the enum |
| no `@Enumerated` (default ORDINAL) | `0` | ❌ out | inserting a constant renumbers every row's meaning |
| `BigDecimal`, `precision 19, scale 2` | `52000.30` | ✅ in | exact decimal arithmetic |
| `double` | `52000.30000000001`-style drift | ❌ out | binary floating point can't hold 0.10 |
| `equals` on id, constant `hashCode` | — | ✅ in | stable in sets before and after persist |
| Lombok `@Data` | — | ❌ out | hashCode changes when id is set |

**Gotchas**
- `@Column(nullable = false)` only shapes the DDL Hibernate generates; it is not a Java null check (that's `@NotNull`, chapter 12).
- `ddl-auto=create-drop` wipes the data on every restart. Use it for labs and tests only.
- `IDENTITY` means Hibernate must insert immediately on `persist` to learn the id; `SEQUENCE` can batch inserts.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **B.** The default is `EnumType.ORDINAL`.
2. **B.** The set put it in the bucket for `hash(null)`, then looks in the bucket for `hash(1)`.
3. **B.** `new BigDecimal("0.10").add(new BigDecimal("0.20"))` is exactly `0.30`.

</details>
