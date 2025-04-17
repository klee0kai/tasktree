package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.TaskTreeExtension
import com.github.klee0kai.tasktree.info.TaskStat
import com.github.klee0kai.tasktree.info.TaskStatHelper
import com.github.klee0kai.tasktree.utils.formatString
import org.gradle.api.tasks.TaskAction
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
            tasksStats.groupBy { it.projectDetails }.entries,
            { it.key }
        ) { projectTasks ->
            val topTasks = projectTasks.value
                .filter { task -> task.allDependedOnTasks.none { allRequestedTasksIds.get().contains(it.id) } }
                .sortedBy { it.allDependedOnCount }
            topTasks.forEach { render(it) }

            printMostExpensiveTasksIfNeed()
            printMostExpensiveModulesIfNeed()
        }

    }

    private fun render(taskStat: TaskStat, lastChild: Boolean = true, depth: Int = 0) {
        graphRenderer?.visit({

            printTaskShort(taskStat)

            if (ext.printClassName) {
                withStyle(Description)
                    .text(" class: ${taskStat.className};")
            }

        }, lastChild)

        if ((!ext.printDoubles && taskStat in renderedTasks) || ext.maxDepth in 0..depth) {
            graphRenderer?.startChildren()
            graphRenderer?.visit({
                withStyle(Normal)
                    .text("***")
            }, lastChild)
            graphRenderer?.completeChildren()
            return
        }
        renderedTasks.add(taskStat)

        graphRenderer?.startChildren()
        val depsSize = taskStat.dependencies.size
        taskStat.dependencies.forEachIndexed { indx, it ->
            val lastChild = indx >= depsSize - 1
            render(it, lastChild = lastChild, depth = depth + 1)
        }
        graphRenderer?.completeChildren()
    }

    private fun printMostExpensiveTasksIfNeed() {
        if (ext.printMostExpensiveTasks) {
            val allStat = tasksStats
                .filter { it.complexPrice > 0 }
                .sortedByDescending { it.complexPrice }
            renderer.textOutput
                .println()
                .withStyle(Header)
                .println("Most expensive tasks:")

            allStat.forEach {
                renderer.textOutput
                    .printTaskShort(it)
                    .println()
            }
            renderer.textOutput.println()
        }
    }

    private fun printMostExpensiveModulesIfNeed() {
        if (ext.printMostExpensiveModules) {
            val allStat = tasksStats
                .groupBy { it.projectDetails }
                .map { (pr, stat) -> pr to stat.sumOf { it.complexPriceOutsideProject.toDouble() } }
                .sortedByDescending { (_, price) -> price }

            renderer.textOutput
                .println()
                .withStyle(Header)
                .println("Most expensive modules:")

            allStat.forEach { (proj, price) ->
                renderer.textOutput.apply {
                    withStyle(Identifier)
                        .text(proj?.displayName)

                    withStyle(Description)
                        .text(" complexPrice: ${price.formatString()}")

                    println()
                }
            }
            renderer.textOutput.println()
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


