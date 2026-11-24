# Chapter 30 · Access and refresh tokens

**Time:** ~60 min · **Status:** ⬜ paused (banking-app is the active Spring work) · **Done when:** `Chapter30CheckTest` 13/13 ✅ + interview ≥ 7/10

## ✅ Tasks

- Run from the repo root: `mvn -pl chapters/30-access-and-refresh-tokens test -Popen` (without `-Popen` the checks are skipped).
- App: `mvn -pl chapters/30-access-and-refresh-tokens spring-boot:run` (port 8095; `asha`/`asha-pass`, `ops`/`ops-pass`).
- Fresh stub: 0/13 green.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | `issue`: access JWT + random refresh, stored | `src/main/java/ledgerly/TokenService.java` | `mvn -pl chapters/30-access-and-refresh-tokens test -Popen` | task 1 checks green | ⬜ |
| 2 | `refresh`: unknown / expired / rotate | `TokenService.java` | same | task 2 checks green | ⬜ |
| 3 | reuse detection → `revokeAll` | `TokenService.java` | same | task 3 checks green | ⬜ |
| 4 | `logout` | `TokenService.java` | same | task 4 checks green | ⬜ |
| 5 | **(Interview)** access/refresh, rotation, logout | `interview-answer.md` | graded by Lumi | ≥ 7/10 | ⬜ |

## 🎤 Interview (task 5)

- Why a short access token plus a refresh token?
- Rotation, reuse detection, storage.
- Logout for a stateless JWT; a stolen access token.

## ⚠️ Traps

- Refresh tokens must expire.
- Store hashes only.
- Check `revoked` before `expired`.

## ❓ Checks (letters only)

1. Which token travels with every API call? A. access token · B. refresh token
2. A refresh token that was already used comes in again. Best reaction: A. issue a new pair anyway · B. revoke all the user's refresh tokens
3. After `/auth/logout`, the user's 15-minute access token: A. stops at once · B. works until it expires

<details><summary>📖 Teaching (read before task 1)</summary>

**Why it matters.** "How do you keep users logged in safely with JWTs?" and "How do you log out a JWT?" are standard follow-ups the moment you mention JWT in an interview.

**The concept.** Think of a hotel. The **access token** is your room key card: it opens the door on its own and expires at checkout, and nobody at the desk is asked. The **refresh token** is your booking slip: you only show it at the front desk, the desk looks it up in its book, takes the old slip away and hands you a new card and a new slip. If an old slip ever comes back to the desk, someone copied it, so the desk cancels the whole booking.

**Small example.** asha logs in at 10:00. Real results with the lab clock:

| Time | Call | Result | Why |
|---|---|---|---|
| 10:00 | login | access A1 (to 10:15), refresh R1 (7 days) | R1 stored as its hash |
| 10:20 | `/me` with A1 | 401 | access expired |
| 10:20 | refresh R1 | A2 + R2; R1 revoked | rotation |
| 10:21 | refresh R1 (thief) | 401 `reused`; R2 revoked too | reuse detection |
| 10:22 | refresh R2 (asha) | 401 `reused` | asha must log in again; so must the thief |
| +8 days | refresh an old R | 401 `expired` | refresh tokens die too |

**Gotchas**
- Keep the refresh token out of JavaScript (an `HttpOnly` cookie on the web, secure storage on mobile).
- Two parallel refreshes with the same token look like reuse; clients should serialise refreshes.
- A deny-list of access-token ids (`jti`) gives instant revocation at the cost of a lookup per request.

</details>

<details><summary>🔒 Answers (checks)</summary>

1. **A.** The refresh token goes only to `/auth/refresh`.
2. **B.** You cannot tell the thief from the user, so end the session for both.
3. **B.** Stateless means nobody is asked; it simply expires.

</details>
