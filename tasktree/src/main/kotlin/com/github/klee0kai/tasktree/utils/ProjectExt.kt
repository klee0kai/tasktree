package com.github.klee0kai.tasktree.utils

import com.github.klee0kai.tasktree.tasks.TaskTreeTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.execution.taskgraph.DefaultTaskExecutionGraph

val Task.simpleClassName get() = this.javaClass.simpleName.removeSuffix("_Decorated")

val Project.taskGraph get() = gradle.taskGraph as DefaultTaskExecutionGraph

val Project.executionPlan get() = taskGraph.executionPlan

val Project.isTaskTreeRequested get() = executionPlan?.requestedTasks?.any { it is TaskTreeTask } ?: false

val Project.requestedTasks get() = executionPlan?.requestedTasks?.filter { it !is TaskTreeTask }