package com.github.klee0kai.tasktree

import com.github.klee0kai.tasktree.tasks.*
import com.github.klee0kai.tasktree.utils.allRequestedTasks
import com.github.klee0kai.tasktree.utils.taskGraph
import org.gradle.api.Plugin
import org.gradle.api.Project

open class TaskTreePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val ext = project.extensions.create("tasktree", TaskTreeExtension::class.java)
        project.rootProject.applyProjectReportOnProject(ext)
        project.allprojects.forEach { project -> project.applyTaskReportOnProject(ext) }
    }

    private fun Project.applyProjectReportOnProject(ext: TaskTreeExtension) {
        tasks.register("projectTree", ProjectTreeTask::class.java, ext)
        tasks.register("projectGraph", ProjectsGraphTask::class.java)
    }


    private fun Project.applyTaskReportOnProject(ext: TaskTreeExtension) {
        afterEvaluate {
            val taskTree = tasks.register("taskTree", TaskTreeTask::class.java, ext)
            val taskDag = tasks.register("taskGraph", TaskGraphTask::class.java)
            val flatlist = tasks.register("flatList", FlatListTask::class.java, ext)

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

