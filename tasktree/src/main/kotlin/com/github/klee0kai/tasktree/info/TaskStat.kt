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
) {

    var allTasksCount: Int = 0
    var maxPrice: Int = 0
    var maxDepth: Int = 0

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

    var depth: Int = 0
        private set
        get() {
            if (field != 0) return field
            var maxDepth = 1
            val checked = mutableSetOf<Int>()
            val deps = LinkedList(dependencies.map { it to 2 }.toMutableList())
            while (deps.isNotEmpty()) {
                val dep = deps.pollFirst()
                if (checked.contains(dep.first.id)) continue
                if (dep.second > maxDepth) maxDepth = dep.second
                checked.add(dep.first.id)
                deps.addAll(0, dep.first.dependencies.map { it to dep.second + 1 })
            }
            field = maxDepth
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


    val price get() = allDepsCount + 1
    val importance get() = allDependedOnCount

    val relativePrice get() = price.toFloat() / maxPrice.toFloat()

    val relativeDepth get() = depth.toFloat() / maxDepth.toFloat()

}


fun TaskInfo.toTaskStat(
) = TaskStat(
    id,
    taskName,
    className,
    simpleClassName,
    projectName,
    projectDetails,
    rootProjectDetails,
)