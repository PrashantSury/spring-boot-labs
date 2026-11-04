# Chapter 1: Maven and the pom.xml

By the end of this chapter you can read and write a Spring Boot `pom.xml`: its coordinates, its parent, its dependencies and scopes, and a profile. You can also say who picks a library's version.

## Why you need this

Ledgerly Bank is starting a new service for accounts and transfers. Meera, the tech lead, opens the repo and finds three things: a `pom.xml` copied from an old project, jars with five different versions of the same library, and a build that "works on Rohan's laptop". Nobody knows who chose which version. One bad upgrade last quarter broke the nightly statement job.

With plain `javac` you would download each jar by hand, add it to the classpath, and then find the jars *those* jars need. Maven does this from one file: you say what you need, it fetches everything and builds the same way on every machine.

## What you will learn

- **Coordinates:** `groupId : artifactId : version` is the name of a project or library.
- **The parent:** `spring-boot-starter-parent` hands you tested versions and build defaults.
- **Dependencies and scopes:** `compile`, `runtime`, `test`, `provided` and `import`, and when each one is right.
- **Profiles:** one `pom.xml`, different settings for different environments.

## Coordinates

Every Maven project and every library has three values:

```xml
<groupId>com.ledgerly</groupId>      <!-- who made it -->
<artifactId>ledgerly-core</artifactId> <!-- what it is called -->
<version>0.1.0-SNAPSHOT</version>    <!-- which release -->
```

`-SNAPSHOT` means "work in progress, may change under you". A release version, like `4.1.2`, never changes once published.

## The parent: who picks the version?

Look at this dependency:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
```

There is no `<version>`. Maven does not guess. It looks at the `<parent>` of your project:

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>4.1.2</version>
  <relativePath/>
</parent>
```

The Spring Boot parent contains a long list called **dependency management**: "if someone asks for this library without a version, use this one". The Spring Boot team tests those versions together. So **the parent picks the version**, and to upgrade Spring Boot you change one number, the parent's version. Every starter, Spring, Jackson, Tomcat and Hibernate move to the matching tested release.

`<relativePath/>` is empty on purpose. Without it, Maven first looks for a `pom.xml` in the folder above yours. Empty means "go to the repository".

Check it yourself. This is the *real* top of the tree for this chapter's finished pom (`mvn dependency:tree`):

```
com.ledgerly:ledgerly-core:jar:0.1.0-SNAPSHOT
+- org.springframework.boot:spring-boot-starter-webmvc:jar:4.1.2:compile
|  +- org.springframework.boot:spring-boot-starter:jar:4.1.2:compile
|  |  +- org.springframework.boot:spring-boot-starter-logging:jar:4.1.2:compile
|  |  |  +- ch.qos.logback:logback-classic:jar:1.5.38:compile
|  ...
|  +- org.springframework.boot:spring-boot-starter-tomcat:jar:4.1.2:compile
|  |  +- org.springframework.boot:spring-boot-starter-tomcat-runtime:jar:4.1.2:compile
|  |  |  +- org.apache.tomcat.embed:tomcat-embed-core:jar:11.0.27:compile
|  ...
|     \- org.springframework:spring-webmvc:jar:7.0.10:compile
+- org.springframework.boot:spring-boot-starter-webmvc-test:jar:4.1.2:test
...
\- com.h2database:h2:jar:2.4.240:runtime
```

You wrote three dependencies. Maven found about seventy jars. You never typed a version for any of them.

## Scopes: where does the jar go?

| Scope | On the compile classpath? | In the jar you ship? | Example |
|---|---|---|---|
| `compile` (default) | yes | yes | `spring-boot-starter-webmvc` |
| `runtime` | no | yes | `h2`, a JDBC driver: needed to run, never imported by your code |
| `test` | no (only for tests) | no | `spring-boot-starter-webmvc-test`: JUnit, AssertJ, Mockito, MockMvc |
| `provided` | yes | no | the server gives it to you, for example a servlet API in a WAR |
| `import` | n/a | n/a | only inside `<dependencyManagement>`, to import a BOM (below) |

