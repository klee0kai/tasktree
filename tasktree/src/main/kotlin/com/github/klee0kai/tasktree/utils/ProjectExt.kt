package com.github.klee0kai.tasktree.utils

import com.github.klee0kai.tasktree.tasks.DiagonDagTask
import com.github.klee0kai.tasktree.tasks.FlatListTask
import com.github.klee0kai.tasktree.tasks.TaskTreeTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.execution.taskgraph.DefaultTaskExecutionGraph

private const val RECURSIVE_DETECT = 10_000

val Project.fullName
    get() = buildString {
        parents
            .toList()
            .reversed()
            .forEach { project ->
                val isRoot = project.parent == null
                if (!isRoot) append(":${project.name}")
            }
    }

val Task.simpleClassName get() = this.javaClass.simpleName.removeSuffix("_Decorated")

val Project.taskGraph get() = gradle.taskGraph as DefaultTaskExecutionGraph

val Project.allRequestedTasks
    get() = taskGraph.allTasks
        .filter { it !is TaskTreeTask && it !is DiagonDagTask && it !is FlatListTask }

val Project.parents
    get() = generateSequence(this) {
        runCatching { it.parent }.getOrNull()
    }.take(RECURSIVE_DETECT)

fun DefaultTaskExecutionGraph.getDeps(task: Task): Set<Task> =
    try {
        getDependencies(task)
    } catch (ignore: Exception) {
        //ignore non available info
        setOf()
    }
