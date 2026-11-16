# Chapter 1: Maven and the pom.xml

By the end of this chapter you can read and write a Spring Boot `pom.xml`: its coordinates, its parent, its dependencies and scopes, and a profile. You can also say who picks a library's version.

## Why Maven matters

Even though a Spring Boot app needs dozens of libraries, Maven lets you describe them in one file. You say what you need; Maven fetches the jars, including the jars *those* jars need, and builds the same way on every machine.

Ledgerly Bank is starting a new service for accounts and transfers. Meera, the tech lead, opens the repo and finds three things: a `pom.xml` copied from an old project, jars with five different versions of the same library, and a build that "works on Rohan's laptop". Nobody knows who chose which version. One bad upgrade last quarter broke the nightly statement job.

With plain `javac` you would download each jar by hand and add it to the classpath yourself. That is how you end up with Rohan's laptop and five versions of the same library.

## What you will learn

- **Coordinates:** `groupId : artifactId : version` is the name of a project or library.
- **The parent:** `spring-boot-starter-parent` hands you tested versions and build defaults.
- **Dependencies and scopes:** `compile`, `runtime`, `test`, `provided` and `import`, and when each one is right.
- **Profiles:** one `pom.xml`, different settings for different environments.

## Coordinates

Every Maven project and every library has a three-part name: who made it, what it is called, and which release it is.

Here are Ledgerly Core's coordinates:

```xml
<groupId>com.ledgerly</groupId>      <!-- who made it -->
<artifactId>ledgerly-core</artifactId> <!-- what it is called -->
<version>0.1.0-SNAPSHOT</version>    <!-- which release -->
```

Read them as `com.ledgerly : ledgerly-core : 0.1.0-SNAPSHOT`.

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

Here is why each of those lines appears, and what is left out:

- `ledgerly-core:0.1.0-SNAPSHOT` is the project itself. Those are the coordinates you set.
- `spring-boot-starter-webmvc:4.1.2:compile` is there because you declared that starter. You did not write `4.1.2` or `compile`. The parent filled the version. Scope defaults to `compile`.
- `logback-classic:1.5.38` is there even though you never declared Logback. The web starter pulls in the logging starter, and that pulls in Logback. The parent chose `1.5.38`.
- `tomcat-embed-core:11.0.27:compile` is there because the web starter needs a server. In this chapter it is `compile`, not `provided`, because a Spring Boot jar ships its own embedded Tomcat so the app can start on its own.
- `spring-webmvc:7.0.10` is the Spring Framework jar that matches Boot `4.1.2`. You never typed `7.0.10` either.
- `spring-boot-starter-webmvc-test:4.1.2:test` is there because you declared it with scope `test`. The `:test` at the end is that scope at work. It will not go into the jar you ship.
- `h2:2.4.240:runtime` is there because you declared H2 with scope `runtime`. The parent knew the version. `:runtime` means the compiler does not need it, but the running app does.

Nothing in this tree has a version you typed. That is the point of the parent.

### `import` scope: a BOM when you can't use the parent

Many banks have their own company parent pom, and a Maven project can have only one parent. Then you import Spring Boot's version list instead. That list is a BOM, a bill of materials, published as `spring-boot-dependencies`:

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

`import` is only for this case, inside `<dependencyManagement>`. It is not a classpath scope like `compile` or `test`.

## Java 21

The parent also sets build defaults, including which Java release the compiler uses. Its default is 17. Ledgerly runs on 21, so you tell the parent through a property it already understands:

```xml
<properties>
  <java.version>21</java.version>
</properties>
```

There is already a `<properties>` block in the lab pom. Edit the `17` in it. Don't add a second block.

Ask Maven what the compiler release became, after inheritance:

```bash
mvn -q -f chapters/01-maven-and-the-pom/lab/pom.xml help:evaluate -Dexpression=maven.compiler.release -DforceStdout
```

On the finished pom, the output is:

```
21
```

The parent read `java.version`, set `maven.compiler.release` to the same number, and that is what you just printed.

On the stub, before there is a parent, the output is:

```
null object or invalid expression
```

Nothing has set `maven.compiler.release` yet, so Maven has no value to print. The property only works once the parent is in place.

## Scopes: where does the jar go?

A scope answers three questions about a jar: can you compile main code against it, can tests see it, and does it go into the jar you ship?

To illustrate this, imagine four libraries Ledgerly might use:

| Library | Why Ledgerly needs it | Scope |
|---|---|---|
| `spring-boot-starter-webmvc` | Account HTTP APIs. Your code imports Spring types. | `compile` (default) |
| The servlet API, when you deploy a WAR onto a bank server that already has Tomcat | You compile against it, but the server already provides it. | `provided` |
| `h2` | An in-memory database so the app can run. Your code never imports H2 classes. | `runtime` |
| `spring-boot-starter-webmvc-test` | JUnit, AssertJ, Mockito, MockMvc. Only tests need them. | `test` |

This is where each scope is included, and where it is left out:

