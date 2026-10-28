package main.kotlin

import java.time.LocalDate

fun TaskStatus.canMoveTo(next: TaskStatus): Boolean {
    return when(next) {
        TaskStatus.TODO -> this == TaskStatus.IN_PROGRESS
        TaskStatus.IN_PROGRESS -> this == TaskStatus.TODO || this == TaskStatus.DONE
        TaskStatus.DONE -> this == TaskStatus.IN_PROGRESS
    }
}

fun Task.isOverdue(today: LocalDate): Boolean {
    return this.dueDate?.isBefore(today) == true && this.status != TaskStatus.DONE
}