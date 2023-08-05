package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.TaskTreeExtension
import com.github.klee0kai.tasktree.utils.requestedTasks
import com.github.klee0kai.tasktree.utils.simpleClassName
import com.github.klee0kai.tasktree.utils.taskGraph
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.diagnostics.ProjectBasedReportTask
import org.gradle.api.tasks.diagnostics.internal.ReportRenderer
import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer
import org.gradle.internal.graph.GraphRenderer
import org.gradle.internal.logging.text.StyledTextOutput.Style.*
import javax.inject.Inject

open class TaskTreeTask @Inject constructor(
    private val ext: TaskTreeExtension
) : ProjectBasedReportTask() {

    private val renderer = TextReportRenderer()
    private val graphRenderer: GraphRenderer? by lazy { GraphRenderer(renderer.textOutput) }
    private val renderedTasks = HashSet<Task>()

    override fun getRenderer(): ReportRenderer = renderer

    override fun generate(project: Project) {
        project.requestedTasks?.forEach { render(it) }
    }

    private fun render(task: Task, lastChild: Boolean = true, depth: Int = 0) {
        graphRenderer?.visit({

            withStyle(Identifier)
                .text(task.name)

            if (ext.printClassName) {
                withStyle(Description)
                    .text(" class: ${task.simpleClassName}")
            }

            if (task.isIncludedBuild) {
                withStyle(Description)
                    .text(" (included build '${task.project.gradle.rootProject.name}')")
            }

            val inputs by lazy { task.inputs.files.files }
            if (ext.inputs && inputs.isNotEmpty())
                withStyle(Description)
                    .text(" inputs: [ ${inputs.joinToString { it.path }} ] ")

            val outputs by lazy { task.outputs.files.files }
            if (ext.outputs && outputs.isNotEmpty())
                withStyle(Description)
                    .text(" outputs: [ ${outputs.joinToString { it.path }} ] ")

        }, lastChild)

        if (task in renderedTasks || ext.maxDepth in 0..depth) {
            graphRenderer?.startChildren()
            graphRenderer?.visit({
                withStyle(Normal)
                    .text("***")
            }, lastChild)
            graphRenderer?.completeChildren()
            return
        }

        graphRenderer?.startChildren()
        try {
            val deps = project.taskGraph.getDependencies(task)
            val depsSize = deps.size
            deps.forEachIndexed { indx, it ->
                val lastChild = indx >= depsSize - 1
                render(it, lastChild = lastChild, depth = depth + 1)
            }
        } catch (ignore: Exception) {
            //ignore non available info
        }
        graphRenderer?.completeChildren()

    }

    private val Task.isIncludedBuild get() = this@TaskTreeTask.project.gradle != project.gradle


}


