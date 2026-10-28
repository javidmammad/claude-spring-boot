# Phase 1 task: TaskFlow in plain Kotlin

**Goal:** model TaskFlow's core domain and query it using only Kotlin: no Spring, no database.
The services you write in Phase 2 will look almost exactly like this code.

**Estimated time:** 4–6 hours

---

## 1. Project setup (10 min)

In IntelliJ: **New Project → Kotlin**

| Setting | Value |
|---|---|
| Name | `phase1-kotlin` |
| Location | `~/Desktop/be-spring-boot` |
| Build system | **Gradle** |
| JDK | 17 or 21 |
| Gradle DSL | **Kotlin** |

Check that the generated `Main.kt` runs (green ▶ next to `fun main`).

Create these files in `src/main/kotlin/`:

```
Models.kt      → enums, data classes, the sealed result type
TaskStore.kt   → the in-memory "database" + query functions
Extensions.kt  → extension functions
SeedData.kt    → sample data (copy from section 4)
Main.kt        → calls everything and prints results
```

---

## 2. The model (`Models.kt`)

Create these types:

- `enum class TaskStatus { TODO, IN_PROGRESS, DONE }`
  - Add a method `fun canMoveTo(next: TaskStatus): Boolean` that uses `when`. Allowed transitions:
    - `TODO → IN_PROGRESS`
    - `IN_PROGRESS → DONE`
    - `IN_PROGRESS → TODO`
    - `DONE → IN_PROGRESS` (reopen)
    - Every other transition is invalid, including staying on the same status.
- `enum class Priority { LOW, MEDIUM, HIGH }`
- `data class User(id, name, email)`
- `data class Project(id, name, ownerId, memberIds: Set<String>)`
- `data class Task(id, projectId, title, description: String?, status, priority, assigneeId: String?, labels: Set<String>, dueDate: LocalDate?)`
- `data class ProjectSummary(projectName, total, done, completionPercent: Int, overdue)`
- A **sealed** type for the result of a status change:
  - `Success(task)`
  - `NotFound(taskId)`
  - `InvalidTransition(from, to)`

---

## 3. Functions to implement

### `Extensions.kt`

1. `fun Task.isOverdue(today: LocalDate): Boolean`
   A task is overdue when it has a due date, that date is before `today`, and the task isn't `DONE`.

### `TaskStore.kt`

`class TaskStore(users: List<User>, projects: List<Project>, tasks: List<Task>)`
Store the data internally in maps keyed by id. Only the tasks map should be mutable.

2. `fun tasksByStatus(projectId: String): Map<TaskStatus, List<Task>>`
3. `fun overdueTasks(today: LocalDate): List<Task>`: sorted by due date, oldest first
4. `fun workloadPerAssignee(): Map<String, Int>`: **user name** mapped to the number of *open* (not DONE) tasks. Unassigned tasks go under `"Unassigned"`.
5. `fun search(query: String): List<Task>`: case-insensitive match on the title **or** the description. Remember that the description can be null.
6. `fun findTasks(projectId: String, status: TaskStatus? = null, assigneeId: String? = null, label: String? = null): List<Task>`: a `null` argument means "don't filter on this field".
7. `fun updateStatus(taskId: String, newStatus: TaskStatus): StatusChangeResult`: data classes are immutable, so store an updated **copy** of the task.
8. `fun projectSummary(projectId: String, today: LocalDate): ProjectSummary?`: returns `null` when the project doesn't exist.

### `Main.kt`

Call every function above with the checks in section 5 and print the results. Handle `StatusChangeResult` with an exhaustive `when`, without an `else` branch.

---

## Rules (these are the point of the exercise)

- ❌ No `!!` anywhere
- ❌ No `var` inside data classes
- ❌ No manual `for` loops that build up a mutable list. Use `filter`, `map`, `groupBy`, `associateBy`, `count`, `sortedBy`, and so on.
- ✅ Use named arguments when calling `findTasks`
- ✅ Use `?.`, `?:` and `let` for nullable values
- ✅ Keep functions short. Most should be 1 to 5 lines.

---

## 4. Seed data (`SeedData.kt`): copy this

You don't learn anything from typing out sample data, so here it is:

