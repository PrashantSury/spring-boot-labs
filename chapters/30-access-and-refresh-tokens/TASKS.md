# Chapter 30 · Access and refresh tokens

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter30CheckTest` all green + interview answer ≥ 7/10

> Not written yet: this chapter has no stub files and no `Chapter30CheckTest`. The tasks come from the [README](README.md)'s planned topics.

## ✅ Tasks

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Short access tokens and longer refresh tokens | not written yet | — | checks not written yet | ⬜ |
| 2 | Refresh endpoint and rotating tokens | not written yet | — | checks not written yet | ⬜ |
| 3 | Revoking: a stored refresh token table | not written yet | — | checks not written yet | ⬜ |
| 4 | Logout and what it really means for a stateless token | not written yet | — | checks not written yet | ⬜ |
| 5 | **(Interview)** see below | — | aloud, 60-90 s | ≥ 7/10 | ⬜ |

## 🎤 Interview

- Why two tokens? What does logout really do when the access token is stateless?
- Answer with a Ledgerly example, what goes wrong, and the fix.

## ⚠️ Traps (from the README)

- A refresh token that never expires is a permanent password
- A stolen access token is valid until it expires; plan for that

## ❓ Checks (letters only)

1. After logout, the current access token: A. stops working at once · B. works until it expires (unless you keep a deny-list)
2. Refresh-token rotation means: A. the same refresh token forever · B. each refresh returns a new one and invalidates the old
3. Where must refresh tokens be stored server-side? A. nowhere · B. a table, so they can be revoked

<details><summary>📖 Teaching</summary>

**Why it matters.** A 15-minute access token keeps risk low; a refresh token keeps customers from logging in all day.

**The concept.** A short access token (about 15 min) is sent on every call. A longer refresh token is used only to get a new pair, and the server stores it so it can be revoked.

**Small example.**

```text
POST /auth/login     -> {"accessToken":"A1 (15 min)","refreshToken":"R1 (7 days)"}
POST /auth/refresh   {"refreshToken":"R1"} -> {"accessToken":"A2","refreshToken":"R2"}   (R1 now revoked)
POST /auth/refresh   {"refreshToken":"R1"} -> ?
POST /auth/logout    {"refreshToken":"R2"} -> 204
```

| Case | Result | In or out, and why |
|---|---|---|
| Refresh with `R1` | new pair `A2` + `R2`; `R1` marked used | rotation |
| Refresh again with the old `R1` | 401, and revoke the whole family | reuse of a rotated token signals theft |
| Logout with `R2` | `R2` deleted or revoked; `A2` still works until it expires | a stateless access token can't be recalled |
| Refresh token with no expiry | a permanent password ❌ | always expire and store it |

**Gotchas**
- A refresh token that never expires is a permanent password.
- A stolen access token is valid until it expires; plan for that (short lifetime, or a deny-list).

</details>

<details><summary>🔒 Answers</summary>

1. **B.** It is self-contained.
2. **B.** Reuse can then be detected.
3. **B.** Revocation needs state.

</details>
