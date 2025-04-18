package com.github.klee0kai.tasktree.info

import org.gradle.api.tasks.diagnostics.internal.ProjectDetails
import java.util.*

class TaskStat(
    val id: Int = 0,
    val taskName: String,
    val className: Class<*>? = null,
    val simpleClassName: String? = null,
    val projectName: String? = null,
    val projectDetails: ProjectDetails? = null,
    val rootProjectDetails: ProjectDetails? = null,
    val dependencies: MutableSet<TaskStat> = mutableSetOf(),
    val dependedOnTasks: MutableSet<TaskStat> = mutableSetOf(),
    val allTasksCount: Int = 0,
) {

    val fullName: String = "${projectName}:${taskName}"

    var allDepsCount: Int = 0
        private set
        get() {
            if (field != 0) return field
            field = allDependencies.count()
            return field
        }

    var allDependedOnCount: Int = 0
        private set
        get() {
            if (field != 0) return field
            field = allDependedOnTasks.count()
            return field
        }


    var allDependedOnOutsideProjectCount: Int = 0
        private set
        get() {
            if (field != 0) return field
            field = allDependedOnTasks.count { it.projectDetails != projectDetails }
            return field
        }

    val allDependencies = sequence {
        val sent = mutableSetOf<Int>()
        val deps = LinkedList(dependencies.toMutableList())
        while (deps.isNotEmpty()) {
            val dep = deps.pollFirst()
            if (sent.contains(dep.id)) continue
            yield(dep)
            sent.add(dep.id)
            deps.addAll(dep.dependencies)
        }
    }

    val allDependedOnTasks = sequence {
        val sent = mutableSetOf<Int>()
        val deps = LinkedList(dependedOnTasks.toMutableList())
        while (deps.isNotEmpty()) {
            val dep = deps.pollFirst()
            if (sent.contains(dep.id)) continue
            yield(dep)
            sent.add(dep.id)
            deps.addAll(dep.dependedOnTasks)
        }
    }


    val price get() = allDepsCount
    val importance get() = allDependedOnCount

    val complexPrice get() = (price * importance).toFloat() / allTasksCount


}


fun TaskInfo.toTaskStat(
    allTasksCount: Int = 0,
) =
    TaskStat(
        id,
        taskName,
        className,
        simpleClassName,
        projectName,
        projectDetails,
        rootProjectDetails,
        allTasksCount = allTasksCount,
    )