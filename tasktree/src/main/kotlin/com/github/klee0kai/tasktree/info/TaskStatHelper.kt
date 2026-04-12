package com.github.klee0kai.tasktree.info

import com.github.klee0kai.tasktree.utils.fullName
import com.github.klee0kai.tasktree.utils.simpleClassName
import org.gradle.api.Project

object TaskStatHelper {

    fun collectAllTasksInfo(project: Project): List<TaskInfo> {
        val allTasks = project.tasks.toList()
        val tasksInfos = allTasks.map { task ->

            TaskInfo(
                path = task.path,
                taskName = task.name,
                group = task.group ?: "",
                description = task.description ?: "",
                termDir = task.temporaryDir.path,
                className = task::class.java,
                simpleClassName = task.simpleClassName,
                projectName = project.fullName,
            )
        }.associateBy { task -> task.path }

        allTasks.forEach { task ->
            runCatching {
                task.taskDependencies.getDependencies(task).forEach { dependsOn ->
                    val taskStat = tasksInfos[dependsOn.path] ?: return@forEach
                    tasksInfos[dependsOn.path]?.dependencies?.add(taskStat)
                }
            }
        }
        return tasksInfos.values.toList()
    }

    fun calcToTaskStats(taskInfos: List<TaskInfo>): List<TaskStat> {
        val taskStats = taskInfos.map { taskInfo -> taskInfo.toTaskStat() }
            .associateBy { task -> task.path }

        taskStats.values.forEach { task ->
            task.allTasksCount = taskInfos.size
        }


        taskInfos.forEach { taskInfo ->
            taskInfo.dependencies.forEach { dependsOn ->
                taskStats[taskInfo.path]?.dependencies?.add(taskStats[dependsOn.path] ?: return@forEach)
            }
        }

        taskStats.values.forEach { taskStat ->
            taskStat.dependencies.forEach { dependsOn ->
                dependsOn.dependedOnTasks.add(taskStat)
            }
        }

        val maxPrice = taskStats.values.maxOfOrNull { it.price } ?: 0
        val maxDepth = taskStats.values.maxOfOrNull { it.depth } ?: 0
        taskStats.values.forEach { task ->
            task.maxPrice = maxPrice
            task.maxDepth = maxDepth
        }

        return taskStats.values.toList()
    }

    fun filterByRequestedTasks(
        tasksStats: List<TaskStat>,
        allRequestedTasksIds: Set<String>,
    ): List<TaskStat> {
        if (allRequestedTasksIds.isEmpty()) return tasksStats
        return tasksStats.filter {
            it.path in allRequestedTasksIds
        }
    }

}