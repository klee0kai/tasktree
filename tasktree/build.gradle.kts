plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.publish.plugin)
}

group = libs.plugins.tasktree.get().pluginId

gradlePlugin {
    plugins.register("tasktree") {
        id = libs.plugins.tasktree.get().pluginId
        group = libs.plugins.tasktree.get().pluginId
        version = libs.versions.tasktree.get()
        implementationClass = "com.github.klee0kai.tasktree.TaskTreePlugin"
        displayName = "Task Tree"
        description = "Print gradle build dependencies graph"
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    implementation(gradleApi())
}



