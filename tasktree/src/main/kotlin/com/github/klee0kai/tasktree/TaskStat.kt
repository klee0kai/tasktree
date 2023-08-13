package com.github.klee0kai.tasktree

import com.github.klee0kai.tasktree.utils.getAllDeps
import com.github.klee0kai.tasktree.utils.taskGraph
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.provideDelegate

class TaskStat(
    val task: Task,
    val allTasks: Set<Task>,
    val rootProject: Project,
) {

    val allDepsCount by lazy { rootProject.taskGraph.getAllDeps(task).count() }
    val allDependedOnCount by lazy {
        allTasks.count {
            rootProject.taskGraph
                .getAllDeps(it)
                .contains(task)
        }
    }
    val price by lazy { allDepsCount }
    val importance by lazy { allDependedOnCount }

    val complexPrice by lazy {
        (price * importance).toFloat() / allTasks.size
    }


    // ---- outside of project ------
    val allDependedOnOutsideProjectCount by lazy {
        allTasks.count {
            it.project != task.project &&
                    rootProject.taskGraph
                        .getAllDeps(it)
                        .contains(task)
        }
    }
    val importanceOutsideProject by lazy { allDependedOnOutsideProjectCount }
    val complexPriceOutsideProject by lazy { (price * importanceOutsideProject).toFloat() / allTasks.size }
}
