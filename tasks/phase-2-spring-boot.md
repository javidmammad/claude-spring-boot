# Phase 2 task: TaskFlow as a Spring Boot REST API

**Goal:** turn your Phase 1 `TaskStore` into a real HTTP API. Same domain, same rules, but now Spring creates your objects, routes HTTP requests to your code, and turns your Kotlin objects into JSON.
Data still lives in memory. MongoDB comes in Phase 3, and the way you structure things here is what makes that swap painless.

**Estimated time:** about 12 hours over 2 weeks, split into 5 milestones. Ask for a review after each one.

| Milestone | What | Time |
|---|---|---|
| M1 | Generate the project, run it, first endpoint, look at your beans | ~2h |
| M2 | Projects CRUD (Controller → Service → Repository) | ~3h |
| M3 | Tasks CRUD, filters, status changes | ~4h |
| M4 | Configuration: `application.yml`, `@ConfigurationProperties`, profiles, seed data | ~2h |
| M5 | Explain the request lifecycle in your own words | ~1h |

---

## 1. Setup (30 min)

### 1.1 JDK 21

```bash
sdk list java | grep -i zulu        # pick the newest 21.x zulu version from the list
sdk install java 21.0.x-zulu        # replace x with that version
sdk default java 21.0.x-zulu
java -version                       # should say 21
```

In IntelliJ, set **Project SDK** and **Gradle JVM** to 21 (Settings → Build Tools → Gradle) after you open the project.

### 1.2 Generate the project