Look at the tree above: the test starter says `:test` and h2 says `:runtime`. That is the scope at work.

### `import` scope: a BOM when you can't use the parent

Many banks have their own company parent pom, and a Maven project can have only one parent. Then you import Spring Boot's version list instead:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-dependencies</artifactId>
      <version>4.1.2</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

Same effect for versions. You lose the parent's plugin and compiler defaults, so you configure those yourself.

## When two libraries want different versions

Maven's rule is **nearest wins**: the version closest to your project in the tree is the one used. If both are the same distance, the one declared first wins. Here `commons-fileupload` wants `commons-io 2.2`, and the project asks for `commons-io 2.15.1` directly. This is real output from `mvn dependency:tree -Dverbose`:

```
demo:conflict:jar:1
+- commons-fileupload:commons-fileupload:jar:1.4:compile
|  \- (commons-io:commons-io:jar:2.2:compile - omitted for conflict with 2.15.1)
\- commons-io:commons-io:jar:2.15.1:compile
```

The direct dependency is one step away, so it beats the one that is two steps away. When a surprise version appears at runtime, `dependency:tree` is the first place to look.

## Profiles

A profile is a block of settings that applies only when you switch it on:

```xml
<profiles>
  <profile>
    <id>uat</id>
    <properties>
      <spring-boot.run.profiles>uat</spring-boot.run.profiles>
    </properties>
  </profile>
</profiles>
```

Run `mvn spring-boot:run -Puat` and Spring Boot starts with its own `uat` profile. (Maven profiles and Spring profiles are two different things. This property is the bridge between them. Chapter 5 goes deeper into Spring's side.)

## Try it

From the repo root. Safe to run at any time, on the stub too:

```bash
# print the tree of YOUR lab pom
mvn -f chapters/01-maven-and-the-pom/lab/pom.xml dependency:tree

# ask Maven what a value is, after inheritance
mvn -q -f chapters/01-maven-and-the-pom/lab/pom.xml help:evaluate -Dexpression=maven.compiler.release -DforceStdout
# finished pom prints: 21
# stub prints: null object or invalid expression (no parent yet, so nothing sets it)

# run the checks
mvn -pl chapters/01-maven-and-the-pom test
```

## Your turn

You edit **one file**: `chapters/01-maven-and-the-pom/lab/pom.xml`. Each task has a `<!-- Task N -->` comment where its change goes. Run the checks after each task. Red is normal at the start; the message tells you what is expected.

1. **Coordinates.** Change the project's own coordinates to `com.ledgerly : ledgerly-core : 0.1.0-SNAPSHOT`.
2. **Parent.** Add `spring-boot-starter-parent` as the parent, with exactly the version in the root `pom.xml` (three numbers) and an empty `relativePath`.
3. **Java 21.** Set the `java.version` property to `21`.
4. **(Interview)** Add the `spring-boot-starter-webmvc` dependency with no version and no scope.
5. **Scopes.** Add `spring-boot-starter-webmvc-test` with scope `test`, and `h2` (`com.h2database`) with scope `runtime`. No versions.
6. **Profile.** Add a `uat` profile whose own properties set `spring-boot.run.profiles` to `uat`.

**Traps**

- In task 1, change only the three lines directly under `<project>`, not anything inside `<parent>`.
- `<parent>` must come **before** your own `groupId`. Order matters to the check.
- `4.1` is not `4.1.2`. Use three numbers.
- There is already a `<properties>` block. Edit the `17` in it. Don't add a second block.
- Dependencies with no version are correct. If you add one, you broke the point of the parent.
- `<profiles>` goes at the **end** of `<project>`, and the property goes inside the profile, not in the main `<properties>`.
- A tag opened and not closed makes every check fail with an XML error.

**Interview question (task 4).** Write the answer in your own words, out loud, in two or three sentences: *Who picks the version of `spring-boot-starter-webmvc` when you don't write one, and what happens to all your starters when you upgrade Spring Boot?* Then add: *what is the difference between `runtime` and `provided`?*
