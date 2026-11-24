# Chapter 26 · JWT fundamentals

**Time:** ~50 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter26CheckTest` 13/13 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/26-jwt-fundamentals test -Popen` (without `-Popen` the checks are skipped).
- Tour: `mvn -q -pl chapters/26-jwt-fundamentals compile exec:java "-Dexec.mainClass=ledgerly.JwtTour"`.
- Fresh stub: 0/13 green.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `sign`: header.payload.signature | `src/main/java/ledgerly/JwtCodec.java` | `mvn -pl chapters/26-jwt-fundamentals test -Popen` | task 1 checks green (incl. the jwt.io token) | ⬜ |
| 2 | `signatureValid` with `MessageDigest.isEqual` | `JwtCodec.java` | same | task 2 checks green | ⬜ |
| 3 | `verify`: alg → signature → exp | `JwtCodec.java` | same | task 3 checks green | ⬜ |
| 4 | `readUnverified` | `JwtCodec.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** parts, signed vs encrypted, checks | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Three parts; how HS256 signs and verifies?
- Encrypted? What may go in the payload?
- Which checks always?

## ⚠️ Traps

- base64url, no padding.
- Constant-time compare.
- Reject `alg: none`; always check `exp`.

## ❓ Checks (letters only)

1. Someone copies a Ledgerly JWT from a log. Without the secret they can: A. read the claims · B. nothing
2. They change `"sub":"asha"` to `"sub":"ops"` and keep the signature. The server: A. accepts · B. rejects (bad signature)
3. `exp` = 1795515300, now = 1795515300. The token is: A. valid · B. expired

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** JWTs carry identity in almost every modern banking API, and "is a JWT encrypted?" plus "what is the `none` attack?" are standard security interview questions.

**The concept.** The signature is a keyed hash of the first two parts. Changing any byte of header or payload changes the expected signature, and only someone with the secret can produce the right one. It proves integrity and origin, not secrecy.

**Small example.** Real token for asha (iat 10:00 UTC, exp 10:15 UTC):

| Part | Base64url | Decoded |
|---|---|---|
| header | `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9` | `{"alg":"HS256","typ":"JWT"}` |
| payload | `eyJzdWIiOiJhc2hhIiwiaWF0IjoxNzk1NTE0NDAwLCJleHAiOjE3OTU1MTUzMDB9` | `{"sub":"asha","iat":1795514400,"exp":1795515300}` |
| signature | `04Bw8s2jwCTjt9RiFK86HVGVMwVjaFLzmnjMtEivEyY` | 32 bytes of HMAC-SHA256 |

| Claim idea | In or out | Why |
|---|---|---|
| `sub`, `exp`, `iat`, roles | ✅ in | identifiers and lifetime; harmless if read |
| customer name | ⚠️ avoid | personal data, readable by anyone holding the token |
| PIN, password, card number | ❌ out | the payload is plain base64 |

**Gotchas**
- HS256 = one shared secret (sign and verify). RS256/ES256 = private key signs, public key verifies (good when other services only verify).
- Clock skew: real libraries allow a small leeway (30–60 s) on `exp`.
- A JWT cannot be "logged out" before `exp` without a server-side list (chapter 30).

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **A.** Base64 is not encryption.
2. **B.** The recomputed HMAC no longer matches.
3. **B.** The rule is `exp > now`; at `exp` it is expired.

</details>
