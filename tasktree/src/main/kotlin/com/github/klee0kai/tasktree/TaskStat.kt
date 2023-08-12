package com.github.klee0kai.tasktree

import com.github.klee0kai.tasktree.utils.getAllDeps
import com.github.klee0kai.tasktree.utils.taskGraph
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.provideDelegate

class TaskStat(
    val task: Task,
    val allTasks: Set<Task>,
    val project: Project,
) {

    val allDepsCount by lazy { project.taskGraph.getAllDeps(task).count() }
    val allDependedOnCount by lazy {
        allTasks.count {
            project.taskGraph
                .getAllDeps(it)
                .contains(task)
        }
    }
    val price by lazy { allDepsCount }
    val importance by lazy { allDependedOnCount }

    val complexPrice by lazy {
        (price * importance).toFloat() / allTasks.size
    }

}
