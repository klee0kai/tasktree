package com.github.klee0kai.tasktree

import com.github.klee0kai.tasktree.tasks.DiagonDagTask
import com.github.klee0kai.tasktree.tasks.TaskTreeTask
import com.github.klee0kai.tasktree.utils.isDiagonGraphRequested
import com.github.klee0kai.tasktree.utils.isTaskTreeRequested
import com.github.klee0kai.tasktree.utils.requestedTasks
import com.github.klee0kai.tasktree.utils.taskGraph
import org.gradle.api.Plugin
import org.gradle.api.Project

open class TaskTreePlugin : Plugin<Project> {

    override fun apply(project: Project) = project.applyOnProject()

    private fun Project.applyOnProject() {
        val ext = extensions.create("tasktree", TaskTreeExtension::class.java)
        tasks.register("tasktree", TaskTreeTask::class.java, ext)
        tasks.register("diagonDAG", DiagonDagTask::class.java)

        taskGraph.whenReady {
            if (isTaskTreeRequested || isDiagonGraphRequested) {
                requestedTasks?.forEach { it.enabled = false }
            }
        }
    }

}

