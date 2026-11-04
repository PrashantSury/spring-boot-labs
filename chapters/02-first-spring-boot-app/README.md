# Chapter 2: Your first Spring Boot app

By the end of this chapter you can start a Spring Boot app and answer HTTP requests with text and JSON, using values from the path and the query string. You will also see what Spring answers on its own: 404, 405, 400 and 500.

## Why you need this

The branch app, the mobile app and the partner payment gateway all need the same account data. Meera's rule: "One backend. Everyone asks it over HTTP." The first version should answer three things: is the bank open, what are the accounts, and what will this transfer cost.

With plain Java you would open a `ServerSocket`, read lines like `GET /accounts HTTP/1.1`, split the path, turn `"5000"` into a number, write headers, build the JSON string by hand, and make all of it safe for many requests at once. Every new URL is more of that plumbing.

## What you will learn

- **The application class:** `SpringApplication.run` and one annotation start a container and a web server.
- **Controllers:** `@RestController` and `@GetMapping` connect a URL to a method. The return value is the answer, as text or JSON.
- **Path variables and query parameters:** values from the URL, already converted to Java types.
- **What Spring does for free:** the 404, 405, 400 and 500 answers you never write.

## The application class

`main` calls `SpringApplication.run(LedgerlyApplication.class, args)`. That creates the **application context**, the container that makes and wires your objects (Spring calls them *beans*), and then starts an embedded Tomcat web server.

The class you pass decides what gets set up. Without the right annotation, this is the *real* output of `mvn spring-boot:run` on the stub in this repo:

```
***************************
APPLICATION FAILED TO START
***************************

Description:

Web application could not be started as there was no org.springframework.boot.web.server.servlet.ServletWebServerFactory bean defined in the context.

Action:

Check your application's dependencies for a supported servlet web server.
Check the configured web application type.
```

No annotation means no auto-configuration, so no web server bean. The annotation you need is one name that is really three annotations in one. Each switches on one thing:

| Annotation inside it | What it switches on |
|---|---|
| `@SpringBootConfiguration` | This class is a source of bean definitions (it is a `@Configuration`). |
| `@EnableAutoConfiguration` | Look at the libraries on the classpath and set up defaults. Spring MVC is there, so make a web server. |
| `@ComponentScan` | Find classes marked as components, such as a `@RestController`, in this package and below. That is why every file here starts with `package ledgerly;`. |

When it works, the log says so (real output):

```
Tomcat initialized with port 8081 (http)
Starting Servlet engine: [Apache Tomcat/11.0.27]
Tomcat started on port 8081 (http) with context path '/'
Started LedgerlyApplication in 0.787 seconds (process running for 0.949)
```

## A controller

`BranchController` in this chapter is a finished demo. It is **not** one of your tasks:

```java
@RestController
public class BranchController {
  record Branch(String code, String city) {}

  @GetMapping("/version")
  String version() {
    return "Ledgerly 0.1";
  }

  @GetMapping("/branches")
  List<Branch> branches() {
    return List.of(new Branch("BLR01", "Bengaluru"), new Branch("DEL01", "New Delhi"));
  }
}
```

`@RestController` makes the class a bean that component scanning finds, and says a method's return value is the response body. Return a `String` and the client gets plain text. Return a record or a list and Spring writes JSON. Real output:

```
$ curl -i localhost:8081/version
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 12
Date: Wed, 04 Nov 2026 17:43:55 GMT

Ledgerly 0.1

$ curl localhost:8081/branches
[{"code":"BLR01","city":"Bengaluru"},{"code":"DEL01","city":"New Delhi"}]
```

## Path variables and query parameters

```java
@GetMapping("/branches/{code}")
String branch(@PathVariable String code) {
  return "Branch " + code.toUpperCase();
}

@GetMapping("/double")
int twice(@RequestParam int n) {
  return n * 2;
}
```

A **path variable** is part of the address. A **query parameter** comes after the `?`. Everything on the wire is text, and Spring converts it to the parameter's type:

```
$ curl localhost:8081/branches/blr01
Branch BLR01

$ curl "localhost:8081/double?n=3"
6
```

## What Spring answers for you

You wrote none of the code behind these. All five are real answers from this chapter's demo app.

**404: no method is mapped to that path.**

```
$ curl localhost:8081/nowhere
{"timestamp":"2026-11-04T17:43:55.572Z","status":404,"error":"Not Found","path":"/nowhere"}
```

