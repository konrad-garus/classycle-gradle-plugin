# classycle-gradle-plugin
Gradle plugin for Classycle dependency analyzer

## Usage

This is a gradle plugin that creates Classycle tasks for all project source sets (including
Android source sets). Task names are constructed as "classycle" + source set name 
(e.g. "classycleRelease", "classycleMain" etc).

As the result of this plugin application "check" task depends on the general "classycle" task. General
"classycle" task depends on source set Classycle tasks i.e. the resulting task graph looks like:
```
check
    classycle
        classycleRelease
        classycleDebug
        ...
```
### Add Plugin to Your Project

```
buildscript {
  repositories { maven { url "https://plugins.gradle.org/m2/" } }
  dependencies {
    classpath "gradle.plugin.pl.squirrel:classycle-gradle-plugin:2.0"
  }
}

apply plugin: "pl.squirrel.classycle"
```

Build script snippet for new, incubating, plugin mechanism introduced in 
Gradle 2.1:

```
plugins {
  id "pl.squirrel.classycle" version "2.0"
}
```
### Create Classycle Definition File

config/classycle-main.txt:
```
show allResults

{package} = com.example
check absenceOfPackageCycles > 1 in ${package}.*
```
Specify Classycle definition file path for the source sets you would like to be checked. This could be 
done by setting "definitionFilePath" property of the corresponding Classycle task:
```
classycleMain.definitionFilePath = "config/classycle-main.txt"
```
Note that source set Classycle tasks without definition file are ignored.
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
