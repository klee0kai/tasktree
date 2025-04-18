package com.github.klee0kai.tasktree.info

import org.gradle.api.tasks.diagnostics.internal.ProjectDetails

class TaskInfo(
    val id: Int = 0,
    val taskName: String,
    val className: Class<*>? = null,
    val simpleClassName: String? = null,
    val projectName: String? = null,
    val projectDetails: ProjectDetails? = null,
    val rootProjectDetails: ProjectDetails? = null,
    val dependencies: MutableList<TaskInfo> = mutableListOf()
)

