package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.TaskTreeExtension
import com.github.klee0kai.tasktree.info.TaskStat
import com.github.klee0kai.tasktree.info.TaskStatHelper
import com.github.klee0kai.tasktree.utils.formatString
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer
import org.gradle.internal.graph.GraphRenderer
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutput.Style.*
import org.gradle.internal.serialization.Cached
import javax.inject.Inject

open class TaskTreeTask @Inject constructor(
    private val ext: TaskTreeExtension,
) : BaseReportTask() {

    private val renderedTasks = mutableSetOf<TaskStat>()

    private val tasksInfos = Cached.of { TaskStatHelper.collectAllTasksInfo(project) }

    private val tasksStats by lazy {
        TaskStatHelper.calcToTaskStats(tasksInfos.get())
            .let { TaskStatHelper.filterByRequestedTasks(it, allRequestedTasksIds.get()) }
    }

    @TaskAction
    fun generate() {
        renderedTasks.clear()
        reportGenerator().generateReport(
            listOf(projectDetails.get()),
            { it }
        ) { projectTasks ->
            val graphRenderer = GraphRenderer(renderer.textOutput)
            val topTasks = tasksStats
                .filter { task ->
                    if (allRequestedTasksIds.get().isNotEmpty()) {
                        task.dependedOnTasks.none { allRequestedTasksIds.get().contains(it.id) }
                    } else {
                        task.allDependedOnCount <= 0L
                    }
                }
                .sortedBy { it.allDependedOnCount }
            topTasks.forEach { graphRenderer.render(it) }

            renderer.printMostExpensiveTasksIfNeed()
            renderer.printMostExpensiveModulesIfNeed()
        }

    }

    private fun GraphRenderer.render(taskStat: TaskStat, lastChild: Boolean = true, depth: Int = 0) {
        visit({

            printTaskShort(taskStat)

            if (ext.printClassName) {
                withStyle(Description)
                    .text(" class: ${taskStat.className};")
            }

        }, lastChild)

        if ((!ext.printDoubles && taskStat in renderedTasks) || ext.maxDepth in 0..depth) {
            startChildren()
            visit({
                withStyle(Normal)
                    .text("***")
            }, lastChild)
            completeChildren()
            return
        }
        renderedTasks.add(taskStat)

        startChildren()
        val depsSize = taskStat.dependencies.size
        taskStat.dependencies.forEachIndexed { indx, it ->
            val lastChild = indx >= depsSize - 1
            render(it, lastChild = lastChild, depth = depth + 1)
        }
        completeChildren()
    }

    private fun TextReportRenderer.printMostExpensiveTasksIfNeed() {
        if (ext.printMostExpensiveTasks) {
            val allStat = tasksStats
                .filter { it.complexPrice > 0 }
                .sortedByDescending { it.complexPrice }
            textOutput
                .println()
                .withStyle(Header)
                .println("Most expensive tasks:")

            allStat.forEach {
                renderer.textOutput
                    .printTaskShort(it)
                    .println()
            }
            textOutput.println()
        }
    }

    private fun TextReportRenderer.printMostExpensiveModulesIfNeed() {
        if (ext.printMostExpensiveModules) {
            val allStat = tasksStats
                .groupBy { it.projectDetails }
                .map { (pr, stat) -> pr to stat.sumOf { it.complexPriceOutsideProject.toDouble() } }
                .sortedByDescending { (_, price) -> price }

            textOutput
                .println()
                .withStyle(Header)
                .println("Most expensive modules:")

            allStat.forEach { (proj, price) ->
                textOutput.apply {
                    withStyle(Identifier)
                        .text(proj?.displayName)

                    withStyle(Description)
                        .text(" complexPrice: ${price.formatString()}")

                    println()
                }
            }
            textOutput.println()
        }
    }

    private fun StyledTextOutput.printTaskShort(taskStat: TaskStat) = apply {
        withStyle(Identifier)
            .text(taskStat.fullName)

        if (ext.printPrice) {
            withStyle(Description)
                .text(" price: ${taskStat.price};")
        }
        if (ext.printImportance) {
            withStyle(Description)
                .text(" importance: ${taskStat.importance};")
        }
        if (ext.printImportanceOutSide) {
            withStyle(Description)
                .text(" importance outside: ${taskStat.importanceOutsideProject};")
        }
        if (ext.printComplexPrice) {
            withStyle(Description)
                .text(" complexPrice: ${taskStat.complexPrice.formatString()};")
        }
    }


}


