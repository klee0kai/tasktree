package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.TaskTreeExtension
import com.github.klee0kai.tasktree.info.TaskStat
import com.github.klee0kai.tasktree.info.TaskStatHelper
import com.github.klee0kai.tasktree.utils.formatString
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutput.Style.Description
import org.gradle.internal.logging.text.StyledTextOutput.Style.Identifier
import org.gradle.internal.serialization.Cached
import javax.inject.Inject

open class FlatListTask @Inject constructor(
    private val ext: TaskTreeExtension,
) : BaseReportTask() {

    private val renderedTasks = mutableSetOf<TaskStat>()

    private val tasksInfos = Cached.of { TaskStatHelper.collectAllTasksInfo(project) }

    private val tasksStats by lazy { TaskStatHelper.calcToTaskStats(tasksInfos.get()) }

    @TaskAction
    fun generate() {
        reportGenerator().generateReport(
            tasksStats.groupBy { it.projectDetails }.entries,
            { it.key }
        ) { projectTasks ->
            projectTasks.value
                .sortedBy { it.allDepsCount }
                .forEach { taskStat ->
                    graphRenderer?.visit({
                        printTaskShort(taskStat)

                        if (ext.printClassName) {
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


