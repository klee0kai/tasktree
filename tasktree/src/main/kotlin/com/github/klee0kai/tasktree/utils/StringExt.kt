package com.github.klee0kai.tasktree.utils

fun String.insertTo(index: Int, txt: String): String {
    val start = substring(0, index)
    val end = substring(index, length)

    return "${start}${txt}${end}"
}

fun String.indexesSequence(txt: String): Sequence<Int> = sequence {
    for (i in 0 until length - txt.length) {
        if (substring(i, i + txt.length) == txt)
            yield(i)
    }
}

