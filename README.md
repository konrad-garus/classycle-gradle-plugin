# classycle-gradle-plugin
Gradle plugin for Classycle dependency analyzer

## Usage

This plugin assumes that the "java" plugin is also applied on the project. For 
each source set $name it looks for classycle definition file
src/test/resources/classycle-$name.txt. If the file exists, the plugin creates
a classycle$Name task that analyzes that source set and writes report to
$reporting/classycle/$name.txt.

### Add Plugin to Your Project

```
buildscript {
  repositories { maven { url "https://plugins.gradle.org/m2/" } }
  dependencies {
    classpath "gradle.plugin.pl.squirrel:classycle-gradle-plugin:1.2"
  }
}

apply plugin: "pl.squirrel.classycle"
```

Build script snippet for new, incubating, plugin mechanism introduced in 
Gradle 2.1:

```
plugins {
  id "pl.squirrel.classycle" version "1.2"
}
```

### Create Classycle Definition File

src/test/resources/classycle-main.txt:

```
show allResults

{package} = com.example
check absenceOfPackageCycles > 1 in ${package}.*
```

### Run the Analyzer

Concrete source set:
```
gradle classycleMain
```

All source sets:
```
gradle classycle
```

Also part of the "check" task:
```
gradle check
```

## Acknowledgments

Classycle is a Java dependency analysis library created by Franz-Josef Elmer. 
Read more about it at http://classycle.sourceforge.net/.

This plugin is inspired by the classycle.gradle script in Gradle project source:
https://github.com/gradle/gradle/blob/master/gradle/classycle.gradle.
