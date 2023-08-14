package com.github.klee0kai.tasktree

import org.gradle.api.Project
import org.gradle.api.Task

class TaskStat(
    val task: Task,
    val allTasks: Set<Task>,
    val rootProject: Project,
) {

    var allDepsCount: Long = 0
    var allDependedOnCount: Long = 0
    var allDependedOnOutsideProjectCount: Long = 0


    val price get() = allDepsCount
    val importance get() = allDependedOnCount

    val complexPrice get() = (price * importance).toFloat() / allTasks.size


    // ---- outside of project ------
    val importanceOutsideProject get() = allDependedOnOutsideProjectCount
    val complexPriceOutsideProject get() = (price * importanceOutsideProject).toFloat() / allTasks.size

}
