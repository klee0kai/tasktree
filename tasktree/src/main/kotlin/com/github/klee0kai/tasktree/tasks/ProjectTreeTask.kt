package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.TaskTreeExtension
import com.github.klee0kai.tasktree.projectInfo.ProjectInfo
import com.github.klee0kai.tasktree.projectInfo.ProjectStatHelper
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

open class ProjectTreeTask @Inject constructor(
    private val ext: TaskTreeExtension,
) : BaseReportTask() {

    private val renderedProjects = mutableSetOf<ProjectInfo>()

    private val projectsInfos = Cached.of { ProjectStatHelper.collectProjectDependencies(project) }

    private var projectsStats: List<ProjectInfo> = emptyList()

    @Input
    @Optional
    @set:Option(option = "verifyDepth", description = "Verify project's module depth")
    protected var verifyDepth: String? = null

    @Input
    @Optional
    @set:Option(option = "verifyPrice", description = "Verify project's module price")
    protected var verifyPrice: String? = null

    @TaskAction
    fun generate() {
        projectsStats = ProjectStatHelper.calcToProjectStats(projectsInfos.get())

        renderedProjects.clear()
        reportGenerator().generateReport(
            listOf(projectDetails.get()),
            { it }
        ) { projects ->
            val graphRenderer = GraphRenderer(renderer.textOutput)
            val topProjects = projectsStats
                .filter { project -> project.allDependedOnCount <= 0L }
                .sortedByDescending { it.depth }
            topProjects.forEach { graphRenderer.render(it) }

            renderer.printMostExpensiveProjectsIfNeed()
            verifyIfNeed()
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
                .filter { it.depth > 0 }
                .sortedByDescending { it.depth }
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

            withStyle(Description)
                .text(" depth: ${projectInfo.depth};")
        }
        if (ext.printImportance) {
            withStyle(Description)
                .text(" importance: ${projectInfo.importance};")
        }
        if (ext.printRelativePrice) {
            withStyle(Description)
                .text(" relativePrice: ${projectInfo.relativePrice.formatString()};")

            withStyle(Description)
                .text(" relativeDepth: ${projectInfo.relativeDepth.formatString()};")
        }
    }


    private fun verifyIfNeed() {
        val verifyDepth = verifyDepth?.toInt() ?: return
        var heavyProjects = projectsStats.filter { it.depth > verifyDepth }
        if (heavyProjects.isNotEmpty()) {
            throw IllegalStateException("Heavy projects: ${heavyProjects.joinToString("\n") { "'${it.fullName}' depth: ${it.depth}" }}")
        }

        val verifyPrice = verifyPrice?.toInt() ?: return
        heavyProjects = projectsStats.filter { it.price > verifyPrice }
        if (heavyProjects.isNotEmpty()) {
            throw IllegalStateException("Heavy projects: ${heavyProjects.joinToString("\n") { "'${it.fullName}' price : ${it.price}" }}")
        }
    }

}


