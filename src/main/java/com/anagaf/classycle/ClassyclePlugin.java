package com.anagaf.classycle;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.AndroidSourceSet;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import java.io.File;

/**
 * Gradle plugin that creates classycle tasks for project source sets. Task names are built as
 * "classycle" + source set name (e.g. "classycleRelease", "classycleMain" etc).
 *
 * Resulting task graph is:
 * -- check
 * ---- classycle
 * ------ classycleRelease
 * ------ classycleDebug
 * ...
 *
 * Specify classycle definition file path to enable check for the particular source set (e.g.
 * classycleRelease.definitionFilePath = "config/classycle.txt")
 *
 * If definition file is not specified the check is skipped for the particular source set.
 */
public class ClassyclePlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        final Logger logger = project.getLogger();

        final Task classycleTask = project.task("classycle");

        final JavaPluginConvention javaPlugin = project.getConvention().getPlugin(JavaPluginConvention.class);
        for (SourceSet sourceSet : javaPlugin.getSourceSets()) {
            logger.debug("Creating classycle tasks for Java source sets");

            createSourceSetClassycleTask(logger,
                                         classycleTask,
                                         sourceSet.getName(),
                                         sourceSet.getOutput().getClassesDir(),
                                         sourceSet.getClassesTaskName());
        }

        final AppExtension androidExtension = project.getExtensions().findByType(AppExtension.class);
        if (androidExtension != null) {
            logger.debug("Creating classycle tasks for Android source sets");

            for (AndroidSourceSet sourceSet : androidExtension.getSourceSets()) {
                final File classesDir = new File(project.getBuildDir(),
                                                 "intermediates/classes/" + sourceSet.getName());
                createSourceSetClassycleTask(logger,
                                             classycleTask,
                                             sourceSet.getName(),
                                             classesDir,
                                             "assemble");
            }
        }

        project.getTasks().getByName("check").dependsOn(classycleTask);
    }

    /**
     * Creates source set classycle task. Adds dependency of the created task to general "classycle"
     * task.
     *
     * @param logger          logger
     * @param classycleTask   general classycle task
     * @param sourceSetName   source set name
     * @param classesDir      source set classes directory
     * @param classesTaskName task that builds class-files
     */
    private void createSourceSetClassycleTask(final Logger logger,
                                              final Task classycleTask,
                                              final String sourceSetName,
                                              final File classesDir,
                                              final String classesTaskName) {
        final Project project = classycleTask.getProject();
        final String taskName = "classycle" + capitalizeFirstLetter(sourceSetName);
        final SourceSetClassycleTask task = project.getTasks().create(taskName, SourceSetClassycleTask.class);
        task.setSourceSetName(sourceSetName);
        task.setClassesDir(classesDir);
        task.dependsOn(project.getTasks().getByName(classesTaskName));
        classycleTask.dependsOn(task);

        logger.debug("Created task " + taskName
                             + " for source set " + sourceSetName
                             + " (classes dir " + classesDir.getAbsolutePath()
                             + ", classes task name " + classesTaskName
                             + ")");
    }

    /**
     * Capitalizes string first letter.
     *
     * @param original original string
     * @return string with the first letter capitalized
     */
    private static String capitalizeFirstLetter(String original) {
        if (original == null || original.isEmpty()) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
}
