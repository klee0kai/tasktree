package com.github.klee0kai.tasktree.utils

import java.util.*

fun <T> Iterable<T>.removeDoubles(
    predicate: (T, T) -> Boolean
): List<T> {
    val out = LinkedList<T>()
    for (item in this) {
        val contains = out.contains { it: T -> predicate.invoke(item, it) }
        if (!contains) out.add(item)
    }
    return out
}