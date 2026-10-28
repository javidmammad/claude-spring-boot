package main.kotlin

import java.time.LocalDate

class TaskStore( users: List<User>, projects: List<Project>, tasks: List<Task>) {
    private val usersById = users.associateBy { it.id }
    private val projectsById = projects.associateBy { it.id }
    private val tasksById = tasks.associateBy { it.id }.toMutableMap() // output is: { id1: Task(...) }

    fun tasksByStatus(projectId: String): Map<TaskStatus, List<Task>> {
        val projectTasks = tasksById.values.filter { it.projectId == projectId }
        return TaskStatus.entries.associateWith { status ->
            projectTasks.filter { it.status == status }
        }
    }

    fun overdueTasks(today: LocalDate): List<Task> {
        return tasksById.values.filter { it.isOverdue(today) }.sortedBy { it.dueDate }
    }

    fun workloadPerAssignee(): Map<String, Int> {
        return tasksById.values.filter { it.status != TaskStatus.DONE }
            .groupingBy { task -> usersById[task.assigneeId]?.name ?: "Unassigned" }
            .eachCount()
    }

    fun search(query: String): List<Task> {
        val lowerCaseQuery = query.lowercase()
        return tasksById.values.filter {
            it.title.lowercase().contains(lowerCaseQuery) ||
                    (it.description?.lowercase()?.contains(lowerCaseQuery) ?: false)}
    }

    fun findTasks(projectId: String, status: TaskStatus? = null, assigneeId: String? = null, label: String? = null): List<Task> {
        return tasksById.values.filter { task ->
            task.projectId == projectId &&
                    (status == null || task.status == status) &&
                    (assigneeId == null || task.assigneeId == assigneeId) &&
                    (label == null || task.labels.contains(label))
        }
    }

    fun updateStatus(taskId: String, newStatus: TaskStatus): StatusChangeResult {
        val task = tasksById[taskId] ?: return StatusChangeResult.NotFound(taskId)
        if (task.status.canMoveTo(newStatus).not()) {
            return StatusChangeResult.InvalidTransition(from = task.status, to = newStatus)
        }
        val updatedTask = task.copy(status = newStatus)
        tasksById[taskId] = updatedTask
        return StatusChangeResult.Success(updatedTask)
    }

    fun projectSummary(projectId: String, today: LocalDate): ProjectSummary? {
        val project = projectsById[projectId] ?: return null
        val projectTasks = tasksById.values.filter { it.projectId == projectId }
        val projectDoneTasks = projectTasks.count { it.status == TaskStatus.DONE }
        val projectOverdueTasks = projectTasks.count { it.isOverdue(today) }
        val completionPercent = if (projectTasks.isNotEmpty()) {
            (projectDoneTasks * 100) / projectTasks.size
        } else {
            0
        }

        return ProjectSummary(
            projectName = project.name,
            total = projectTasks.size,
            done = projectDoneTasks,
            completionPercent = completionPercent,
            overdue = projectOverdueTasks
        )
    }
}
