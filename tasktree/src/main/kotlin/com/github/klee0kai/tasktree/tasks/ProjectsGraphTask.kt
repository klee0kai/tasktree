package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.projectInfo.ProjectInfo
import com.github.klee0kai.tasktree.projectInfo.ProjectStatHelper
import org.apache.tools.ant.util.TeeOutputStream
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.internal.serialization.Cached
import org.gradle.process.internal.ExecActionFactory
import org.gradle.process.internal.ExecException
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject

open class ProjectsGraphTask @Inject constructor(
    @Input
    val objectFactory: ObjectFactory,
    @Input
    val execAction: ExecActionFactory,
) : BaseReportTask() {

    private val projectsInfos = Cached.of { ProjectStatHelper.collectProjectDependencies(project) }

    private var projectsStats: List<ProjectInfo> = emptyList()

    @Input
    @Optional
    @set:Option(option = "target", description = "Build graph to module as target")
    protected var target: String? = null

    override fun getDescription(): String =
        "Draw project's dependencies graph use Diagon. More: https://github.com/ArthurSonzogni/Diagon"


    @TaskAction
    fun generate() {
        projectsStats = ProjectStatHelper.calcToProjectStats(projectInfos = projectsInfos.get())
            .let { ProjectStatHelper.filterByRequestedProject(projectStats = it, target = target) }

        val depsCode = projectsStats.joinToString("\n") { project ->
            project.dependencies.joinToString("\n") { dep ->
                "${dep.fullName} -> ${project.fullName}"
            }
        }

        val result = sh(cmd = arrayOf("diagon", "GraphDAG"), input = depsCode)

        println("------------------  Diagon DAG graph ----------- ")
        println(result)
        println("------------------  ---------------- ----------- ")
    }


    private fun sh(cmd: Array<String>, input: String): String {
        val execAction = execAction.newExecAction()

        val localErrStream = ByteArrayOutputStream()
        val localOutputStream = ByteArrayOutputStream()

        try {
            execAction.commandLine(*cmd)
            execAction.errorOutput = TeeOutputStream(localErrStream, System.err)
            execAction.standardInput = input.byteInputStream()
            execAction.standardOutput = localOutputStream
            val result = execAction.execute()

            if (result.exitValue != 0) {
                throw ExecException("Cmd ${cmd.joinToString(" ")} finished with exit code ${result.exitValue}")
            }
            return String(localOutputStream.toByteArray())
        } catch (e: Exception) {
            val errStreamText = String(localErrStream.toByteArray())
            throw IOException("can't run ${cmd.joinToString(" ")}\n ${e.message} $errStreamText", e)
        }
    }

}