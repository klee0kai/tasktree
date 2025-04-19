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
    var maxDepth: Int = 0

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
            field = allDependedOnProject.count()
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

    val allDependedOnProject = sequence {
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

    var depth: Int = 0
        private set
        get() {
            if (field != 0) return field
            val checked = mutableMapOf<String, Int>()
            val deps = LinkedList(dependencies.map { it to 2 }.toMutableList())
            while (deps.isNotEmpty()) {
                val dep = deps.pollFirst()
                if (checked.getOrDefault(dep.first.path, 0) >= dep.second) continue
                checked[dep.first.path] = dep.second
                deps.addAll(0, dep.first.dependencies.map { it to dep.second + 1 })
            }
            field = checked.values.maxOfOrNull { it } ?: 0
            return field
        }

    val depthDependencies: List<ProjectInfo>
        get() {
            val checked = mutableMapOf<String, List<ProjectInfo>>()
            val deps = LinkedList(dependencies.map { listOf(this@ProjectInfo, it) }.toMutableList())
            while (deps.isNotEmpty()) {
                val dep = deps.pollFirst()
                if (checked.getOrDefault(dep.last().path, emptyList()).size >= dep.size) continue
                checked[dep.last().path] = dep
                deps.addAll(0, dep.last().dependencies.map { dep + it })
            }
            return checked.values.maxByOrNull { it.size } ?: emptyList()
        }


    val price get() = allDepsCount + 1
    val importance get() = allDependedOnCount

    val relativePrice get() = price.toFloat() / maxPrice

    val relativeDepth get() = depth.toFloat() / maxDepth

}

