package com.github.klee0kai.tasktree.info

class TaskInfo(
    val path: String,
    val taskName: String,
    val group: String,
    val description: String,
    val termDir: String,
    val className: Class<*>? = null,
    val simpleClassName: String? = null,
    val projectName: String? = null,
    val dependencies: MutableList<TaskInfo> = mutableListOf(),
)

