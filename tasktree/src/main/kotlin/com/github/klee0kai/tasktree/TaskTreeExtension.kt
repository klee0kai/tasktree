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
     * Print the number of dependencies for a task
     */
    var printPrice: Boolean = false

    /**
     * Print number of dependent tasks
     */
    var printImportance: Boolean = false

    /**
     * Print the number of dependent tasks from another project
     */
    var printImportanceOutSide: Boolean = false

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
    var printMostExpensiveTasks: Boolean = false

    /**
     * List sorted list of most expensive modules
     */
    var printMostExpensiveModules: Boolean = false

}


