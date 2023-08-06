package com.github.klee0kai.tasktree

open class TaskTreeExtension {

    /**
     * Print task inputs
     */
    var inputs: Boolean = false

    /**
     * Print task outputs
     */
    var outputs: Boolean = false

    /**
     * Graph max depth
     */
    var maxDepth: Int = -1

    /**
     * print task's class type
     */
    var printClassName: Boolean = false

    /**
     * Don't show doubles in graph
     */
    var printDoubles: Boolean = false

    /**
     * Print dependency count for task
     */
    var printPrice: Boolean = false

    /**
     * Print depended task count
     */
    var printImportance: Boolean = false

    /**
     * Complex price
     * ```
     * ( price ) * ( importance )
     * ```
     * Show that task expensive to run and necessary for many others
     */
    var printComplexPrice: Boolean = false


    /**
     * List sorted list of most expensive tasks
     */
    var printMostExpensive: Boolean = false

}


