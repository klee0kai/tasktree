package com.github.klee0kai.tasktree.utils

import org.gradle.api.Task

val Task.fullName
    get() = buildString {
        project.parents
            .toList()
            .reversed()
            .forEach { project ->
                val isRoot = project.parent == null
                if (!isRoot) append(":${project.name}")
            }
        append(":${name}")
    }

