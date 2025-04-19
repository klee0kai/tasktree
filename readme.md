## TaskTree

[![](https://img.shields.io/badge/license-GNU_GPLv3-blue.svg?style=flat-square)](./LICENSE)
[![](https://jitpack.io/v/klee0kai/tasktree.svg)](https://jitpack.io/#klee0kai/tasktree)

Print gradle task dependencies graph

## Usage

Apply plugin in your module's `build.gradle`:

```kotlin
plugins {
    id("com.github.klee0kai.tasktree") version "0.0.11"
}

tasktree {
    printClassName = true
    maxDepth = 1
}
```

Project build report in the form of a build graph. [Diagon](https://github.com/ArthurSonzogni/Diagon) must be installed

```bash
./gradlew taskTree assemble

>>
:example:assemble price: 12; depth: 6; importance: 3; relativePrice: 0,55; relativeDepth: 0,67;
+--- :example:simple_first_task price: 2; depth: 2; importance: 4; relativePrice: 0,09; relativeDepth: 0,22;
|    \--- :example:sub_first_task price: 1; depth: 1; importance: 5; relativePrice: 0,05; relativeDepth: 0,11;

```

Verify project's module dependency depth

```bash 
./gradlew projectTree  --verifyDepth=1

>> :dynamic_findstorage price: 3; depth: 3; importance: 0; relativePrice: 1,00; relativeDepth: 1,00; depth dependencies: :dynamic_findstorage <- :app_mobile <- :core;
    Heavy projects: ':dynamic_findstorage' depth: 3
```

Build graphs

```bash 
./gradlew projectGraph

>>
┌─────────────┐
│:example_core│
└┬────────────┘
┌▽───────┐     
│:example│     
└────────┘     
```

## Configure Init Script

Configure your init script `$HOME/.gradle/init.gradle.kts`
[HowIt'sWork](https://docs.gradle.org/current/userguide/init_scripts.html).

```kotlin
initscript {
    repositories {
        maven(url = "https://jitpack.io")
    }
    dependencies {
        classpath("com.github.klee0kai:tasktree:0.0.11")
    }
}

rootProject {
    pluginManager.apply(com.github.klee0kai.tasktree.TaskTreePlugin::class.java)

    extensions.findByType(com.github.klee0kai.tasktree.TaskTreeExtension::class.java)
        ?.apply {
            maxDepth = 1
            printDetails = true
            printRelativePrice = true
        }
}
```

## License

```
Copyright (c) 2023 Andrey Kuzubov
```

