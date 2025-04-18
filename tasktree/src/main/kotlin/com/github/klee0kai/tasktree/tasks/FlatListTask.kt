package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.TaskTreeExtension
import com.github.klee0kai.tasktree.info.TaskStat
import com.github.klee0kai.tasktree.info.TaskStatHelper
import com.github.klee0kai.tasktree.utils.formatString
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.graph.GraphRenderer
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutput.Style.Description
import org.gradle.internal.logging.text.StyledTextOutput.Style.Identifier
import org.gradle.internal.serialization.Cached
import javax.inject.Inject

open class FlatListTask @Inject constructor(
    private val ext: TaskTreeExtension,
) : BaseReportTask() {

    private val tasksInfos = Cached.of { TaskStatHelper.collectAllTasksInfo(project) }

    private val tasksStats by lazy {
        TaskStatHelper.calcToTaskStats(tasksInfos.get())
            .let { TaskStatHelper.filterByRequestedTasks(it, allRequestedTasksIds.get()) }
    }

    @TaskAction
    fun generate() {
        reportGenerator().generateReport(
            listOf(projectDetails.get()),
            { it }
        ) { projectTasks ->
            val graphRenderer = GraphRenderer(renderer.textOutput)

            tasksStats
                .sortedBy { it.allDepsCount }
                .forEach { taskStat ->
                    graphRenderer.visit({
                        printTaskShort(taskStat)

                        if (ext.printDetails) {
                            withStyle(Description)
                                .text(" class: ${taskStat.simpleClassName};")
                        }

                    }, /*last child */ true)

                }
        }
    }


    private fun StyledTextOutput.printTaskShort(taskStat: TaskStat) = apply {
        withStyle(Identifier)
            .text(taskStat.taskName)

        if (ext.printPrice) {
            withStyle(Description)
                .text(" price: ${taskStat.price};")
        }
        if (ext.printImportance) {
            withStyle(Description)
                .text(" importance: ${taskStat.importance};")
        }
        if (ext.printRelativePrice) {
            withStyle(Description)
                .text(" relativePrice: ${taskStat.relativePrice.formatString()};")
        }
    }

}


