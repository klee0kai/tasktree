package com.github.klee0kai.tasktree

open class TaskTreeExtension {

    /**
     * Graph max depth
     */
    var maxDepth: Int = -1

    /**
     * print task's/project additional details
     */
    var printDetails: Boolean = false

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
     * Complex price
     * ```
     * ( price ) * ( importance )
     * ```
     * Show that task expensive to run and necessary for many others
     */
    var printComplexPrice: Boolean = false


    /**
     * List sorted list of most expensive tasks or projects
     */
    var printMostExpensive: Boolean = false

}


