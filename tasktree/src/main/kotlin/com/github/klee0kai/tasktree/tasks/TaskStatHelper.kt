package com.github.klee0kai.tasktree.tasks

import com.github.klee0kai.tasktree.TaskStat
import com.github.klee0kai.tasktree.utils.allRequestedTasks
import com.github.klee0kai.tasktree.utils.getDeps
import com.github.klee0kai.tasktree.utils.taskGraph
import org.gradle.api.Project
import org.gradle.api.Task

class TaskStatHelper {

    val taskStat = mutableMapOf<Task, TaskStat>()

    fun collectFrom(project: Project) {
        taskStat.clear()

        val allTasksSet = project.allRequestedTasks.toSet()
        allTasksSet.forEach { task ->
            taskStat.putIfAbsent(
                task,
                TaskStat(
                    task = task,
                    allTasks = allTasksSet,
                    rootProject = project
                )
            )
        }

        //https://docs.gradle.org/current/javadoc/org/gradle/api/execution/TaskExecutionGraph.html#getAllTasks--
        // use sorted list
        allTasksSet.forEach { task ->
            val stat = taskStat[task] ?: return@forEach
            val deps = project.taskGraph.getDeps(task)
            stat.allDepsCount = deps.count() + deps.sumOf { dep ->
                taskStat[dep]?.allDepsCount ?: 0
            }
        }

        allTasksSet.reversed().forEach { task ->
            val stat = taskStat[task] ?: return@forEach
            project.taskGraph.getDeps(task).forEach {
                val depStat = taskStat[it] ?: return@forEach
                depStat.allDependedOnCount += 1 + stat.allDependedOnCount
                if (depStat.task.project != stat.task.project) {
                    depStat.allDependedOnOutsideProjectCount += 1 + stat.allDependedOnCount
                }
            }
        }
    }

}