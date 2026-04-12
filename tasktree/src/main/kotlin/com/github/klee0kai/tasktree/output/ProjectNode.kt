package com.github.klee0kai.tasktree.output

import kotlinx.serialization.Serializable

@Serializable
data class ProjectNode(
    val id: Int,
    val name: String,
)
