buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.21")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.7.21")
    }
}

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.1.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}


gradlePlugin {
    plugins.register("tasktree") {
        id = "tasktree"
        group = "com.github.klee0kai"
        version = "0.0.6"
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



