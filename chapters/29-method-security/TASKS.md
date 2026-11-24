# Chapter 29 · Method security

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter29CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter29CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `@EnableMethodSecurity`, `@PreAuthorize`, `@PostAuthorize` | not written yet | — | checks not written yet | ⬜ |
| 2 | SpEL: `#id == authentication.principal.id` | not written yet | — | checks not written yet | ⬜ |
| 3 | Custom permission checks as a bean | not written yet | — | checks not written yet | ⬜ |
| 4 | Testing method security with MockMvc and `@WithMockUser` | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- How do you make sure a customer only reads their own accounts? Why does a self-call skip `@PreAuthorize`?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Method security works through a proxy, so self-invocation skips it
- `@PreAuthorize` on a controller and on a service can both apply

## ❓ Checks (letters only)

1. `#accountId` in the expression refers to: A. a request header · B. the method parameter named `accountId`
2. A self-invocation of a `@PreAuthorize` method: A. is checked · B. skips the check
3. Ownership ("only my accounts") is best expressed with: A. a URL rule · B. method security with a bean check

<details><summary>📖 Teaching</summary>

**Why it matters.** A customer must see only their own accounts, which a URL rule cannot express.

**The concept.** Method security checks rules on a method call, using the arguments, which a URL rule can't do. `@PreAuthorize` runs before the method; `@PostAuthorize` checks the returned value.

**Small example.**

```java
@Component("ownership")
class Ownership {
    boolean owns(Authentication auth, Long accountId) { return repo.ownerOf(accountId).equals(auth.getName()); }
}

@PreAuthorize("hasRole('CUSTOMER') and @ownership.owns(authentication, #accountId)")
public AccountView get(Long accountId) { ... }
```

| Case | Result | In or out, and why |
|---|---|---|
| Customer `cust-7` asks for account 42 (theirs) | 200 | both parts of the rule pass |
| `cust-7` asks for account 43 (someone else's) | 403 | `owns` returns false |
| A teller (no `ROLE_CUSTOMER`) | 403 | the role part fails |
| Another method in the same class calls `this.get(43)` | the check is skipped ❌ | method security works through a proxy |
| Test with `@WithMockUser(username = "cust-7", roles = "CUSTOMER")` | runs as that customer | no token needed in the test |

**Gotchas**
- Method security works through a proxy, so self-invocation skips it.
- `@PreAuthorize` on a controller and on a service can both apply.

</details>

<details><summary>🔒 Answers</summary>

1. **B.** Parameters are available by name.
2. **B.** No proxy in between.
3. **B.** It needs the argument value.

</details>
