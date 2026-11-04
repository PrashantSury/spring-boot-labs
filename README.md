# Ledgerly Spring Boot Labs

Self-paced Spring Boot labs, set in a retail bank called **Ledgerly Bank**: customers, accounts and transfers. You read a short chapter, edit a stub, run the checks, and read what they tell you. The labs are written for a working Java developer who wants the Spring *defaults* explained: the built-in 400, 404, 405, 415 and 500, handler ordering, `@Valid` and MockMvc tests.

- Java 21, Spring Boot 4.1.2, Maven 3.9+
- Plain words, short chapters, real output (every sample in a chapter was produced by running it)
- One interview question per chapter

## How a chapter works

Each chapter is a folder `chapters/NN-name/` with:

| File | What it is |
|---|---|
| `README.md` | The story, the ideas with real output, the tasks and the traps |
| `src/main/java/...` | Stub code. Each `// Task N` comment marks where a change goes |
| `src/test/java/.../ChapterNNCheckTest.java` | The checks. They fail on the stub, and the message says what is expected |

Chapter 1 is a little different: you edit `lab/pom.xml`, and the checks read it.

Start a chapter like this:

```bash
mvn -pl chapters/02-first-spring-boot-app test
```

Red is where you start. Do a task, run it again, and read the next message. Green means every check passes. See [HOW-TO-RUN.md](HOW-TO-RUN.md) for IntelliJ on Windows.

## Roadmap

| # | Section | Chapter | Status |
|---|---|---|---|
| 01 | Spring foundations | [Maven and the pom.xml](chapters/01-maven-and-the-pom/README.md) | READY |
| 02 | Spring foundations | [Your first Spring Boot app](chapters/02-first-spring-boot-app/README.md) | READY |
| 03 | Spring foundations | [Beans and the application context](chapters/03-beans-and-the-application-context/README.md) | READY |
| 04 | Spring foundations | [Dependency injection](chapters/04-dependency-injection/README.md) | NOT_STARTED |
| 05 | Spring foundations | [Configuration and profiles](chapters/05-configuration-and-profiles/README.md) | NOT_STARTED |
| 06 | Spring foundations | [How auto-configuration works](chapters/06-how-auto-configuration-works/README.md) | NOT_STARTED |
| 07 | Spring foundations | [The bean lifecycle](chapters/07-the-bean-lifecycle/README.md) | NOT_STARTED |
| 08 | REST APIs | [How Spring handles requests](chapters/08-how-spring-handles-requests/README.md) | NOT_STARTED |
| 09 | REST APIs | [Requests and responses](chapters/09-requests-and-responses/README.md) | NOT_STARTED |
| 10 | REST APIs | [Layers and DTOs](chapters/10-layers-and-dtos/README.md) | NOT_STARTED |
| 11 | REST APIs | [Updating and deleting](chapters/11-updating-and-deleting/README.md) | NOT_STARTED |
| 12 | REST APIs | [Validating input](chapters/12-validating-input/README.md) | NOT_STARTED |
| 13 | REST APIs | [Handling errors](chapters/13-handling-errors/README.md) | NOT_STARTED |
| 14 | REST APIs | [Filters and interceptors](chapters/14-filters-and-interceptors/README.md) | NOT_STARTED |
| 15 | Data | [Plain SQL with JdbcClient](chapters/15-plain-sql-with-jdbcclient/README.md) | NOT_STARTED |
| 16 | Data | [JPA entities](chapters/16-jpa-entities/README.md) | NOT_STARTED |
| 17 | Data | [Repositories and query methods](chapters/17-repositories-and-query-methods/README.md) | NOT_STARTED |
| 18 | Data | [Pagination and sorting](chapters/18-pagination-and-sorting/README.md) | NOT_STARTED |
| 19 | Data | [Projections and aggregations](chapters/19-projections-and-aggregations/README.md) | NOT_STARTED |
| 20 | Data | [Entity relationships](chapters/20-entity-relationships/README.md) | NOT_STARTED |
| 21 | Data | [Cascading and transactions](chapters/21-cascading-and-transactions/README.md) | NOT_STARTED |
| 22 | Data | [The persistence context](chapters/22-the-persistence-context/README.md) | NOT_STARTED |
| 23 | Data | [Fetching and the N+1 problem](chapters/23-fetching-and-the-n-plus-1-problem/README.md) | NOT_STARTED |
| 24 | Security | [Spring Security basics](chapters/24-spring-security-basics/README.md) | NOT_STARTED |
| 25 | Security | [Users from the database](chapters/25-users-from-the-database/README.md) | NOT_STARTED |
| 26 | Security | [JWT fundamentals](chapters/26-jwt-fundamentals/README.md) | NOT_STARTED |
| 27 | Security | [JWT login and the JWT filter](chapters/27-jwt-login-and-the-jwt-filter/README.md) | NOT_STARTED |
| 28 | Security | [Roles and authorities](chapters/28-roles-and-authorities/README.md) | NOT_STARTED |
| 29 | Security | [Method security](chapters/29-method-security/README.md) | NOT_STARTED |
| 30 | Security | [Access and refresh tokens](chapters/30-access-and-refresh-tokens/README.md) | NOT_STARTED |
| 31 | Security | [Security errors](chapters/31-security-errors/README.md) | NOT_STARTED |
| 32 | Production | [Caching](chapters/32-caching/README.md) | NOT_STARTED |
| 33 | Production | [Transactions and locking](chapters/33-transactions-and-locking/README.md) | NOT_STARTED |
| 34 | Production | [AOP](chapters/34-aop/README.md) | NOT_STARTED |
| 35 | Production | [Calling other services with RestClient](chapters/35-calling-other-services-with-restclient/README.md) | NOT_STARTED |
| 36 | Production | [Actuator](chapters/36-actuator/README.md) | NOT_STARTED |
| 37 | Production | [Events and scheduling](chapters/37-events-and-scheduling/README.md) | NOT_STARTED |
| 38 | Projects | [Project: the transfers service](chapters/38-project-the-transfers-service/README.md) | NOT_STARTED |
| 39 | Projects | [Project: secure transfers](chapters/39-project-secure-transfers/README.md) | NOT_STARTED |
| 40 | Interview | [Interview: Spring core](chapters/40-interview-spring-core/README.md) | NOT_STARTED |
| 41 | Interview | [Interview: data and web](chapters/41-interview-data-and-web/README.md) | NOT_STARTED |

`READY` means the chapter is written and verified. `NOT_STARTED` means only the plan exists (a README with the topics and traps).

## Rules of this repo

- The repo has **stubs and checks only**. Solutions are not committed. Write your own.
- Keep your own work on a branch (for example `my-solutions`) so you can pull new chapters on `main`.
- No secrets in the repo. `.env` and `*.pem` are ignored.
