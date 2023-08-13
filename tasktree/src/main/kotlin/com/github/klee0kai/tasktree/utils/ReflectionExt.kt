package com.github.klee0kai.tasktree.utils

import com.github.klee0kai.tasktree.tasks.DiagonDagTask
import com.github.klee0kai.tasktree.tasks.TaskTreeTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.execution.plan.ExecutionPlan
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

val Project.requestedTasksReflection
    get() = taskGraph.entryNodesTasksReflection
        .filter { it !is TaskTreeTask && it !is DiagonDagTask }

val DefaultTaskExecutionGraph.entryNodesTasksReflection: Set<Task>
    get() {
        val gradle7Tasks = fieldValue<ExecutionPlan>("executionPlan")?.requestedTasks ?: emptySet()

        return gradle7Tasks
    }
