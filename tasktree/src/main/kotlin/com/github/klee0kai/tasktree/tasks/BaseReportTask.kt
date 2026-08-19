package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.utils.allRequestedTasks
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.diagnostics.ConventionReportTask
import org.gradle.api.tasks.diagnostics.internal.ProjectDetails
import org.gradle.api.tasks.diagnostics.internal.ReportGenerator
import org.gradle.api.tasks.diagnostics.internal.ReportRenderer
import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer
import org.gradle.internal.serialization.Cached

abstract class BaseReportTask : ConventionReportTask() {

    @get:Internal
    protected val projectDetails = Cached.of { ProjectDetails.of(project) }

    @get:Internal
    protected val allRequestedTasksIds = Cached.of {
        project.allRequestedTasks.map { it.path }.toSet()
    }

    @Internal
    protected val renderer = TextReportRenderer()

    override fun getRenderer(): ReportRenderer = renderer

    protected open fun reportGenerator(): ReportGenerator {
        return ReportGenerator(
            getRenderer(),
            outputFile,
            textOutputFactory
        )
    }
}