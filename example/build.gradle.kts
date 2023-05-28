plugins {
    id("java")
    id("tasktree")
}

tasks.create("simple_first_task") {
    doLast { println("run simple_first_task") }

    tasks.getByName("assemble").dependsOn(this)
}


tasktree {

    inputs = false
    outputs = false
    printClassName = true
    maxDepth = 1

}


