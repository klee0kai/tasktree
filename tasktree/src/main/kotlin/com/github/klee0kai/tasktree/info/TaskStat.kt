package com.github.klee0kai.tasktree.info

import java.util.*

class TaskStat(
    val path: String,
    val taskName: String,
    val className: Class<*>? = null,
    val simpleClassName: String? = null,
    val projectName: String? = null,
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

    val depth: Int get() = depthDependencies.size

    var depthDependencies: List<TaskStat> = emptyList()
        private set
        get() {
            if (field.isNotEmpty()) return field
            val checked = mutableMapOf<String, List<TaskStat>>()
            val deps = LinkedList(dependencies.map { listOf(this@TaskStat, it) }.toMutableList())
            while (deps.isNotEmpty()) {
                val dep = deps.pollFirst()
                val checkedDepthDeps = checked.getOrDefault(dep.last().path, emptyList())
                if (checkedDepthDeps.size >= dep.size
                    || checkedDepthDeps.isNotEmpty()
                    && dep.take(checkedDepthDeps.size)
                        .map { it.path } == checkedDepthDeps.map { it.path } // ignore doubles
                ) {
                    continue
                }
                checked[dep.last().path] = dep
                deps.removeAll { task -> task.last().path in dep.last().dependencies.map { it.path } }
                deps.addAll(0, dep.last().dependencies.map { dep + it })
            }
            field = checked.values.maxByOrNull { it.size } ?: emptyList()
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
        val deps = LinkedList(dependedOnTasks.toMutableList())
        while (deps.isNotEmpty()) {
            val dep = deps.pollFirst()
            if (sent.contains(dep.path)) continue
            yield(dep)
            sent.add(dep.path)
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
    path = path,
    taskName = taskName,
    className = className,
    simpleClassName = simpleClassName,
    projectName = projectName,
)