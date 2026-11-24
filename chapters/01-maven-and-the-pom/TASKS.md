# Chapter 1 · Maven and the pom.xml

**Time:** ~60 min · **Status:** ✅ done (15 Nov, checks 19/19, interview 7/10) · **Done when:** `Chapter01CheckTest` 19/19 + interview ≥ 7/10

## ✅ Tasks

- All edits go in `chapters/01-maven-and-the-pom/lab/pom.xml` (each task has a `<!-- Task N -->` comment). Run from the repo root: `mvn -pl chapters/01-maven-and-the-pom test`.

| # | Task | File | Run | Done when | Status |
|---|---|---|---|---|---|
| 1 | Coordinates `com.ledgerly : ledgerly-core : 0.1.0-SNAPSHOT` | `lab/pom.xml` | `mvn -pl chapters/01-maven-and-the-pom test` | task 1 checks green | ✅ |
| 2 | Parent `spring-boot-starter-parent`, same version as the root pom, empty `relativePath` | `lab/pom.xml` | same | task 2 checks green | ✅ |
| 3 | `java.version` = 21 (edit the existing property) | `lab/pom.xml` | same | task 3 checks green | ✅ |
| 4 | **(Interview)** `spring-boot-starter-webmvc`, no version, no scope | `lab/pom.xml` | same | check green + answer ≥ 7 | ✅ 7/10 |
| 5 | `spring-boot-starter-webmvc-test` (test) and `h2` (runtime), no versions | `lab/pom.xml` | same | task 5 checks green | ✅ |
| 6 | Profile `uat` setting `spring-boot.run.profiles` to `uat` | `lab/pom.xml` | same | task 6 checks green | ✅ |

## 🎤 Interview (task 4)

- Who picks the version of `spring-boot-starter-webmvc` when you don't write one, and what happens to all your starters when you upgrade Spring Boot?
- Then: what is the difference between `runtime` and `provided`?

## ❓ Checks (letters only)

1. Who picks the starter's version when you write no `<version>`? A. Maven guesses the latest · B. the build fails · C. the parent's dependency management
2. H2 is needed to run, but your code never imports it. Scope? A. `test` · B. `runtime` · C. `provided`
3. A WAR on the bank's Tomcat: compile against the servlet API but don't ship it. Scope? A. `provided` · B. `runtime` · C. `compile`

<details><summary>📖 Teaching (recap)</summary>

**Why it matters.** One pom decides every library version in the app. Get the parent right and a Spring Boot upgrade is one number.

**The concept.** The parent's dependency management supplies tested versions, so starters need no `<version>`. A scope decides where a jar is: compile, test, packaged.

**Small example.**

```xml
<dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>runtime</scope></dependency>
```

| Scope | Compile classpath | Test classpath | In the packaged jar |
|---|---|---|---|
| `compile` | ✅ | ✅ | ✅ |
| `runtime` | ❌ | ✅ | ✅ |
| `provided` | ✅ | ✅ | ❌ |
| `test` | ❌ | ✅ | ❌ |

`runtime` and `provided` fail in opposite places: one is missing at compile time, the other is missing in the jar.

**Gotchas**
- `4.1` is not `4.1.2`; use three numbers.
- `<parent>` comes before your own `groupId`; `<profiles>` goes at the end.
- Adding a version to a starter breaks the point of the parent.

</details>

<details><summary>🔒 Answers</summary>

1. **C.** The parent's dependency management.
2. **B.** `runtime`: not on the compile classpath, packaged, on the test classpath.
3. **A.** `provided`: compile and test only, the server supplies it.

</details>
