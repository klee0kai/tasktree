## TaskTree

[![](https://img.shields.io/badge/license-GNU_GPLv3-blue.svg?style=flat-square)](./LICENSE)
[![](https://jitpack.io/v/klee0kai/tasktree.svg)](https://jitpack.io/#klee0kai/tasktree)

Print gradle task dependencies graph

## Usage

Apply plugin in your module's `build.gradle`:

```kotlin
plugins {
    id("com.github.klee0kai.tasktree") version "0.0.10"
}

tasktree {
    printClassName = true
    maxDepth = 1
}
```

Report your build graph

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

>> Heavy projects: ':example' depth: 2
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
        classpath("com.github.klee0kai:tasktree:0.0.10")
    }
}

rootProject{
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

