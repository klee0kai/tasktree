package com.github.klee0kai.tasktree.utils

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

fun String.pathPlus(path: String?): String {
    return when {
        endsWith(File.separator) && path?.startsWith(File.separator) == true -> {
            plus(path.substring(1))
        }

        endsWith(File.separator) -> {
            plus(path)
        }

        else -> {
            plus(File.separator).plus(path)
        }
    }
}

fun File.walkStarMasked(): Sequence<File> = Paths.get(absolutePath).walkStarMasked()

fun Path.walkStarMasked(): Sequence<File> =
    sequence<File> {
        val path = this@walkStarMasked
        val names = path.toPathNames()
        val starNameIndex = names.indexOfFirst {
            it == "*"
        }

        when {
            starNameIndex > 0 -> {
                val file = names.subList(0, starNameIndex).joinToPath().toFile()
                file.list()?.forEach {
                    val newMask = (names.subList(0, starNameIndex)
                            + listOf(it)
                            + names.subList(starNameIndex + 1, names.size)
                            ).joinToPath()
                    yieldAll(newMask.walkStarMasked())
                }
            }

            path.toFile().exists() -> {
                yield(path.toFile())
            }
        }
    }


fun List<String>.joinToPath(): Path {
    return Path.of(
        first(),
        *subList(1, size).toTypedArray()
    )
}

fun Path.toPathNames(): List<String> =
    when {
        startsWith(File.separator) -> listOf(File.separator) + toList().map { it.fileName.toString() }
        else -> toList().map { it.fileName.toString() }
    }
