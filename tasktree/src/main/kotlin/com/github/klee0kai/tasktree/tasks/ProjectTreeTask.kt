package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.TaskTreeExtension
import com.github.klee0kai.tasktree.projectInfo.ProjectInfo
import com.github.klee0kai.tasktree.projectInfo.ProjectStatHelper
import com.github.klee0kai.tasktree.utils.formatString
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer
import org.gradle.internal.graph.GraphRenderer
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutput.Style.*
import org.gradle.internal.serialization.Cached
import javax.inject.Inject

open class ProjectTreeTask @Inject constructor(
    private val ext: TaskTreeExtension,
) : BaseReportTask() {

    private val renderedProjects = mutableSetOf<ProjectInfo>()

    private val projectsInfos = Cached.of { ProjectStatHelper.collectProjectDependencies(project) }

    private val projectsStats by lazy { ProjectStatHelper.calcToProjectStats(projectsInfos.get()) }

    @TaskAction
    fun generate() {
        renderedProjects.clear()
        reportGenerator().generateReport(
            listOf(projectDetails.get()),
            { it }
        ) { projects ->
            val graphRenderer = GraphRenderer(renderer.textOutput)
            val topProjects = projectsStats
                .filter { project -> project.allDependedOnCount <= 0L }
                .sortedBy { it.allDependedOnCount }
            topProjects.forEach { graphRenderer.render(it) }

            renderer.printMostExpensiveProjectsIfNeed()
        }

    }

    private fun GraphRenderer.render(
        projectStat: ProjectInfo,
        lastChild: Boolean = true,
        depth: Int = 0,
    ) {
        visit({
            printProjectShort(projectStat)

            if (ext.printDetails) {
                withStyle(Description)
                    .text(" class: ${projectStat.path};")
            }

        }, lastChild)

        if (projectStat.dependencies.isEmpty()) return

        if ((!ext.printDoubles && projectStat in renderedProjects) || ext.maxDepth in 0..depth) {
            startChildren()
            visit({
                withStyle(Normal)
                    .text("***")
            }, lastChild)
            completeChildren()
            return
        }
        renderedProjects.add(projectStat)

        startChildren()
        val depsSize = projectStat.dependencies.size
        projectStat.dependencies.forEachIndexed { indx, it ->
            val lastChild = indx >= depsSize - 1
            render(it, lastChild = lastChild, depth = depth + 1)
        }
        completeChildren()
    }

    private fun TextReportRenderer.printMostExpensiveProjectsIfNeed() {
        if (ext.printMostExpensive) {
            val allStat = projectsStats
                .filter { it.complexPrice > 0 }
                .sortedByDescending { it.complexPrice }
            textOutput
                .println()
                .withStyle(Header)
                .println("Most expensive modules:")

            allStat.forEach {
                renderer.textOutput
                    .printProjectShort(it)
                    .println()
            }
            textOutput.println()
        }
    }

    private fun StyledTextOutput.printProjectShort(projectInfo: ProjectInfo) = apply {
        withStyle(Identifier)
            .text(projectInfo.fullName)

        if (ext.printPrice) {
            withStyle(Description)
                .text(" price: ${projectInfo.price};")
        }
        if (ext.printImportance) {
            withStyle(Description)
                .text(" importance: ${projectInfo.importance};")
        }
        if (ext.printComplexPrice) {
            withStyle(Description)
                .text(" complexPrice: ${projectInfo.complexPrice.formatString()};")
        }
    }


}


