# How to run the labs (IntelliJ IDEA on Windows)

## 1. Install once

1. **JDK 21.** Install Eclipse Temurin 21 (or any JDK 21). Check in a new terminal:
   ```
   java -version
   ```
   It must print `21`.
2. **Maven 3.9 or newer.** Download it from maven.apache.org, unzip it, and add its `bin` folder to `PATH`. Check:
   ```
   mvn -v
   ```
   It must show Maven 3.9+ and Java 21. If it shows another Java, set `JAVA_HOME` to the JDK 21 folder.
3. **Git**, and **IntelliJ IDEA** (Community is enough).
4. **curl** for chapters that call the app. Windows 10 and 11 already have `curl.exe`. In PowerShell type `curl.exe`, not `curl` (that one is an alias for something else).

## 2. Get the labs and open them

```
git clone https://github.com/PrashantSury/spring-boot-labs.git
cd spring-boot-labs
git checkout -b my-solutions
```

Working on your own branch means you can pull new chapters from `main` later without conflicts.

In IntelliJ: **File > Open**, choose the **root `pom.xml`** (not a chapter folder), click **Open as Project**, and **Trust Project**.

- **File > Project Structure > Project:** set the SDK to 21.
- Wait for the **Maven** tool window to finish importing. Every chapter shows up as a module.
- If IntelliJ offers "Load Maven Project", accept.

## 3. Run a chapter's checks

The checks are normal JUnit 5 tests. Two ways:

**In a terminal** (IntelliJ: View > Tool Windows > Terminal), from the repo root:

```
mvn -pl chapters/01-maven-and-the-pom test
mvn -pl chapters/02-first-spring-boot-app test
mvn -pl chapters/03-beans-and-the-application-context test
```

The first run downloads libraries and takes a minute. Later runs take a few seconds.

**In IntelliJ:** open `src/test/java/ledgerly/ChapterNNCheckTest.java` and click the green arrow next to the class name, then **Run**. The Run window shows each check as a line. Click a red line to read its message.

## 4. What the result means

- **Red:** the check failed. Read the message in square brackets. It names the task and says what is expected, for example `[task 2: the parent version] expected 4.1.2`. A fresh chapter is mostly red. That is the starting line, not a mistake.
- **Green:** the check passed. When **every** check in the class is green, the chapter is done.
- **Red with `[context] the application context did not start`** or **`ERROR` before any check:** something does not compile or start. Read the first error line. Usually a typo, a missing import, or a tag not closed.

Tip: the line `Tests run: 19, Failures: 18` at the end tells you how far you are.

## 5. Your loop for every chapter

1. Read the chapter `README.md`. Try the commands in **Try it**.
2. Open the stub file. Find the first `// Task N` comment.
3. Make the change. Run the checks. Read the message.
4. Repeat until green. Then answer the **(Interview)** question aloud, in your own words.
5. Commit on your `my-solutions` branch.

## 6. Running the app (chapter 2 on)

```
mvn -pl chapters/02-first-spring-boot-app spring-boot:run
```

Leave it running and open a second terminal:

```
curl.exe localhost:8081/status
curl.exe "localhost:8081/accounts/above?balance=10000"
```

Stop it with `Ctrl+C`. Chapter 2 prints `APPLICATION FAILED TO START` until task 6 is done. That is expected.

To run a small demo class (chapter 3):

```
mvn -q -pl chapters/03-beans-and-the-application-context compile "-Dexec.mainClass=ledgerly.BeanTour" exec:java
```

The quotes around `-Dexec.mainClass=...` matter in PowerShell.

## 7. Chapter 1 is special

You edit `chapters/01-maven-and-the-pom/lab/pom.xml`. It is a pom that is **not** part of the build, so a typo cannot stop the checks. The checks read that file and compare it with what is expected.

## 8. Problems

| Symptom | Fix |
|---|---|
| `release version 21 not supported` | Maven is using an older Java. Set `JAVA_HOME` to JDK 21, reopen the terminal. |
| `Port 8081 was already in use` | The app is still running in another window. Stop it, or find it with `netstat -ano | findstr 8081`. |
| `Could not resolve dependencies` | Check your internet or company proxy. Retry with `mvn -U ...`. |
| IntelliJ shows red imports but Maven works | Maven tool window > Reload All Maven Projects. |
| `Module not found` after `-pl` | Run the command from the repo root, the folder with the root `pom.xml`. |

## 9. Rules

- The repo has **no solutions**. If you are stuck for 15 minutes, read the failure message again, then the README section for that task.
- Do not edit the check files. Editing them hides the message that teaches you.
