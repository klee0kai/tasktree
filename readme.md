## TaskTree

[![](https://img.shields.io/badge/license-GNU_GPLv3-blue.svg?style=flat-square)](./LICENSE)
[![](https://jitpack.io/v/klee0kai/tasktree.svg)](https://jitpack.io/#klee0kai/tasktree)

Print gradle build dependencies graph

## Usage

Configure classpath in project's `build.gradle`:

```kotlin
buildscript {
    repositories {
        maven(url = "https://jitpack.io")
    }
    dependencies {
        classpath("com.github.klee0kai:tasktree:0.0.3")
    }
}
```

Apply plugin in your module's `build.gradle`:

```kotlin
plugins {
    id("tasktree")
}

tasktree {
    inputs = false
    outputs = false
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
        classpath("com.github.klee0kai:tasktree:0.0.3")
    }
}

rootProject{
    pluginManager.apply(com.github.klee0kai.tasktree.TaskTreePlugin::class.java)
}
```

## License

```
Copyright (c) 2023 Andrey Kuzubov
```

