package main.kotlin

import java.time.LocalDate

enum class TaskStatus { TODO, IN_PROGRESS, DONE }
enum class Priority { LOW, MEDIUM, HIGH }
data class User(val id: String, val name: String, val email: String)
data class Project(val id: String, val name: String, val ownerId: String, val memberIds: Set<String>)
data class Task(val id: String, val projectId: String, val title: String, val description: String?, val status: TaskStatus, val priority: Priority, val assigneeId: String?, val labels: Set<String>, val dueDate: LocalDate?)
data class ProjectSummary(val projectName: String, val total: Int, val done: Int, val completionPercent: Int, val overdue: Int)

sealed interface StatusChangeResult {
    data class Success(val task: Task): StatusChangeResult
    data class NotFound(val taskId: String): StatusChangeResult
    data class InvalidTransition(val from: TaskStatus, val to: TaskStatus): StatusChangeResult
}