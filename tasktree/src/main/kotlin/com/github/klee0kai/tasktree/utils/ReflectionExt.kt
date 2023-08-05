package com.github.klee0kai.tasktree.utils

import org.gradle.execution.plan.ExecutionPlan
import org.gradle.execution.taskgraph.DefaultTaskExecutionGraph

inline fun <reified T, reified R> T.fieldValue(name: String): R? {
    return runCatching {
        val field = T::class.java.getDeclaredField(name)
        field.isAccessible = true
        val value = field.get(this)
        return value as? R?
    }.getOrNull()
}

val DefaultTaskExecutionGraph.executionPlan: ExecutionPlan? get() = fieldValue("executionPlan")
