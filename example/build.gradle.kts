plugins {
    `kotlin-dsl`
    alias(libs.plugins.tasktree)
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
    printDetails = false
    printPrice = true
    printImportance = true
    printRelativePrice = true
    printDoubles = false
    printMostExpensive = true
}

dependencies {
    implementation(project(":example_core"))
}

