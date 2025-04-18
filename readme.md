## TaskTree

[![](https://img.shields.io/badge/license-GNU_GPLv3-blue.svg?style=flat-square)](./LICENSE)
[![](https://jitpack.io/v/klee0kai/tasktree.svg)](https://jitpack.io/#klee0kai/tasktree)

Print gradle task dependencies graph

## Usage

Apply plugin in your module's `build.gradle`:

```kotlin
plugins {
    id("com.github.klee0kai.tasktree") version "0.0.9"
}

tasktree {
    printClassName = true
    maxDepth = 1
}
```

Report your build graph

```bash
./gradlew tasktree assemble
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
        classpath("com.github.klee0kai.tasktree:com.github.klee0kai.tasktree.gradle.plugin:0.0.9")
    }
}

rootProject{
    pluginManager.apply(com.github.klee0kai.tasktree.TaskTreePlugin::class.java)

    extensions.findByType(com.github.klee0kai.tasktree.TaskTreeExtension::class.java)
        ?.apply {
            maxDepth = 1
            printClassName = true
            printComplexPrice = true
        }
}
```

## License

```
Copyright (c) 2023 Andrey Kuzubov
```

