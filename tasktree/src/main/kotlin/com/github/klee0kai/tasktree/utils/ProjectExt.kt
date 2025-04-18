package com.github.klee0kai.tasktree.utils

import com.github.klee0kai.tasktree.tasks.TaskGrapthTask
import com.github.klee0kai.tasktree.tasks.FlatListTask
import com.github.klee0kai.tasktree.tasks.TaskTreeTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.execution.taskgraph.DefaultTaskExecutionGraph

const val RECURSIVE_DETECT = 100

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
        .filter { it !is TaskTreeTask && it !is TaskGrapthTask && it !is FlatListTask }

val Project.parents
    get() = generateSequence(this) {
        runCatching { it.parent }.getOrNull()
    }.take(RECURSIVE_DETECT)

fun <T> Sequence<T>.recursiveDetect(
    detectCallback: (List<T>) -> Unit = {},
): Sequence<T> {
    val noValue = object {};
    var ticker = 0
    var rabbit: Any? = noValue
    var turtle: Any? = noValue
    var recursiveDetected = false
    val recursiveCollection = mutableListOf<T>()

    return onEach {
        if (recursiveDetected) {
            recursiveCollection.add(it)
            if (recursiveCollection.size > 2 && recursiveCollection.first() == recursiveCollection.last()) {
                detectCallback.invoke(recursiveCollection)
            }
        } else {
            rabbit = it
            if (turtle == rabbit) recursiveDetected = true
            ticker = (ticker + 1) % 2
            if (ticker == 0) turtle = it
        }
    }
}

