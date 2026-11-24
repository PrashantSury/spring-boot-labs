# Chapter 26: JWT fundamentals

By the end of this chapter you have built a tiny HS256 JWT signer and verifier by hand, so you know exactly what is inside a Ledgerly login token, what the signature proves, and which checks a server must never skip.

## Why you need this

Ledgerly's mobile team proposed putting the customer's name, account list and card PIN into the login token "because JWTs are encrypted". They are not. Anyone who copies a token from a log file or a phone can read every claim with a base64 decoder. Before chapter 27 hands tokens to a library, you should be able to take one apart yourself.

## What you will learn

- **Three parts:** `header.payload.signature`, each base64url without padding.
- **HS256:** signature = HMAC-SHA256(secret, `header.payload`).
- **Claims:** `sub` (who), `iat` (issued at), `exp` (expires at), in epoch seconds.
- **Signed, not encrypted:** the payload is readable by anyone.
- **Must-do checks:** expected `alg` (never `none`), signature in constant time, `exp`.

## Real output

`JwtTour` on the finished code (fixed clock 2026-11-24 10:00 UTC, 15-minute token):

```
token      eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhc2hhIiwiaWF0IjoxNzk1NTE0NDAwLCJleHAiOjE3OTU1MTUzMDB9.04Bw8s2jwCTjt9RiFK86HVGVMwVjaFLzmnjMtEivEyY
header     {"alg":"HS256","typ":"JWT"}
payload    {"sub":"asha","iat":1795514400,"exp":1795515300}
verify     {sub=asha, iat=1795514400, exp=1795515300}
sub changed to ops -> rejected: bad signature
alg none -> rejected: unsupported alg
20 min later -> rejected: expired
```

The checks also sign the famous jwt.io example claims with `your-256-bit-secret` and expect the exact jwt.io token `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c`, byte for byte.

## Try it

```bash
mvn -q -pl chapters/26-jwt-fundamentals compile exec:java "-Dexec.mainClass=ledgerly.JwtTour"
mvn -pl chapters/26-jwt-fundamentals test -Popen
```

No Spring here: plain Java (`javax.crypto.Mac`, `Base64`) and Jackson for the JSON.

## Your turn

You edit `JwtCodec.java` and `interview-answer.md`. `issue`, `hmac`, `b64` and `part` are finished.

1. **`sign`.** `b64(header) + "." + b64(json) + "." + b64(hmac(header.payload))`.
2. **`signatureValid`.** Recompute and compare with `MessageDigest.isEqual`; not 3 parts → false.
3. **`verify`.** `alg` must be HS256 → signature → `exp` > now; the exact messages from the comment.
4. **`readUnverified`.** Decode the payload without any check.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- Base64 vs base64url: JWTs use `-` and `_` and no `=` padding.
- `Arrays.equals`/`String.equals` on signatures returns early at the first different byte (a timing leak). Use `MessageDigest.isEqual`.
- Trusting the token's own `alg` header: `alg: none` with an empty signature must be rejected before anything else.
- No `exp` check: a token stolen today works forever.
- A short HS256 secret can be brute-forced offline from one token; use ≥ 32 random bytes from configuration, never from code.

**Interview question (task 5).** The three parts, and how HS256 signs and verifies? Is a JWT encrypted, and what may go in the payload? Which checks must a server always do? The questions are repeated at the top of `interview-answer.md`.
