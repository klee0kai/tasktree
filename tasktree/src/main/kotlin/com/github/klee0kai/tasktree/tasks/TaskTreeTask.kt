package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.TaskTreeExtension
import com.github.klee0kai.tasktree.info.TaskStat
import com.github.klee0kai.tasktree.info.TaskStatHelper
import com.github.klee0kai.tasktree.utils.formatString
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer
import org.gradle.api.tasks.options.Option
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

    private var tasksStats: List<TaskStat> = emptyList()

    @Input
    @Optional
    @set:Option(option = "verifyDepth", description = "Verify tasks depth")
    protected var verifyDepth: String? = null

    @Input
    @Optional
    @set:Option(option = "verifyPrice", description = "Verify tasks price")
    protected var verifyPrice: String? = null

    @TaskAction
    fun generate() {
        tasksStats = TaskStatHelper.calcToTaskStats(tasksInfos.get())
            .let { TaskStatHelper.filterByRequestedTasks(it, allRequestedTasksIds.get()) }

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
                .sortedByDescending { it.depth }
            topTasks.forEach { graphRenderer.render(it) }

            renderer.printMostExpensiveTasksIfNeed()
            verifyIfNeed()
        }
    }

    private fun GraphRenderer.render(taskStat: TaskStat, lastChild: Boolean = true, depth: Int = 0) {
        visit({

            printTaskShort(taskStat)

            if (ext.printDetails) {
                withStyle(Description)
                    .text(" class: ${taskStat.className};")
            }

        }, lastChild)

        if (taskStat.dependencies.isEmpty()) return

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
        if (ext.printMostExpensive) {
            val allStat = tasksStats
                .filter { it.depth > 0 }
                .sortedByDescending { it.depth }
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


    private fun StyledTextOutput.printTaskShort(taskStat: TaskStat) = apply {
        withStyle(Identifier)
            .text(taskStat.fullName)

        if (ext.printPrice) {
            withStyle(Description)
                .text(" price: ${taskStat.price};")
            withStyle(Description)
                .text(" depth: ${taskStat.depth};")
        }
        if (ext.printImportance) {
            withStyle(Description)
                .text(" importance: ${taskStat.importance};")
        }
        if (ext.printRelativePrice) {
            withStyle(Description)
                .text(" relativePrice: ${taskStat.relativePrice.formatString()};")

            withStyle(Description)
                .text(" relativeDepth: ${taskStat.relativeDepth.formatString()};")
        }
    }


    private fun verifyIfNeed() {
        val verifyDepth = verifyDepth?.toInt() ?: return
        var heavyTasks = tasksStats.filter { it.depth > verifyDepth }
        if (heavyTasks.isNotEmpty()) {
            throw IllegalStateException("Heavy tasks: ${heavyTasks.joinToString("\n") { "'${it.fullName}' depth : ${it.depth}" }}")
        }

        val verifyPrice = verifyPrice?.toInt() ?: return
        heavyTasks = tasksStats.filter { it.price > verifyPrice }
        if (heavyTasks.isNotEmpty()) {
            throw IllegalStateException("Heavy tasks: ${heavyTasks.joinToString("\n") { "'${it.fullName}' price: ${it.price}" }}")
        }
    }


}