| Scope | On the compile classpath? | On the test classpath? | In the jar you ship? | Example |
|---|---|---|---|---|
| `compile` (default) | yes | yes | yes | `spring-boot-starter-webmvc` |
| `provided` | yes | yes | no | the servlet API in a WAR; the server gives it to you |
| `runtime` | no | yes | yes | `h2`, a JDBC driver: needed to run, never imported by your code |
| `test` | no | yes | no | `spring-boot-starter-webmvc-test`: JUnit, AssertJ, Mockito, MockMvc |
| `import` | n/a | n/a | n/a | only inside `<dependencyManagement>`, to import a BOM (above) |

Walk through the grid one row at a time:

- **`compile`** is included everywhere. Your main code imports it, your tests use the app, and the running service needs it. That is why the web starter in the tree above says `:compile`.
- **`provided`** is included for compile and for tests, because your code and tests import those types. It is excluded from the jar you ship, because the server already has it. If you packaged it anyway, you could clash with the server's own copy.
- **`runtime`** is excluded from the compile classpath, because your Java files never `import` it. It is included for tests and in the packaged jar, because the app has to run, including during tests. If you tried to `import org.h2...` in Ledgerly Core, the compile would fail. Look at the tree: h2 says `:runtime`.
- **`test`** is excluded from main compile and from the jar you ship. It is included only on the test classpath. JUnit must not go to production. In the tree, the test starter says `:test`.

The usual mix-up is `runtime` and `provided`. They fail in opposite places. `runtime` is missing at compile time and *is* packaged. `provided` is available at compile time and *is not* packaged.

One more catch, like the JOIN examples that only look the same until a row is missing: in *this* chapter's tree, Tomcat is `compile`, because the Spring Boot jar embeds it. `provided` would be right if you deployed a WAR onto a Tomcat the bank already runs. Don't mark Tomcat `provided` here just because you saw that example.

## When two libraries want different versions

Maven's rule is **nearest wins**: the version closest to your project in the tree is the one used. If both are the same distance, the one declared first wins.

Here `commons-fileupload` wants `commons-io 2.2`, and the project asks for `commons-io 2.15.1` directly. This is real output from `mvn dependency:tree -Dverbose`:

```
demo:conflict:jar:1
+- commons-fileupload:commons-fileupload:jar:1.4:compile
|  \- (commons-io:commons-io:jar:2.2:compile - omitted for conflict with 2.15.1)
\- commons-io:commons-io:jar:2.15.1:compile
```

Why each line turns out this way:

- `commons-io:2.15.1` is included. It is declared by the project itself, so it is one step away.
- `commons-io:2.2` is excluded. It sits under `commons-fileupload`, so it is two steps away. Nearest wins, and 2.15.1 is nearer. Maven tells you this with `omitted for conflict with 2.15.1`.

When a surprise version appears at runtime, `dependency:tree` is the first place to look.

## Profiles

A profile is a block of settings that applies only when you switch it on.

Ledgerly needs a UAT run that starts Spring Boot with its `uat` profile:

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

Run `mvn spring-boot:run -Puat` and Spring Boot starts with its own `uat` profile.

`-Puat` turns on the *Maven* profile named `uat`. That profile sets the property `spring-boot.run.profiles` to `uat`, and that property is what Spring Boot reads. Maven profiles and Spring profiles are two different things. This property is the bridge between them. Chapter 5 goes deeper into Spring's side.

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

The checks are in `Chapter01CheckTest`. They read `lab/pom.xml` and are grouped as Task 1 through Task 6. Read the failure message. It says what was expected and what was found.

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

## Check yourself

1. Who picks the version of `spring-boot-starter-webmvc` when you do not write a `<version>`?

    - A. Maven guesses the latest release on the internet
    - B. The build fails until you type a version
    - C. `spring-boot-starter-parent`'s dependency management
    - D. The `artifactId` of the dependency

2. Ledgerly needs H2 to run, but your code never imports H2 classes. Which scope is right, and what does that mean?

    - A. `test` — on the test classpath only, and not packaged
    - B. `runtime` — not on the compile classpath, but on the test classpath and in the packaged jar
    - C. `provided` — on the compile and test classpaths, but not packaged
    - D. `compile` — on every classpath and in the packaged jar

3. You deploy Ledgerly as a WAR onto a Tomcat the bank already runs. You compile against the servlet API, but you must not ship it. Which scope is that, and how is it different from `runtime`?

    - A. `provided`: available at compile and test time, not packaged. `runtime` is the opposite: not available at compile time, but packaged.
    - B. `runtime`: available at compile and test time, not packaged. `provided` is packaged but not on the compile classpath.
    - C. `test`: it stays out of the shipped jar, which is all that matters
    - D. `compile`: if you compile against it, it must also be packaged

<details>
<summary>Answers</summary>

1. **C.** The parent’s dependency management supplies the tested version. Maven does not guess, and a missing version is correct once the parent is in place.

2. **B.** `runtime` is for jars the app needs to run but your code never imports. Tests also need it so the app can start. It is not on the compile classpath, and it is packaged.

3. **A.** `provided` and `runtime` fail in opposite places. `provided` is on the compile and test classpaths and is left out of the jar, because the server already has it. `runtime` is left out of compile and is included in the jar.

</details>
