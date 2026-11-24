# Chapter 30: Access and refresh tokens

By the end of this chapter a Ledgerly customer logs in once a week, not once every 15 minutes: the app quietly swaps a long-lived refresh token for a fresh access token, each refresh token works exactly once, and a stolen one that is replayed shuts the whole session down.

## Why you need this

Chapter 27's tokens expire after 15 minutes. That is good for security and terrible for a mobile banking app. Making them last 30 days fixes the annoyance and turns every leaked token into a month-long key to someone's account. Two tokens give you both: a short one for every request, a long one that is only ever sent to `/auth/refresh` and can be revoked.

## What you will learn

- **Short access + long refresh:** what each one is for, and why the refresh token is random, not a JWT.
- **Storing refresh tokens:** a table of SHA-256 hashes with user and expiry.
- **Rotation:** every refresh revokes the old token and issues a new pair.
- **Reuse detection:** a revoked token coming back means theft → revoke the user's whole family.
- **Logout** for a stateless token, and what it does not do.

## Real output

The finished app on port 8095:

```
$ curl -H Content-Type:application/json -d '{"username":"asha","password":"asha-pass"}' localhost:8095/auth/login
{"accessToken":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhc2hhIiwiaWF0IjoxNzk1NTE3MTM0LCJ...","refreshToken":"1RtIdaQ2LobjiWNXXVzzB...","expiresIn":900}
$ curl -H Content-Type:application/json -d "{\"refreshToken\":\"$RT\"}" localhost:8095/auth/refresh
{"accessToken":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...","refreshToken":"<a new one>","expiresIn":900}
$ # the same old $RT again (a thief replaying it):
{"error":"invalid refresh token: reused"}
$ # now even the NEW refresh token is dead:
{"error":"invalid refresh token: reused"}

$ # fresh login, then logout:
logout 204
$ curl -H "Authorization: Bearer $AT" localhost:8095/me      # access token still valid
asha [ROLE_USER]
$ # refresh after logout:
{"error":"invalid refresh token: reused"}
```

## The shape of it

```java
TokenPair issue(String user) {
  String access = codec.issue(user, 900);
  String refresh = base64url(32 random bytes);
  store.save(refresh, user, now + 7 days);        // stores the SHA-256 hash
  return new TokenPair(access, refresh, 900);
}
TokenPair refresh(String rt) {
  Row row = store.find(rt) or "unknown";
  if (row.revoked()) { store.revokeAll(row.username()); throw "reused"; }
  if (expired) throw "expired";
  store.revoke(rt);
  return issue(row.username());
}
```

## Try it

```bash
mvn -pl chapters/30-access-and-refresh-tokens spring-boot:run
mvn -pl chapters/30-access-and-refresh-tokens test -Popen
```

On the stub `/auth/login` fails with `Task 1: TokenService.issue`.

## Your turn

You edit `TokenService.java` and `interview-answer.md`. The store, clock, filter and controllers are finished.

1. **Issue.** 15-minute JWT + 32-byte random refresh token, saved with a 7-day expiry.
2. **Refresh + rotate.** `unknown` / `expired`; revoke the old one; return a new pair.
3. **Reuse detection.** Revoked token again → `revokeAll(user)`, `reused`.
4. **Logout.** Revoke the refresh token.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- A refresh token that never expires is a permanent password.
- Storing refresh tokens in plain text: one database leak logs the attacker in as everyone.
- Refresh without rotation: a stolen refresh token works as long as the real one.
- Logout does not kill the access token; for that you need a short TTL or a deny-list.
- After logout, the old refresh token counts as "reused" here and revokes the rest of the user's tokens. For a logged-out session that is harmless, and some teams prefer a separate `logged_out` flag to tell the cases apart.

**Interview question (task 5).** Why access + refresh tokens? How do rotation and reuse detection work, and how do you store refresh tokens? What does logout mean for a stateless JWT? The questions are repeated at the top of `interview-answer.md`.
