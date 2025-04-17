package com.github.klee0kai.tasktree.info

import org.gradle.api.tasks.diagnostics.internal.ProjectDetails

class TaskStat(
    val id: Int = 0,
    val taskName: String,
    val className: Class<*>? = null,
    val simpleClassName: String? = null,
    val projectName: String? = null,
    val projectDetails: ProjectDetails? = null,
    val rootProjectDetails: ProjectDetails? = null,
    val dependencies: MutableList<TaskStat> = mutableListOf(),
    val dependedOnTasks: MutableList<TaskStat> = mutableListOf(),
) {

    val fullName: String = "${projectName}:${taskName}"

    var allDepsCount: Long = 0
        private set
        get() {
            if (field != 0L) return field
            field = dependencies.sumOf { 1 + it.allDepsCount }
            return field
        }

    var allDependedOnCount: Long = 0
        private set
        get() {
            if (field != 0L) return field
            field = dependedOnTasks.sumOf { 1 + it.allDependedOnCount }
            return field
        }


    var allDependedOnOutsideProjectCount: Long = 0
        private set
        get() {
            if (field != 0L) return field
            field = dependedOnTasks.filter { it.projectDetails != projectDetails }.sumOf { 1 + it.allDependedOnCount }
            return field
        }


    val price get() = allDepsCount
    val importance get() = allDependedOnCount

    val complexPrice get() = (price * importance).toFloat() / dependencies.size


    // ---- outside of project ------
    val importanceOutsideProject get() = allDependedOnOutsideProjectCount
    val complexPriceOutsideProject get() = (price * importanceOutsideProject).toFloat() / dependencies.size


}


fun TaskInfo.toTaskStat() =
    TaskStat(
        id,
        taskName,
        className,
        simpleClassName,
        projectName,
        projectDetails,
        rootProjectDetails
    )