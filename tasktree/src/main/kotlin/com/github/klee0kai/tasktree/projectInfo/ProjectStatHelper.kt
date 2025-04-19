package com.github.klee0kai.tasktree.projectInfo

import com.github.klee0kai.tasktree.utils.fullName
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.diagnostics.internal.ProjectDetails

object ProjectStatHelper {

    fun collectProjectDependencies(rootProject: Project): List<ProjectInfo> {
        val projectInfos = rootProject.allprojects.map {
            ProjectInfo(
                name = it.name,
                fullName = it.fullName,
                path = it.path,
                projectDetails = ProjectDetails.of(it),
            )
        }.associateBy { it.path }

        rootProject.allprojects.forEach { project ->
            val projectInfo = projectInfos[project.path] ?: return@forEach
            project.configurations
                .firstOrNull { it.name.contains("implementation") }
                ?.allDependencies
                ?.filterIsInstance<ProjectDependency>()
                ?.forEach { dep ->
                    val depProjectInfo = runCatching { projectInfos[dep.path] }.getOrNull()
                        ?: runCatching { projectInfos[dep.dependencyProject.path] }.getOrNull()
                        ?: return@forEach
                    projectInfo.dependencies.add(depProjectInfo)
                }
        }

        return projectInfos.values.toList()
    }

    fun calcToProjectStats(projectInfos: List<ProjectInfo>): List<ProjectInfo> {
        val projectInfos = projectInfos.toMutableList()

        projectInfos.forEach { it.allProjectsCount = projectInfos.size }

        projectInfos.forEach { taskStat ->
            taskStat.dependencies.forEach { dependsOn ->
                dependsOn.dependedOnProjects.add(taskStat)
            }
        }

        val maxPrice = projectInfos.maxOfOrNull { it.price } ?: 0
        val maxDepth = projectInfos.maxOfOrNull { it.depth } ?: 0
        projectInfos.forEach { project ->
            project.maxPrice = maxPrice
            project.maxDepth = maxDepth
        }

        return projectInfos
    }


    fun filterByRequestedProject(
        projectStats: List<ProjectInfo>,
        target: String?,
    ): List<ProjectInfo> {
        if (target.isNullOrBlank()) return projectStats
        return projectStats.filter {
            it.fullName == target || it.allDependedOnProject.any { it.fullName == target }
        }
    }

}