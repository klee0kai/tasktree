package com.github.klee0kai.tasktree

import org.gradle.api.Plugin
import org.gradle.api.Project

open class TaskTreePlugin : Plugin<Project> {

    override fun apply(project: Project) = project.applyOnProject()

    private fun Project.applyOnProject() {

    }

}

