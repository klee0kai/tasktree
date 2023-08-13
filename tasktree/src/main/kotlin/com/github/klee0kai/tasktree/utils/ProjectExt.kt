package com.github.klee0kai.tasktree.utils

import com.github.klee0kai.tasktree.tasks.DiagonDagTask
import com.github.klee0kai.tasktree.tasks.TaskTreeTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.execution.taskgraph.DefaultTaskExecutionGraph

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
    get() = taskGraph.allTasks.filter { it !is TaskTreeTask && it !is DiagonDagTask }

val Project.parents get() = generateSequence(this) { runCatching { it.parent }.getOrNull() }

fun DefaultTaskExecutionGraph.getAllDeps(task: Task): Set<Task> =
    getDeps(task)
        .flatMap {
            setOf(it) + getAllDeps(it)
        }
        .toSet()


fun DefaultTaskExecutionGraph.getDeps(task: Task): Set<Task> =
    try {
        getDependencies(task)
    } catch (ignore: Exception) {
        //ignore non available info
        setOf()
    }
