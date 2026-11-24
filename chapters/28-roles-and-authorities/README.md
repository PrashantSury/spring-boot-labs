# Chapter 28: Roles and authorities

By the end of this chapter Ledgerly's rules read like the bank's own policy: tellers open the cash drawer, report readers see reports, only customers with the `transfer:write` permission send money, and an admin gets everything a teller can do without being listed twice. The token itself carries those permissions.

## Why you need this

"Is logged in" is not a permission. Meera has an account but has not finished KYC, so she must not send transfers yet. Asha has. Both are `ROLE_USER`. Roles alone cannot tell them apart. A fine-grained authority (`transfer:write`) can.

## What you will learn

- **Roles vs authorities:** both are `GrantedAuthority` strings; a role is one starting with `ROLE_`.
- **`hasRole` vs `hasAuthority`** in the filter chain, and the prefix trap.
- **Authorities in the token:** a claim `"authorities": [...]`, read back by the filter without a database call.
- **Role hierarchy:** `ROLE_ADMIN > ROLE_TELLER > ROLE_USER`.

## Real output

The finished app on port 8093 (`/auth/demo-token` skips the password to keep the lab short; chapter 27 did the real login):

```
$ T=$(curl -s -H Content-Type:application/json -d '{"username":"ops"}' localhost:8093/auth/demo-token | jq -r .token)
$ echo $T | cut -d. -f2 | base64 -d
{"sub":"ops","authorities":["ROLE_ADMIN"],"iat":1795516834,"exp":1795517734}
$ curl -H "Authorization: Bearer $T" localhost:8093/me
ops [ROLE_ADMIN]
$ curl -H "Authorization: Bearer $T" localhost:8093/teller/cash-drawer
drawer: 2,40,000
$ curl -H "Authorization: Bearer $T" localhost:8093/reports/daily
{"status":403,"error":"Not allowed"}
$ curl -X POST -H "Authorization: Bearer $MEERA" -H Content-Type:application/json -d '{}' localhost:8093/transfers
{"status":403,"error":"Not allowed"}
```

The admin opens the teller's drawer (hierarchy) but not the report (an authority the hierarchy does not grant).

## The shape of it

```java
.requestMatchers(HttpMethod.POST, "/transfers/**").hasAuthority("transfer:write")
.requestMatchers("/teller/**").hasRole("TELLER")          // checks "ROLE_TELLER"

@Bean static RoleHierarchy roleHierarchy() {
  return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_TELLER\nROLE_TELLER > ROLE_USER");
}
```

## Try it

```bash
mvn -pl chapters/28-roles-and-authorities spring-boot:run
mvn -pl chapters/28-roles-and-authorities test -Popen
```

On the stub the token has no authorities, so `/me` shows `asha []` and every logged-in user reaches every endpoint.

## Your turn

You edit `TokenService.java`, `SecurityConfig.java` and `interview-answer.md`. The users, filter and controller are finished.

1. **Claim.** `issueFor` adds `"authorities"`: the sorted authority strings.
2. **Read back.** `TokenService.authorities(claims)` returns the list (empty when missing); the filter uses it.
3. **Rules.** `transfer:write`, `report:read`, `TELLER`, `ADMIN` as in the comment.
4. **Hierarchy.** The `RoleHierarchy` bean.
5. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- `hasRole("ROLE_ADMIN")`: Spring Security 6+ refuses it at startup; `hasAuthority("ADMIN")` silently never matches.
- Rule order: put specific matchers before `anyRequest()`; the first match wins.
- Authorities in a token are a snapshot: a removed permission lives on until the token expires. Keep access tokens short (chapter 30).
- The hierarchy expands roles only. `ROLE_ADMIN` does not imply `report:read` unless you write that line.

**Interview question (task 5).** Role vs authority and the `ROLE_` prefix. When `hasAuthority` instead of `hasRole`? What do you gain and lose by putting authorities in the JWT, and what does a role hierarchy do? The questions are repeated at the top of `interview-answer.md`.
