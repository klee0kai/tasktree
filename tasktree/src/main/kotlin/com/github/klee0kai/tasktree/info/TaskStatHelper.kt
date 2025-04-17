package com.github.klee0kai.tasktree.info

import com.github.klee0kai.tasktree.utils.fullName
import com.github.klee0kai.tasktree.utils.simpleClassName
import org.gradle.api.Project
import org.gradle.api.tasks.diagnostics.internal.ProjectDetails

object TaskStatHelper {

    fun collectAllTasksInfo(project: Project): List<TaskInfo> {
        val tasksInfos = project.tasks.map { task ->
            TaskInfo(
                id = System.identityHashCode(task),
                taskName = task.name,
                className = task::class.java,
                simpleClassName = task.simpleClassName,
                projectName = project.fullName,
                projectDetails = ProjectDetails.of(project),
                rootProjectDetails = ProjectDetails.of(project.rootProject),
            )
        }.associateBy { task -> task.id }

        project.tasks.forEach { task ->
            task.taskDependencies.getDependencies(task).forEach { dependsOn ->
                val taskStat = tasksInfos[System.identityHashCode(dependsOn)] ?: return@forEach
                tasksInfos[System.identityHashCode(task)]?.dependencies?.add(taskStat)
            }
        }
        return tasksInfos.values.toList()
    }


    fun calcToTaskStats(taskInfos: List<TaskInfo>): List<TaskStat> {
        val taskStats = taskInfos.map { taskInfo -> taskInfo.toTaskStat() }
            .associateBy { task -> task.id }

        taskInfos.forEach { taskInfo ->
            taskInfo.dependencies.forEach { dependsOn ->
                taskStats[taskInfo.id]?.dependencies?.add(taskStats[dependsOn.id] ?: return@forEach)
            }
        }

        taskStats.values.forEach { taskStat ->
            taskStat.dependencies.forEach { dependsOn ->
                dependsOn.dependedOnTasks.add(taskStat)
            }
        }

        return taskStats.values.toList()
    }
}