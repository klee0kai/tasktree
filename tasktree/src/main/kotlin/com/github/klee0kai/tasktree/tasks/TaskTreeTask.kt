package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.TaskTreeExtension
import com.github.klee0kai.tasktree.utils.*
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
    private val renderedTasks = mutableSetOf<Task>()
    private val allTasks = mutableSetOf<Task>()

    override fun getRenderer(): ReportRenderer = renderer

    override fun generate(project: Project) {
        project.requestedTasks?.flatMap {
            setOf(it) + project.taskGraph.getAllDeps(it)
        }?.let { allTasks.addAll(it) }

        project.requestedTasks?.forEach { render(it) }


        renderedTasks.clear()
        allTasks.clear()
    }

    private fun render(task: Task, lastChild: Boolean = true, depth: Int = 0) {
        graphRenderer?.visit({

            val allDeps by lazy { project.taskGraph.getAllDeps(task) }
            val dependedCount by lazy {
                allTasks.count {
                    project.taskGraph
                        .getDeps(it)
                        .contains(task)
                }
            }
            val complexPrice by lazy {
                val depsCount = allDeps.size.toFloat()
                val allTasksCount = allTasks.count().toFloat()
                depsCount * (dependedCount / allTasksCount)
            }

            withStyle(Identifier)
                .text(task.fullName)

            if (ext.printPrice) {
                withStyle(Description)
                    .text(" price: ${allDeps.size};")
            }
            if (ext.printWeight) {
                withStyle(Description)
                    .text(" weight: $dependedCount;")
            }
            if (ext.printComplexPrice) {
                withStyle(Description)
                    .text(" complexPrice: $complexPrice;")
            }
            if (ext.printClassName) {
                withStyle(Description)
                    .text(" class: ${task.simpleClassName};")
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

        if ((!ext.printDoubles && task in renderedTasks) || ext.maxDepth in 0..depth) {
            graphRenderer?.startChildren()
            graphRenderer?.visit({
                withStyle(Normal)
                    .text("***")
            }, lastChild)
            graphRenderer?.completeChildren()
            return
        }
        renderedTasks.add(task)

        graphRenderer?.startChildren()
        val deps = project.taskGraph.getDeps(task)
        val depsSize = deps.size
        deps.forEachIndexed { indx, it ->
            val lastChild = indx >= depsSize - 1
            render(it, lastChild = lastChild, depth = depth + 1)
        }
        graphRenderer?.completeChildren()
    }

    private val Task.isIncludedBuild get() = this@TaskTreeTask.project.gradle != project.gradle


}


