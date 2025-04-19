package com.github.klee0kai.tasktree.info

import com.github.klee0kai.tasktree.utils.fullName
import com.github.klee0kai.tasktree.utils.simpleClassName
import org.gradle.api.Project
import org.gradle.api.tasks.diagnostics.internal.ProjectDetails

object TaskStatHelper {

    fun collectAllTasksInfo(project: Project): List<TaskInfo> {
        val allTasks = project.tasks.toList()
        val tasksInfos = allTasks.map { task ->
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

        allTasks.forEach { task ->
            runCatching {
                task.taskDependencies.getDependencies(task).forEach { dependsOn ->
                    val taskStat = tasksInfos[System.identityHashCode(dependsOn)] ?: return@forEach
                    tasksInfos[System.identityHashCode(task)]?.dependencies?.add(taskStat)
                }
            }
        }
        return tasksInfos.values.toList()
    }

    fun calcToTaskStats(taskInfos: List<TaskInfo>): List<TaskStat> {
        val taskStats = taskInfos.map { taskInfo -> taskInfo.toTaskStat() }
            .associateBy { task -> task.id }

        taskStats.values.forEach { task ->
            task.allTasksCount = taskInfos.size
        }


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
        allRequestedTasksIds: Set<Int>,
    ): List<TaskStat> {
        if (allRequestedTasksIds.isEmpty()) return tasksStats
        return tasksStats.filter {
            it.id in allRequestedTasksIds
        }
    }

}