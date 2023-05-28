package com.github.klee0kai.tasktree.utils

import org.gradle.execution.plan.ExecutionPlan
import org.gradle.execution.taskgraph.DefaultTaskExecutionGraph

inline fun <reified T, R> T.fieldValue(name: String): R? {
    val field = T::class.java.getDeclaredField(name)
    field.isAccessible = true
    return field.get(this) as? R?
}

val DefaultTaskExecutionGraph.executionPlan: ExecutionPlan? get() = fieldValue("executionPlan")
