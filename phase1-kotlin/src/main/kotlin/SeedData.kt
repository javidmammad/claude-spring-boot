package main.kotlin
import java.time.LocalDate

val TODAY: LocalDate = LocalDate.of(2026, 10, 12)

val seedUsers = listOf<User>(
    User("u1", "Javid", "javid@taskflow.dev"),
    User("u2", "Aysel", "aysel@taskflow.dev"),
    User("u3", "Murad", "murad@taskflow.dev"),
)

val seedProjects = listOf<Project>(
    Project("p1", "Mobile App", ownerId = "u1", memberIds = setOf("u1", "u2")),
    Project("p2", "Backend API", ownerId = "u1", memberIds = setOf("u1", "u3")),
)

val seedTasks = listOf<Task>(
    Task("t1", "p1", "Design login screen", "Figma mockups", TaskStatus.DONE, Priority.HIGH, "u2", setOf("ui"), LocalDate.of(2026, 10, 1)),
    Task("t2", "p1", "Implement login API call", null, TaskStatus.IN_PROGRESS, Priority.HIGH, "u1", setOf("api", "ui"), LocalDate.of(2026, 10, 10)),
    Task("t3", "p1", "Push notifications", "Firebase setup", TaskStatus.TODO, Priority.MEDIUM, null, setOf("mobile"), LocalDate.of(2026, 10, 20)),
    Task("t4", "p1", "Fix crash on Android 14", null, TaskStatus.TODO, Priority.HIGH, "u2", setOf("bug", "mobile"), LocalDate.of(2026, 10, 5)),
    Task("t5", "p2", "Set up Spring Boot project", "Kotlin + Gradle", TaskStatus.DONE, Priority.MEDIUM, "u3", setOf("setup"), LocalDate.of(2026, 10, 3)),
    Task("t6", "p2", "Design Mongo schema", "Embed comments, reference users", TaskStatus.IN_PROGRESS, Priority.HIGH, "u3", setOf("db"), LocalDate.of(2026, 10, 15)),
    Task("t7", "p2", "Write API docs", null, TaskStatus.TODO, Priority.LOW, null, setOf("docs"), null),
    Task("t8", "p2", "Add login endpoint", "JWT based login", TaskStatus.TODO, Priority.HIGH, "u1", setOf("api", "security"), LocalDate.of(2026, 10, 11)),
)