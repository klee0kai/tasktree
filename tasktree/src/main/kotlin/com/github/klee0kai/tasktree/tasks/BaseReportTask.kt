package com.github.klee0kai.tasktree.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.diagnostics.ConventionReportTask
import org.gradle.api.tasks.diagnostics.internal.ReportGenerator
import org.gradle.api.tasks.diagnostics.internal.ReportRenderer
import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer
import org.gradle.internal.graph.GraphRenderer

abstract class BaseReportTask : ConventionReportTask() {

    @Internal
    protected val renderer = TextReportRenderer()

    @get:Internal
    protected val graphRenderer: GraphRenderer? by lazy { GraphRenderer(renderer.textOutput) }

    abstract fun generate(project: Project)

    @TaskAction
    fun generate() {
        reportGenerator().generateReport(projects) {
            generate(it)
        }
    }

    override fun getRenderer(): ReportRenderer = renderer

    private fun reportGenerator(): ReportGenerator {
        return ReportGenerator(
            getRenderer(),
            clientMetaData,
            outputFile,
            textOutputFactory
        )
    }
}