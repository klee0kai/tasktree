package com.github.klee0kai.tasktree.utils

import org.gradle.api.Task

val Task.fullName get() = "${project.fullName}:${name}"
