# Backend Roadmap: Kotlin + Spring Boot + MongoDB

**Who it's for:** a frontend/mobile developer with 5–8 hours a week
**Length:** about 16 weeks (around 110 hours)
**Project:** **TaskFlow**, a task and project manager REST API built step by step in this folder
**How we work:** you write the code. I explain each step, give you tasks, and review what you write.

---

## Why this order

1. **Learn by building.** Every phase adds a real feature to TaskFlow. You won't study a topic without using it right away.
2. **Learn what real jobs use, and skip the rest for now.** Coroutines, WebFlux, Kafka and microservices come at the end as awareness topics. Most Spring/Kotlin jobs run on plain Spring MVC.
3. **Test and secure early.** Many tutorials leave testing and auth for the end. In real teams, a PR without tests doesn't get merged.

### Mapping from what you already know

| Frontend / mobile idea | Backend equivalent |
|---|---|
| TypeScript types / Swift structs | Kotlin `data class` |
| `fetch` / Axios call | HTTP request handled by a `@RestController` |
| React Context / DI in Hilt or Koin | Spring **IoC container** and **dependency injection** |
| Redux slice / ViewModel logic | `@Service` layer |
| AsyncStorage / Room / CoreData | MongoDB through a `MongoRepository` |
| `.env` files | `application.yml` + **profiles** (dev, test, prod) |
| Jest / XCTest | JUnit 5 + MockK + Testcontainers |
| Auth token kept in the app | The server that **creates and checks** that JWT |

---

## TaskFlow domain (what we'll build)

```
User ──owns──▶ Project ──contains──▶ Task ──has──▶ Comment (embedded)
  │               │                   │
  └──member of────┘                   └── assignee (User ref), labels, status, dueDate, attachments
```

Endpoints we'll build up over time: auth (register, login, refresh), projects (CRUD plus members), tasks (CRUD, filters, search, pagination), comments, attachments, and dashboard stats.

---

## Phase 0: Setup (week 1, about 2h)

- [ ] Install **JDK 21** (LTS) with `brew install --cask temurin@21`. You have 17 now; 21 is the current standard.
- [ ] IntelliJ IDEA: you already have it. Install the Kotlin and Spring plugins if you're on Community edition.
- [ ] Docker Desktop: you already have it. We'll run MongoDB in a container.
- [ ] Install **MongoDB Compass** (a GUI for viewing data) and **Bruno** or Postman (to call your API).
- [ ] Run `git init` in this folder. Commit after every task you finish.

**Done when:** `java -version` shows 21 and `docker run hello-world` works.

---

## Phase 1: Kotlin for backend (weeks 1–2, about 10h)

You can skip Android-specific Kotlin. These are the topics that matter for backend work:

- `val`/`var`, type inference, **null safety** (`?`, `?.`, `?:`, avoiding `!!`)
- `data class`, `copy()`, destructuring
- Functions: default and named arguments, single-expression functions, **extension functions**
- Lambdas and collections: `map`, `filter`, `groupBy`, `associateBy`, `firstOrNull`, `sumOf`
- `when`, **sealed classes** and interfaces (useful for modeling results and errors)
- `object`, `companion object`, `enum class`
- Scope functions: `let`, `apply`, `also`, `run` (when to use each)
- Classes: constructors, `open`. Spring needs classes to be `open`, and the `kotlin-spring` plugin handles that for you.

