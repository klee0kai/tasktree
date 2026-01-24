package com.github.klee0kai.tasktree.output

import com.github.klee0kai.tasktree.info.TaskInfo
import kotlinx.serialization.Serializable

@Serializable
data class TaskNode(
    val path: String,
    val name: String,
    val className: String,
    val projectName: String,
    val group: String,
    val description: String,
    val termDir: String,
    val dependencies: List<String> = mutableListOf(),
)


fun TaskInfo.toTaskNode(
): TaskNode = TaskNode(
    path = path,
    name = taskName,
    group = group,
    description = description,
    termDir = termDir,
    className = className!!.canonicalName,
    projectName = projectName ?: "",
    dependencies = dependencies.map { it.path }.toSet().toList(),
)
