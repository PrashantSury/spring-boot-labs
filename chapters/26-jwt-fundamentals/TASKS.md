# Chapter 26 · JWT fundamentals

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter26CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter26CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Header, payload and signature of a JWT | not written yet | — | checks not written yet | ⬜ |
| 2 | Signing and verifying with an HMAC secret | not written yet | — | checks not written yet | ⬜ |
| 3 | Claims, `exp`, `iat`, `sub` | not written yet | — | checks not written yet | ⬜ |
| 4 | What a JWT is not: it is signed, not encrypted | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- A JWT is signed, not encrypted. What does that protect, what doesn't it, and what must a verifier always check?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- Anyone can read the payload; never put secrets in it
- Always check `exp`, and reject the `none` algorithm

## ❓ Checks (letters only)

1. Can a client read a JWT's payload without the key? A. yes · B. no
2. A client edits the `role` claim. The server: A. accepts it · B. rejects it: the signature no longer matches
3. `exp` is: A. the issue time · B. the expiry time, in seconds since 1970

<details><summary>📖 Teaching</summary>

**Why it matters.** Mobile clients need to prove who they are on every call without a server session.

**The concept.** A JWT is `header.payload.signature`, each part Base64URL. The signature proves nobody changed the payload, but anyone can read it.

**Small example.**

```text
header   {"alg":"HS256","typ":"JWT"}
payload  {"sub":"cust-7","iat":1790922600,"exp":1790923500,"role":"CUSTOMER"}
token    eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjdXN0LTci....<signature>
```

| Case | Result | In or out, and why |
|---|---|---|
| Decode the middle part with any Base64 tool | the payload JSON, in plain text | signed, not encrypted |
| Change `"role":"CUSTOMER"` to `"ADMIN"` and resend | rejected: signature mismatch | the signature covers the payload |
| Token used after `exp` (900 s after `iat`) | rejected: expired | always check `exp` |
| Header `{"alg":"none"}`, no signature | must be rejected | accepting `none` means accepting any payload |
| Put the customer's PIN in a claim | anyone holding the token can read it ❌ | never put secrets in the payload |

**Gotchas**
- Anyone can read the payload; never put secrets in it.
- Always check `exp`, and reject the `none` algorithm.

</details>

<details><summary>🔒 Answers</summary>

1. **A.** It is only Base64URL.
2. **B.** Integrity, not secrecy.
3. **B.** `iat` is the issue time.

</details>