Go to [start.spring.io](https://start.spring.io):

| Setting | Value |
|---|---|
| Project | **Gradle - Kotlin** |
| Language | **Kotlin** |
| Spring Boot | the newest version **without** `SNAPSHOT`, `M` or `RC` in its name |
| Group | `dev.taskflow` |
| Artifact / Name | `taskflow` |
| Package name | `dev.taskflow` |
| Packaging | Jar |
| Java | **21** |
| Configuration | **YAML**, if the option is there. Otherwise rename `application.properties` to `application.yml` later. |
| Dependencies | **Spring Web**, **Validation**, **Spring Data MongoDB**, **Spring Boot Actuator** |

Click **Generate**, unzip, and put the folder at the repo root as `taskflow/`:

```
be-spring-boot/
├── ROADMAP.md
├── notes/
├── tasks/
├── phase1-kotlin/
└── taskflow/          ← new
```

Open it in IntelliJ with **File → Open → `taskflow/build.gradle.kts`** → *Open as Project*. Wait for the Gradle sync to finish.

### 1.3 MongoDB (just so the app is happy)

You added Spring Data MongoDB now so you don't have to regenerate the project in Phase 3. The side effect: the app tries to reach a Mongo server on startup, and `/actuator/health` reports `DOWN` without one. Start a throwaway container:

```bash
docker run -d --name taskflow-mongo -p 27017:27017 mongo:8
```

You won't store anything in it yet. Next time, start it again with `docker start taskflow-mongo`.

> Alternative if you don't want Docker running: find the property that turns off the Mongo health indicator (type `management.health.mongo` in `application.yml` and let IntelliJ autocomplete it). You'll still see connection warnings in the log.

### 1.4 An HTTP client

Install **Bruno** (`brew install --cask bruno`) or use Postman. With Bruno, create the collection inside the repo at `taskflow/bruno/` so your requests get committed with your code. Add an environment variable `baseUrl = http://localhost:8080`.

---

## 2. Concepts (read before or during each milestone)

I'll explain each one in chat before you need it. These are the topics; the links are for extra reading.

1. **HTTP on the server side**: methods (GET, POST, PUT, PATCH, DELETE), status codes (200, 201, 204, 400, 404, 409), headers (`Content-Type`, `Location`), JSON bodies, REST naming (nouns, plural, nested resources). [MDN: HTTP status codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)
2. **Gradle basics**: `build.gradle.kts` (plugins, dependencies), the `./gradlew` wrapper, `./gradlew bootRun`, `./gradlew build`. Look for the `kotlin("plugin.spring")` plugin: it makes your classes `open` so Spring can work with them.
3. **IoC and dependency injection**: beans, `@Component`/`@Service`/`@Repository`/`@RestController`, constructor injection, singleton scope. *(Explained in chat today.)*
4. **Layered architecture**: Controller → Service → Repository, and what belongs in each layer. *(Explained in chat today.)*
5. **Spring MVC annotations**: `@RestController`, `@RequestMapping`, `@GetMapping`/`@PostMapping`/`@PutMapping`/`@PatchMapping`/`@DeleteMapping`, `@PathVariable`, `@RequestParam`, `@RequestBody`, `ResponseEntity`. [Spring guide: Building a RESTful web service](https://spring.io/guides/gs/rest-service)
6. **JSON with Jackson**: how a Kotlin data class becomes JSON and back, and why non-null Kotlin types and default values matter (`jackson-module-kotlin` is already in your dependencies).
7. **Configuration**: `application.yml`, `@ConfigurationProperties`, profiles (`application-dev.yml`), `@Profile`, `@Configuration` + `@Bean`. [Externalized configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html), [Profiles](https://docs.spring.io/spring-boot/reference/features/profiles.html)
8. **Package by feature**: `project/`, `task/` folders, each with its own model, repository, service and controller, instead of `controllers/`, `services/`, ...

---

## 3. What to build

### Package layout

```
taskflow/src/main/kotlin/dev/taskflow/
├── TaskflowApplication.kt        ← generated, has @SpringBootApplication
├── project/
│   ├── Project.kt                ← Project, ProjectSummary, ProjectRequest
│   ├── ProjectRepository.kt      ← interface + InMemoryProjectRepository
│   ├── ProjectService.kt
│   └── ProjectController.kt
├── task/
│   ├── Task.kt                   ← Task, TaskStatus, Priority, StatusChangeResult, TaskRequest, StatusChangeRequest
│   ├── TaskRules.kt              ← canMoveTo, isOverdue (copied from Phase 1)
│   ├── TaskRepository.kt         ← interface + InMemoryTaskRepository
│   ├── TaskService.kt
│   └── TaskController.kt
└── config/                       ← M4
    ├── TaskflowProperties.kt
    ├── ClockConfig.kt
    └── DevSeedData.kt
```

**Copy** `Task`, `Project`, `ProjectSummary`, `TaskStatus`, `Priority`, `StatusChangeResult`, `canMoveTo` and `isOverdue` from `phase1-kotlin`. Don't retype or rewrite them; those rules are already tested by your Phase 1 checks. Change only the `package` line. `User` isn't needed until Phase 6.

---

### M1: Run it and say hello (~2h)

1. Run `TaskflowApplication` from IntelliJ (green ▶), then stop it and run `./gradlew bootRun` from a terminal inside `taskflow/`. Both should print `Started TaskflowApplication` and listen on port 8080.
2. Read `build.gradle.kts` top to bottom. Be ready to tell me in one sentence what each `plugins` and `dependencies` line is for.
3. Create a temporary `HelloController` with `GET /api/hello` that returns the string `TaskFlow is running`.
4. In `application.yml`, expose more Actuator endpoints: `management.endpoints.web.exposure.include` = `health,info,beans,mappings`.
5. Call `GET /actuator/beans` and find **your** `helloController` bean in that huge JSON. Call `GET /actuator/mappings` and find your `/api/hello` route.
6. Delete `HelloController` once you've seen it work.

### M2: Projects CRUD (~3h)

#### `ProjectRepository.kt`

An **interface**, plus one implementation annotated with `@Repository`:

```kotlin
interface ProjectRepository {
    fun findAll(): List<Project>
    fun findById(id: String): Project?
    fun existsById(id: String): Boolean
    fun save(project: Project): Project      // insert or replace, returns what was stored
    fun deleteById(id: String): Boolean      // true if something was deleted
}
```

`class InMemoryProjectRepository : ProjectRepository`: keep the data in a **private** `ConcurrentHashMap<String, Project>`, exactly like your Phase 1 `tasksById`. (Why `Concurrent`? Ask me; it's an important question.)

> These names copy Spring Data's naming on purpose. In Phase 3 you'll replace the in-memory class with a Mongo one, and the service won't need to change.

#### `Project.kt`: add a request class

```kotlin
data class ProjectRequest(val name: String, val ownerId: String, val memberIds: Set<String> = emptySet())
```

The client doesn't send an `id`; the server generates one with `UUID.randomUUID().toString()`. The same request class is used for both create and update.

#### `ProjectService` (`@Service`)

Inject `ProjectRepository` and `TaskRepository` through the constructor (you'll write `TaskRepository` in M3, so add `delete` and `summary` after it exists).

| Function | Returns |
|---|---|
| `findAll()` | `List<Project>` |
| `findById(id)` | `Project?` |
| `create(request: ProjectRequest)` | `Project` (with a new UUID) |
| `update(id, request: ProjectRequest)` | `Project?`, `null` if it doesn't exist |
| `delete(id)` | `Boolean`. Deletes the project **and all of its tasks**. |
| `summary(id)` | `ProjectSummary?`, reusing your Phase 1 logic. For "today" use `LocalDate.now()` for now; M4 replaces it. |

#### `ProjectController` (`@RestController`, base path `/api/projects`)

| Method & path | Body | Success | Failure |
|---|---|---|---|
| `GET /api/projects` | | `200` + list | |
| `GET /api/projects/{id}` | | `200` + project | `404` |
| `POST /api/projects` | `ProjectRequest` | `201` + project + `Location: /api/projects/{newId}` header | `400` if the JSON is broken |
| `PUT /api/projects/{id}` | `ProjectRequest` | `200` + updated project | `404` |
| `DELETE /api/projects/{id}` | | `204`, no body | `404` |
| `GET /api/projects/{id}/summary` | | `200` + `ProjectSummary` | `404` |

Hint: `ResponseEntity.ok(x)`, `ResponseEntity.notFound().build()`, `ResponseEntity.noContent().build()`, `ResponseEntity.created(URI.create("/api/projects/$id")).body(x)`. Combine them with `?.let { } ?: ...` or `if`, the same way you handled `projectSummary` returning `null` in Phase 1.

### M3: Tasks (~4h)

#### `TaskRepository.kt`

```kotlin
interface TaskRepository {
    fun findById(id: String): Task?
    fun findByProjectId(projectId: String): List<Task>
    fun save(task: Task): Task
    fun deleteById(id: String): Boolean
    fun deleteByProjectId(projectId: String)
}
```

Plus `InMemoryTaskRepository` with a private `ConcurrentHashMap`, like before.

#### Request classes in `Task.kt`

```kotlin
data class TaskRequest(
    val title: String,
    val description: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val assigneeId: String? = null,
    val labels: Set<String> = emptySet(),
    val dueDate: LocalDate? = null,
)

data class StatusChangeRequest(val status: TaskStatus)
```

Notice that `TaskRequest` has **no `status`**. A new task always starts as `TODO`, and the only way to change the status is the `PATCH .../status` endpoint. If `PUT` could set the status, a client could skip your `canMoveTo` rule.

#### `TaskService` (`@Service`)

Inject `TaskRepository` and `ProjectRepository`.

| Function | Returns |
|---|---|
| `findById(id)` | `Task?` |
| `findTasks(projectId, status: TaskStatus? = null, assigneeId: String? = null, label: String? = null)` | `List<Task>?`, `null` when the **project** doesn't exist. Reuse your Phase 1 filter logic. |
| `create(projectId, request: TaskRequest)` | `Task?`, `null` when the project doesn't exist. New UUID, status `TODO`. |
| `update(id, request: TaskRequest)` | `Task?`. Use `copy()`. Keep `id`, `projectId` and `status` unchanged. |
| `updateStatus(id, newStatus)` | `StatusChangeResult`. This is your Phase 1 `updateStatus`. |
| `delete(id)` | `Boolean` |

#### `TaskController` (`@RestController`)

| Method & path | Body / params | Success | Failure |
|---|---|---|---|
| `GET /api/projects/{projectId}/tasks` | optional `?status=&assigneeId=&label=` | `200` + list | `404` if the project doesn't exist |
| `POST /api/projects/{projectId}/tasks` | `TaskRequest` | `201` + task + `Location: /api/tasks/{newId}` | `404` if the project doesn't exist |
| `GET /api/tasks/{id}` | | `200` + task | `404` |
| `PUT /api/tasks/{id}` | `TaskRequest` | `200` + task | `404` |
| `PATCH /api/tasks/{id}/status` | `StatusChangeRequest` | `200` + task | `404` (NotFound), `409` + `{"from": ..., "to": ...}` (InvalidTransition) |
| `DELETE /api/tasks/{id}` | | `204` | `404` |

Hints:
- A nullable parameter like `status: TaskStatus?` makes `@RequestParam` optional automatically. Spring also converts `"TODO"` to the enum for you.
- Map `StatusChangeResult` to a `ResponseEntity` with an **exhaustive `when`, no `else`**. For the 409 case, `ResponseEntity.status(HttpStatus.CONFLICT).body(result)` turns your `InvalidTransition` data class straight into JSON.
- The controller has two base paths here, so put the full path on each mapping instead of using a class-level `@RequestMapping`.

### M4: Configuration (~2h)

Right now `summary` depends on the real date, so its results change every day, and every restart gives you an empty API. Fix both with configuration.

1. **`TaskflowProperties`**: a `@ConfigurationProperties(prefix = "taskflow")` data class with two properties:
   - `seedData: Boolean = false`
   - `fixedToday: LocalDate? = null`

   Register it by adding `@ConfigurationPropertiesScan` to `TaskflowApplication`.
2. **`ClockConfig`**: a `@Configuration` class with one `@Bean` function that returns a `java.time.Clock`. If `fixedToday` is set, return `Clock.fixed(...)` for that date (UTC). Otherwise return `Clock.systemUTC()`.
   Then inject `Clock` into `ProjectService` and use `LocalDate.now(clock)`. This is DI paying off: the service no longer decides what "today" is.
3. **Profiles**: create `application-dev.yml` with:
   ```yaml
   taskflow:
     seed-data: true
     fixed-today: 2026-10-12
   ```
   Also move the `beans,mappings` Actuator exposure from M1 into the dev profile. Production shouldn't expose those.
4. **`DevSeedData`**: a `@Component` that implements `ApplicationRunner` and saves the Phase 1 seed projects and tasks (`p1`, `p2`, `t1`–`t8`) through the repositories, but only when `taskflow.seedData` is true. Copy the seed lists from `phase1-kotlin/SeedData.kt`.
5. Run with the dev profile: in IntelliJ, edit the run configuration → *Active profiles* → `dev`. From the terminal: `./gradlew bootRun --args='--spring.profiles.active=dev'`.

### M5: The request lifecycle (~1h)

1. In `application-dev.yml`, set `logging.level.org.springframework.web: DEBUG`. Send `POST /api/projects/p1/tasks` and read the log lines it produces.
2. Set a breakpoint in `TaskController`, run in **Debug** mode, send a request, and look at the call stack below your method. Everything under your code is Spring.
3. Write `notes/request-lifecycle.md` in your own words: what happens from the moment `POST /api/projects/p1/tasks` reaches port 8080 until JSON goes back. Mention at least: the embedded server (Tomcat), `DispatcherServlet`, handler mapping, argument resolution and JSON → Kotlin (Jackson), your controller → service → repository, `ResponseEntity`, and Kotlin → JSON.
4. Then tell me **"quiz me on the lifecycle"**. I'll ask you 5 questions.

---

## Rules (these are the point of the exercise)

- ✅ **Constructor injection only.** No `@Autowired`, no `lateinit var` fields.
- ✅ **Controllers only translate HTTP.** They read the request, call one service function, and build a `ResponseEntity`. No filtering, no rules, and no repository injected into a controller.
- ✅ **Services don't know about HTTP.** No `ResponseEntity`, `HttpStatus` or `@RequestParam` in a service. A service should work the same if it's called from a CLI, a test or a scheduled job.
- ✅ **The repository is the only place with state**, and that state is a `private val` map. Services and controllers are shared by every request at the same time, so they must not have `var` fields.
- ✅ **Reuse your rules.** `canMoveTo` and `isOverdue` are copied over and used, not rewritten inline.
- ✅ **Named arguments** for any constructor or call with 3 or more parameters, especially `Task(...)` and `copy(...)`.
- ❌ No `!!`
- ❌ No `else` in a `when` over `StatusChangeResult` or an enum
- ❌ Two services must never inject each other. Services may inject several **repositories**.
- ✅ Commit after each milestone.

### Mistakes from Phase 1 to watch for

- Writing a rule and then repeating its logic somewhere else (`isOverdue`)
- `else` in an exhaustive `when`
- Positional arguments in long constructors
- Public mutable state
- The same filter written in several places (hint: `findTasks` and `summary` both need "the tasks of a project")

---

## 4. Expected results

Run with the **dev** profile, so the seed data is loaded and "today" is **2026-10-12**. As in Phase 1, do the read-only checks first and the ones that change data last. Restart the app to reset the data.

Order inside lists doesn't matter. IDs shown as `<uuid>` are random.

### M1

| Request | Expected |
|---|---|
| `GET /api/hello` | `200`, body `TaskFlow is running` |
| `GET /actuator/health` | `200`, `{"status":"UP"}` (with the Mongo container running) |
| `GET /actuator/beans` | `200`, and you can find `helloController` in it |

### M2 + M3: reads

| Request | Expected |
|---|---|
| `GET /api/projects` | `200`, 2 projects: `p1`, `p2` |
| `GET /api/projects/p1` | `200`, `{"id":"p1","name":"Mobile App","ownerId":"u1","memberIds":["u1","u2"]}` |
| `GET /api/projects/p9` | `404`, empty body |
| `GET /api/projects/p1/summary` | `200`, `{"projectName":"Mobile App","total":4,"done":1,"completionPercent":25,"overdue":2}` |
| `GET /api/projects/p9/summary` | `404` |
| `GET /api/projects/p1/tasks` | `200`, tasks `t1, t2, t3, t4` |
| `GET /api/projects/p1/tasks?label=mobile` | `200`, `t3, t4` |
| `GET /api/projects/p2/tasks?status=TODO` | `200`, `t7, t8` |
| `GET /api/projects/p1/tasks?assigneeId=u2` | `200`, `t1, t4` |
| `GET /api/projects/p1/tasks?status=FINISHED` | `400` (you wrote no code for this; Spring did) |
| `GET /api/projects/p9/tasks` | `404` |
| `GET /api/tasks/t1` | `200`, `{"id":"t1","projectId":"p1","title":"Design login screen","description":"Figma mockups","status":"DONE","priority":"HIGH","assigneeId":"u2","labels":["ui"],"dueDate":"2026-10-01"}` |
| `GET /api/tasks/t99` | `404` |

### M2 + M3: writes (in this order)

| # | Request | Expected |
|---|---|---|
| 1 | `POST /api/projects` with `{"name":"Website","ownerId":"u1","memberIds":["u1"]}` | `201`, header `Location: /api/projects/<uuid>`, body includes `"id":"<uuid>"` and `"name":"Website"` |
| 2 | `GET /api/projects` | `200`, now 3 projects |
| 3 | `PUT /api/projects/p9` with any valid body | `404` |
| 4 | `POST /api/projects/p1/tasks` with `{"title":"Dark mode","labels":["ui"]}` | `201`, `Location: /api/tasks/<uuid>`, body has `"status":"TODO"`, `"priority":"MEDIUM"`, `"description":null`, `"assigneeId":null`, `"dueDate":null` (your defaults worked) |
| 5 | `GET /api/projects/p1/summary` | `total=5, done=1, completionPercent=20, overdue=2` |
| 6 | `POST /api/projects/p1/tasks` with `{"labels":["ui"]}` (no title) | `400`: `title` is a non-null Kotlin type, so Jackson refuses to build the object |
| 7 | `POST /api/projects/p9/tasks` with `{"title":"x"}` | `404` |
| 8 | `PATCH /api/tasks/t3/status` with `{"status":"DONE"}` | `409`, `{"from":"TODO","to":"DONE"}` |
| 9 | `PATCH /api/tasks/t3/status` with `{"status":"IN_PROGRESS"}` | `200`, task `t3` with `"status":"IN_PROGRESS"` |
| 10 | `PATCH /api/tasks/t99/status` with `{"status":"DONE"}` | `404` |
| 11 | `PUT /api/tasks/t7` with `{"title":"Write API docs (OpenAPI)","priority":"HIGH","labels":["docs"]}` | `200`, new title and priority, **status still `TODO`**, `projectId` still `p2` |
| 12 | `DELETE /api/tasks/t7` | `204` |
| 13 | `GET /api/tasks/t7` and then `DELETE /api/tasks/t7` again | `404` both times |
| 14 | `DELETE /api/projects/p2` | `204` |
| 15 | `GET /api/tasks/t5` | `404` (deleted with its project) |

### M4

| Check | Expected |
|---|---|
| Run **without** a profile, `GET /api/projects` | `200`, `[]` (no seed data) |
| Run **without** a profile, `GET /actuator/beans` | `404` (only exposed in dev) |
| Run with `dev`, `GET /api/projects/p1/summary` | `overdue=2`, the same result tomorrow and next month |
| Run with `dev` but override the date: `--spring.profiles.active=dev --taskflow.fixed-today=2026-10-25` | `p1` summary: `overdue=3` (`t3`, due on the 20th, is now late too) |

The last check shows that command-line arguments override `application-dev.yml`, which overrides `application.yml`. That order matters a lot in Phase 8.

### M5

`notes/request-lifecycle.md` exists, and you pass the quiz.

---

## 5. Stretch goals (optional)

- **Validation preview (Phase 4):** add `@field:NotBlank` to `TaskRequest.title` and `@Valid` to the `@RequestBody`. `{"title":"  "}` should now return `400` instead of creating a task with a blank title.
- `GET /api/tasks/search?q=login` that reuses your Phase 1 `search`: returns `t1, t2, t8`
- `GET /api/projects/{id}/tasks/by-status` that reuses `tasksByStatus`
- `PATCH /api/tasks/{id}/assignee` with `{"assigneeId":"u3"}`: only project members can be assigned. Add a sealed `AssignResult` with a `NotAMember` case and return `422`.
- Return the `TaskStatus` transitions as data: `GET /api/task-statuses` → `{"TODO":["IN_PROGRESS"], ...}`, built from `canMoveTo` (no hard-coded list)

---

## When you're done with each milestone

Commit your code and tell me **"review Phase 2 M1"** (or M2, M3, ...). I'll leave `// TODO(n)` comments in your code and give you a short summary in chat. After M5 we do the lifecycle quiz, and then Phase 3 (MongoDB) starts.