**405: the path exists, but not for that HTTP method.** Note the `Allow` header.

```
$ curl -i -X POST localhost:8081/version
HTTP/1.1 405 
Allow: GET
Content-Type: application/json

{"timestamp":"2026-11-04T17:43:55.581Z","status":405,"error":"Method Not Allowed","path":"/version"}
```

**400: a value could not be converted.** It never reaches your method.

```
$ curl -i "localhost:8081/double?n=three"
HTTP/1.1 400 
Content-Type: application/json

{"timestamp":"2026-11-04T17:43:55.560Z","status":400,"error":"Bad Request","path":"/double"}
```

The log says why. This one is for you, not for the client:

```
WARN ... DefaultHandlerExceptionResolver : Resolved [org.springframework.web.method.annotation.MethodArgumentTypeMismatchException: Method parameter 'n': Failed to convert value of type 'java.lang.String' to required type 'int'; For input string: "three"]
```

**500: your method threw.** The client gets no detail (good: no stack traces leak out). The log names your line:

```
$ curl localhost:8081/boom
{"timestamp":"2026-11-04T17:43:55.591Z","status":500,"error":"Internal Server Error","path":"/boom"}

java.lang.UnsupportedOperationException: TODO: this method is not written yet
	at ledgerly.BranchController.boom(BranchController.java:41) ~[classes/:na]
```

(The 415 answer, for a wrong request body type, shows up in chapter 9 when we add POST.)

Remember this table. It is a favourite interview question:

| Status | Meaning here | Who decided |
|---|---|---|
| 404 | no handler for that path | Spring |
| 405 | handler exists, wrong HTTP method | Spring |
| 400 | a parameter could not be converted or is missing | Spring |
| 500 | your code threw | Spring, hiding your exception |

## Try it

After task 6, start the app, call it, then stop it. From the repo root:

```bash
mvn -pl chapters/02-first-spring-boot-app spring-boot:run
```

In a second terminal (on Windows, Git Bash or PowerShell with `curl.exe`):

```bash
curl localhost:8081/version
curl localhost:8081/branches/blr01
curl "localhost:8081/double?n=three"
curl localhost:8081/status          # works after task 1
curl "localhost:8081/accounts/above?balance=10000"   # after task 4
```

Stop the app with `Ctrl+C`. You can also run the checks at any time:

```bash
mvn -pl chapters/02-first-spring-boot-app test
```

## Your turn

You edit two files: `AccountController.java` (tasks 1 to 5) and `LedgerlyApplication.java` (task 6). Each task has a `// Task N` comment where its code goes. Do **not** change the `ACCOUNTS` list.

1. **Status.** `GET /status` answers `200` with the plain text `Ledgerly is open`.
2. **List.** `GET /accounts` answers a JSON list of all four accounts, in order.
3. **Path variable.** `GET /accounts/{number}/masked` answers six stars and the last four characters, for example `******4567`.
4. **Query parameter.** `GET /accounts/above?balance=N` answers the accounts with a balance **more than** N, as JSON in order.
5. **Optional parameter.** `GET /fee?amount=N&express=true` answers the fee as a number: 1% of the amount (whole numbers), at least 10, doubled when `express` is true. `express` is optional and defaults to false.
6. **(Interview)** Make `LedgerlyApplication` a Spring Boot application class so the app starts.

**Traps**

- Task 1 is not only the answer. The method has no URL until you map it.
- A method with `@GetMapping` and no `@PathVariable` or `@RequestParam` will not receive the value. The names in `{braces}` and the parameter names must match.
- Task 3: keep `number` a `String`. An `int` would lose the leading zeros of `0000123456`.
- Task 4: "more than" is `>`, not `>=`. The check has a balance of exactly 52000 to catch this.
- Task 4: don't write code for a missing or bad `balance`. Spring already answers 400.
- Task 5: `amount / 100` with ints drops the decimals. That is what you want here. Take the minimum of 10 **before** you double it for express.
- Task 6: start with this one if the app will not run. Tasks 1 to 5 are checked without starting the server, so they pass in any order.

**Interview question (task 6).** Answer in your own words, aloud, in three sentences: *What does `@SpringBootApplication` switch on?* Name the three annotations inside it and say what each one does. Then: *what happens if the controller class is in a package above the application class?* (Hint: look at what component scan covers.)