**Resources:** [Kotlin Koans](https://play.kotlinlang.org/koans) (do about 60%) and the official "Kotlin for Java developers" docs.
**Task:** a small Kotlin console app that models `Task` and `Project` in memory. Filter tasks by status, group them by assignee, and count overdue tasks.
**Done when:** you can write that app without looking up syntax.

---

## Phase 2: Spring Boot fundamentals (weeks 3–4, about 12h)

- How HTTP works on the server side: methods, status codes, headers, JSON, REST naming
- Generate the project at [start.spring.io](https://start.spring.io): Kotlin, Gradle (Kotlin DSL), Spring Web, Validation, Data MongoDB, Actuator
- Project structure and Gradle basics (`build.gradle.kts`, dependencies, `./gradlew bootRun`)
- **IoC and dependency injection:** beans, `@Component`/`@Service`/`@Repository`, constructor injection, and why the framework creates your objects for you
- **Layered architecture:** Controller → Service → Repository, and why each layer exists
- `@RestController`, `@GetMapping`/`@PostMapping`/…, `@PathVariable`, `@RequestParam`, `@RequestBody`, `ResponseEntity`
- `application.yml`, `@ConfigurationProperties`, **profiles**
- Package by feature (`project/`, `task/`, `user/`) rather than by layer

**Milestone:** a Projects and Tasks CRUD API that stores data in an in-memory `Map`, exercised with Bruno or Postman.
**Done when:** you can explain what happens from the moment an HTTP request arrives until JSON is sent back.

---

## Phase 3: MongoDB and Spring Data MongoDB (weeks 5–6, about 12h)

- Document databases compared with SQL: collections, documents, BSON, `_id`/ObjectId
- **Data modeling, the most important Mongo skill:** when to embed and when to reference. In TaskFlow, comments are embedded in a task and the assignee is a reference.
- Run Mongo with `docker compose`, then explore it in Compass and `mongosh` (`find`, `insertOne`, `updateOne`, operators like `$set`, `$push`, `$in`)
- `@Document`, `@Id`, `@Indexed`, `@CompoundIndex`
- `MongoRepository`: derived queries (`findByProjectIdAndStatus`) and `@Query`
- `MongoTemplate` with `Query`/`Criteria`/`Update` for dynamic filters and partial updates
- **Pagination and sorting** with `Pageable`
- Indexes and `explain()`, so you understand why a query is slow

**Milestone:** TaskFlow stores data in Mongo, and tasks support filtering (status, assignee, label, due date range), text search, and pagination.
**Done when:** you can justify every embed-or-reference decision in your model.

---

## Phase 4: DTOs, validation, error handling (week 7, about 6h)

- Why you never return entities directly: request and response **DTOs**, and mapping them with Kotlin extension functions
- Jakarta Validation: `@field:NotBlank`, `@field:Size`, `@Valid`. The `@field:` prefix is a Kotlin detail you need to know.
- Global error handling with `@RestControllerAdvice` and **`ProblemDetail`** (RFC 7807)
- Custom exceptions such as `NotFoundException` and `ForbiddenException`, and mapping them to status codes

**Milestone:** every endpoint validates its input and returns consistent error JSON.

---

## Phase 5: Testing (weeks 8–9, about 10h)

- Testing pyramid: unit, slice, and integration tests
- **JUnit 5** and **MockK** (the Kotlin-native mocking library) for unit testing services
- `@WebMvcTest` + `MockMvc` for controller tests (status codes, JSON, validation errors)
- **Testcontainers** to run integration tests against a real MongoDB
- `@SpringBootTest` for full-flow tests
- Test naming, arrange-act-assert, and test data builders

**Milestone:** about 70% or more coverage of services and controllers, and `./gradlew test` passes.
**Done when:** you write tests together with each feature, not afterwards.

---

## Phase 6: Security and auth (weeks 10–11, about 14h)

This phase is the hardest. Take your time with it.

- The Spring Security **filter chain** mental model and `SecurityFilterChain` configuration
- Password hashing with BCrypt (never store plain text)
- **JWT** access tokens plus refresh tokens: login, refresh, logout
- Reading the current user (`@AuthenticationPrincipal`, `SecurityContext`)
- **Authorization:** roles, plus ownership and membership checks ("only project members can see its tasks")
- CORS, which your frontend apps will need
- Common mistakes: leaking data through IDs (IDOR), mass assignment, and secrets committed to git

**Milestone:** register and login work, all endpoints are protected, and users only see their own projects. Includes security tests.

---

## Phase 7: Advanced Mongo and real-world features (weeks 12–13, about 12h)

- **Aggregation pipeline** (`$match`, `$group`, `$lookup`, `$project`, `$facet`) to build a dashboard endpoint: tasks per status, overdue counts, workload per assignee
- **Auditing** with `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`
- **Optimistic locking** with `@Version`, so two users editing the same task don't overwrite each other silently
- **Transactions** (these need a replica set; we'll configure Docker for it), for example deleting a project together with its tasks
- Soft delete
- File attachments: upload and download, stored in GridFS or S3-compatible storage
- Schema evolution and data migrations (Mongock)

**Milestone:** dashboard stats, safe concurrent edits, and attachments on tasks.

---

## Phase 8: Production readiness (weeks 14–15, about 12h)

- API docs with **springdoc-openapi** (Swagger UI)
- Logging: SLF4J, log levels, structured logs, and a correlation ID per request
- **Actuator**: health checks and metrics
- Configuration and secrets per environment, using environment variables
- **Dockerfile** (multi-stage) and `docker-compose` with both the app and Mongo
- **CI** with GitHub Actions: build, test, lint (ktlint or detekt)
- **Deploy** to Render, Railway, or Fly.io with MongoDB Atlas (free tier)

**Milestone:** TaskFlow is live at a public URL with Swagger docs, and CI runs on every push.

---

## Phase 9: Ready for real projects (week 16 and later, about 10h)

- Caching (`@Cacheable`, and Redis as an overview)
- Scheduled jobs (`@Scheduled`), for example a daily email about overdue tasks
- Domain events and `@Async`, such as "task assigned" notifications
- Rate limiting
- **Awareness only:** coroutines with Spring, WebFlux/reactive, messaging (Kafka/RabbitMQ), microservices compared with a modular monolith
- Team skills: reading an unfamiliar Spring codebase, PR etiquette, and debugging with IntelliJ breakpoints
- **Optional:** connect one of your own frontend or mobile apps to TaskFlow. This is a strong portfolio piece.

---

## Job-ready checklist

- [ ] I can design a REST API and a Mongo data model for a new feature on my own
- [ ] I understand DI, beans, and the request lifecycle in Spring
- [ ] I write unit and integration tests by default
- [ ] I can implement JWT auth and resource-level authorization
- [ ] I can write aggregation pipelines and explain which indexes a query needs
- [ ] I can containerize, configure, and deploy a Spring Boot app with CI
- [ ] I can debug with breakpoints and logs instead of guessing

## Weekly rhythm (5–8h)

- **About 1h of reading or video** on the week's concept. Don't go further until you've used it.
- **About 5h of coding** the TaskFlow task.
- **About 30 min with me:** ask me to review your code ("review my Phase 3 task"), and I'll point out what a senior reviewer would catch.
- Commit at the end of each session. Don't move on until the current phase's "Done when" line is true.
