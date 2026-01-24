package com.github.klee0kai.tasktree.output

import kotlinx.serialization.Serializable

@Serializable
data class TaskTreeResult(
    val projects: List<ProjectNode>,
    val tasks: List<TaskNode>,
)
