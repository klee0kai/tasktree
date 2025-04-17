package com.github.klee0kai.tasktree

import com.github.klee0kai.tasktree.tasks.DiagonDagTask
import com.github.klee0kai.tasktree.tasks.FlatListTask
import com.github.klee0kai.tasktree.tasks.TaskTreeTask
import com.github.klee0kai.tasktree.utils.allRequestedTasks
import com.github.klee0kai.tasktree.utils.taskGraph
import org.gradle.api.Plugin
import org.gradle.api.Project

open class TaskTreePlugin : Plugin<Project> {

    override fun apply(project: Project) = project.applyOnProject()

    private fun Project.applyOnProject() {
        val ext = extensions.create("tasktree", TaskTreeExtension::class.java)
        afterEvaluate {
            val taskTree = tasks.register("tasktree", TaskTreeTask::class.java, ext)
            val taskDag = tasks.register("diagonDAG", DiagonDagTask::class.java)
            val flatlist = tasks.register("flatlist", FlatListTask::class.java, ext)

            taskGraph.whenReady {
                val isTaskTreeRequested = hasTask(taskTree.get()) || hasTask(taskDag.get()) || hasTask(flatlist.get())
                if (isTaskTreeRequested) {
                    allRequestedTasks.forEach {
                        it.enabled = false
                    }
                }
            }
        }
    }

}

