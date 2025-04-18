package com.github.klee0kai.tasktree.projectInfo

import org.gradle.api.tasks.diagnostics.internal.ProjectDetails
import java.util.*

class ProjectInfo(
    val name: String,
    val fullName: String,
    val path: String,
    val projectDetails: ProjectDetails,
    val dependencies: MutableSet<ProjectInfo> = mutableSetOf(),
    val dependedOnProjects: MutableSet<ProjectInfo> = mutableSetOf(),
) {
    var allProjectsCount: Int = 0
    var maxPrice: Int = 0

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


    val allDependencies = sequence {
        val sent = mutableSetOf<String>()
        val deps = LinkedList(dependencies.toMutableList())
        while (deps.isNotEmpty()) {
            val dep = deps.pollFirst()
            if (sent.contains(dep.path)) continue
            yield(dep)
            sent.add(dep.path)
            deps.addAll(dep.dependencies)
        }
    }

    val allDependedOnTasks = sequence {
        val sent = mutableSetOf<String>()
        val deps = LinkedList(dependedOnProjects.toMutableList())
        while (deps.isNotEmpty()) {
            val dep = deps.pollFirst()
            if (sent.contains(dep.path)) continue
            yield(dep)
            sent.add(dep.path)
            deps.addAll(dep.dependedOnProjects)
        }
    }


    val price get() = allDepsCount + 1
    val importance get() = allDependedOnCount

    val relativePrice get() = price.toFloat() / maxPrice

}

