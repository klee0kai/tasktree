package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.TaskStat
import com.github.klee0kai.tasktree.TaskTreeExtension
import com.github.klee0kai.tasktree.utils.allRequestedTasks
import com.github.klee0kai.tasktree.utils.formatString
import com.github.klee0kai.tasktree.utils.fullName
import com.github.klee0kai.tasktree.utils.simpleClassName
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutput.Style.Description
import org.gradle.internal.logging.text.StyledTextOutput.Style.Identifier
import javax.inject.Inject

open class FlatListTask @Inject constructor(
    private val ext: TaskTreeExtension,
) : BaseReportTask() {

    private val statHelper = TaskStatHelper()

    override fun generate(project: Project) {
        statHelper.collectFrom(project)

        val allTasksOrdered = project.allRequestedTasks
        allTasksOrdered.forEach {
            render(
                task = it,
                lastChild = true,
                depth = 0,
            )
        }

    }

    private fun render(task: Task, lastChild: Boolean = true, depth: Int = 0) {
        graphRenderer?.visit({
            val taskStat = statHelper.taskStat[task] ?: return@visit

            printTaskShort(taskStat)

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
    }

    private fun StyledTextOutput.printTaskShort(taskStat: TaskStat) = apply {
        withStyle(Identifier)
            .text(taskStat.task.fullName)

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

    private val Task.isIncludedBuild get() = this@FlatListTask.project.gradle != project.gradle


}


