package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.TaskStat
import com.github.klee0kai.tasktree.TaskTreeExtension
import com.github.klee0kai.tasktree.utils.*
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutput.Style.*
import javax.inject.Inject

open class TaskTreeTask @Inject constructor(
    private val ext: TaskTreeExtension,
) : BaseReportTask() {

    private val renderedTasks = mutableSetOf<Task>()
    private val taskStat = mutableMapOf<Task, TaskStat>()

    override fun generate(project: Project) {
        renderedTasks.clear()
        taskStat.clear()

        val allTasks = project.allRequestedTasks.toSet()
        allTasks.forEach { task ->
            taskStat.putIfAbsent(
                task,
                TaskStat(
                    task = task,
                    allTasks = allTasks,
                    rootProject = project
                )
            )
        }

        //https://docs.gradle.org/current/javadoc/org/gradle/api/execution/TaskExecutionGraph.html#getAllTasks--
        // use sorted list
        allTasks.forEach { task ->
            val stat = taskStat[task]!!
            val deps = project.taskGraph.getDeps(task)
            stat.allDepsCount = deps.count() + deps.sumOf { dep ->
                taskStat[dep]!!.allDepsCount
            }
        }
        allTasks.reversed().forEach { task ->
            val stat = taskStat[task]!!
            project.taskGraph.getDeps(task).forEach {
                val depStat = taskStat[it]!!
                depStat.allDependedOnCount += 1 + stat.allDependedOnCount
                if (depStat.task.project != stat.task.project) {
                    depStat.allDependedOnOutsideProjectCount += 1 + stat.allDependedOnCount
                }
            }

        }

        val topTasks = taskStat.values
            .filter { it.allDependedOnCount <= 0 }
            .map { it.task }
        topTasks.forEach { render(it) }

        printMostExpensiveTasksIfNeed()
        printMostExpensiveModulesIfNeed()


    }

    private fun render(task: Task, lastChild: Boolean = true, depth: Int = 0) {
        graphRenderer?.visit({
            val taskStat = taskStat[task] ?: return@visit

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

    private fun printMostExpensiveTasksIfNeed() {
        if (ext.printMostExpensiveTasks) {
            val allStat = taskStat.values
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
            val allStat = taskStat.values
                .groupBy { it.task.project }
                .map { (pr, stat) -> pr to stat.sumOf { it.complexPriceOutsideProject.toDouble() } }
                .sortedByDescending { (_, price) -> price }

            renderer.textOutput
                .println()
                .withStyle(Header)
                .println("Most expensive modules:")

            allStat.forEach { (proj, price) ->
                renderer.textOutput.apply {
                    withStyle(Identifier)
                        .text(proj.fullName)

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

    private val Task.isIncludedBuild get() = this@TaskTreeTask.project.gradle != project.gradle


}