```kotlin
import java.time.LocalDate

val TODAY: LocalDate = LocalDate.of(2026, 10, 12)

val seedUsers = listOf(
    User("u1", "Javid", "javid@taskflow.dev"),
    User("u2", "Aysel", "aysel@taskflow.dev"),
    User("u3", "Murad", "murad@taskflow.dev"),
)

val seedProjects = listOf(
    Project("p1", "Mobile App", ownerId = "u1", memberIds = setOf("u1", "u2")),
    Project("p2", "Backend API", ownerId = "u1", memberIds = setOf("u1", "u3")),
)

val seedTasks = listOf(
    Task("t1", "p1", "Design login screen", "Figma mockups", TaskStatus.DONE, Priority.HIGH, "u2", setOf("ui"), LocalDate.of(2026, 10, 1)),
    Task("t2", "p1", "Implement login API call", null, TaskStatus.IN_PROGRESS, Priority.HIGH, "u1", setOf("api", "ui"), LocalDate.of(2026, 10, 10)),
    Task("t3", "p1", "Push notifications", "Firebase setup", TaskStatus.TODO, Priority.MEDIUM, null, setOf("mobile"), LocalDate.of(2026, 10, 20)),
    Task("t4", "p1", "Fix crash on Android 14", null, TaskStatus.TODO, Priority.HIGH, "u2", setOf("bug", "mobile"), LocalDate.of(2026, 10, 5)),
    Task("t5", "p2", "Set up Spring Boot project", "Kotlin + Gradle", TaskStatus.DONE, Priority.MEDIUM, "u3", setOf("setup"), LocalDate.of(2026, 10, 3)),
    Task("t6", "p2", "Design Mongo schema", "Embed comments, reference users", TaskStatus.IN_PROGRESS, Priority.HIGH, "u3", setOf("db"), LocalDate.of(2026, 10, 15)),
    Task("t7", "p2", "Write API docs", null, TaskStatus.TODO, Priority.LOW, null, setOf("docs"), null),
    Task("t8", "p2", "Add login endpoint", "JWT based login", TaskStatus.TODO, Priority.HIGH, "u1", setOf("api", "security"), LocalDate.of(2026, 10, 11)),
)
```

> If your constructor parameters are in a different order, adjust the seed data to match, or switch to named arguments.

---

## 5. Expected results (check your output against these)

| Call | Expected |
|---|---|
| `overdueTasks(TODAY)` | `t4, t2, t8` (in that order) |
| `workloadPerAssignee()` | `Javid=2, Aysel=1, Murad=1, Unassigned=2` (order doesn't matter) |
| `search("login")` | `t1, t2, t8` |
| `search("FIREBASE")` | `t3` (matched on the description) |
| `findTasks("p1", label = "mobile")` | `t3, t4` |
| `findTasks("p2", status = TaskStatus.TODO)` | `t7, t8` |
| `tasksByStatus("p1")` | `TODO=[t3, t4], IN_PROGRESS=[t2], DONE=[t1]` |
| `updateStatus("t3", TaskStatus.DONE)` | `InvalidTransition(TODO, DONE)` |
| `updateStatus("t3", TaskStatus.IN_PROGRESS)` | `Success`, and `t3` now has status IN_PROGRESS when you read it back |
| `updateStatus("t99", TaskStatus.DONE)` | `NotFound(t99)` |
| `projectSummary("p1", TODAY)` | `Mobile App: total=4, done=1, 25%, overdue=2` |
| `projectSummary("p9", TODAY)` | `null` (print something like "Project not found") |

> Run the `updateStatus` checks **last**, so they don't change the earlier results.

---

## 6. Stretch goals (optional)

- `fun assignTask(taskId: String, userId: String): AssignResult`, where only project members can be assigned. Add a `NotAMember` result.
- `fun topPriorityPerProject(): Map<String, Task?>`: the highest-priority open task in each project
- Replace the `println` calls in `Main.kt` with a single extension function, `fun Task.pretty(): String`

---

## When you're done

Commit your code and tell me **"review Phase 1"**. I'll review it the way a senior reviewer would: idiomatic Kotlin, naming, null handling, and anything that would trip you up later in Spring.
