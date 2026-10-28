package main.kotlin

// Exhaustive `when` over a sealed type: no `else`.
// If a 4th result type is added later, this stops compiling until it's handled.
fun printResult(result: StatusChangeResult) = when (result) {
    is StatusChangeResult.Success -> println("✅ ${result.task.id} is now ${result.task.status}")
    is StatusChangeResult.NotFound -> println("❌ Task ${result.taskId} not found")
    is StatusChangeResult.InvalidTransition -> println("⛔ Cannot move from ${result.from} to ${result.to}")
}

// Prints only task ids, so the output is easy to compare with the expected values.
fun List<Task>.ids(): List<String> = map { it.id }

fun main() {
    val taskStore = TaskStore(seedUsers, seedProjects, seedTasks)

    println("— Queries —")
    val tasksByStatus = taskStore.tasksByStatus("p1").mapValues { (_, tasks) -> tasks.ids() }
    println("Tasks by status (p1): $tasksByStatus")
    println("Overdue: ${taskStore.overdueTasks(TODAY).ids()}")
    println("Workload: ${taskStore.workloadPerAssignee()}")
    println("Search 'login': ${taskStore.search("login").ids()}")
    println("Search 'FIREBASE': ${taskStore.search("FIREBASE").ids()}")
    println("p1 + label=mobile: ${taskStore.findTasks("p1", label = "mobile").ids()}")
    println("p2 + status=TODO: ${taskStore.findTasks("p2", status = TaskStatus.TODO).ids()}")

    listOf("p1", "p9").forEach { projectId ->
        taskStore.projectSummary(projectId, TODAY)
            ?.let { println("Summary $projectId: $it") }
            ?: println("Summary $projectId: Project not found")
    }

    println("\n— Status changes —")
    printResult(taskStore.updateStatus("t3", TaskStatus.DONE))
    printResult(taskStore.updateStatus("t3", TaskStatus.IN_PROGRESS)) // ✅
    printResult(taskStore.updateStatus("t99", TaskStatus.DONE))       // ❌ not found
    println("p1 IN_PROGRESS after update: ${taskStore.findTasks("p1", status = TaskStatus.IN_PROGRESS).ids()}")
}
