plugins {
    id("java")
    id("tasktree")
}

val firstTask = tasks.create("simple_first_task") {
    doLast { println("run simple_first_task") }

    tasks.getByName("assemble").dependsOn(this)
}


tasks.create("sub_first_task") {
    doLast { println("run sub_first_task") }

    firstTask.dependsOn(this)
}


val secondTask = tasks.create("simple_second_task") {
    doLast { println("run simple_second_task") }

    tasks.getByName("assemble").dependsOn(this)
}


tasks.create("sub_second_task") {
    doLast { println("run sub_second_task") }

    secondTask.dependsOn(this)
}

tasktree {

    inputs = false
    outputs = false
    printClassName = false
    printPrice = true
    printImportance = true
    printComplexPrice = true
    printDoubles = true
    printImportanceOutSide = true


}


