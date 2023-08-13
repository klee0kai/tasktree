package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.utils.*
import org.apache.tools.ant.util.TeeOutputStream
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.diagnostics.ProjectReportTask
import org.gradle.process.internal.ExecActionFactory
import org.gradle.process.internal.ExecException
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject

open class DiagonDagTask @Inject constructor(
    @Input
    val objectFactory: ObjectFactory,
    @Input
    val execAction: ExecActionFactory,
) : ProjectReportTask() {

    @Internal
    override fun getDescription(): String =
        "Draw tasktree graph use Diagon. More: https://github.com/ArthurSonzogni/Diagon"


    override fun generate(project: Project) {
        super.generate(project)
        val allTasks = project.allRequestedTasks?.toSet() ?: emptySet()

        val depsCode = allTasks.joinToString("\n") { task ->
            project.taskGraph.getDeps(task).joinToString("\n") { dep ->
                "${dep.fullName} ->  ${task.fullName}"
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