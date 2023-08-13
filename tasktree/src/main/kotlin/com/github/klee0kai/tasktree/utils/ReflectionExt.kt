package com.github.klee0kai.tasktree.utils

import com.github.klee0kai.tasktree.tasks.DiagonDagTask
import com.github.klee0kai.tasktree.tasks.TaskTreeTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.execution.plan.ExecutionPlan
import org.gradle.execution.plan.TaskNode
import org.gradle.execution.taskgraph.DefaultTaskExecutionGraph

inline fun <reified R> Any.fieldValue(name: String): R? {
    return runCatching {
        val clazz = javaClass
        val field = clazz.getDeclaredField(name)
        field.isAccessible = true
        val value = field.get(this)
        return value as? R?
    }.getOrNull()
}

inline fun <reified R> Any.invokeMethod(name: String): R? {
    return runCatching {
        val clazz = javaClass
        val method = clazz.getDeclaredMethod(name)
        method.isAccessible = true
        val value = method.invoke(this)
        return value as? R?
    }.getOrNull()
}


val Project.requestedTasksReflection
    get() = taskGraph.entryNodesTasksReflection
        .filter { it !is TaskTreeTask && it !is DiagonDagTask }
        .toSet()

val DefaultTaskExecutionGraph.entryNodesTasksReflection: Set<Task>
    get() {
        // gradle 7.4.2
        val gradle7Tasks = fieldValue<ExecutionPlan>("executionPlan")
            ?.invokeMethod<Set<Task>>("getRequestedTasks")
            ?: emptySet()

        // gradle 8.1.1
        val startTasksOnGradle8 = fieldValue<Any>("executionPlan")
            ?.fieldValue<Set<TaskNode>>("waitingToStartNodes")
            ?.map { it.task }
            ?.toSet()
            ?: emptySet()

        return gradle7Tasks + startTasksOnGradle8
    }
