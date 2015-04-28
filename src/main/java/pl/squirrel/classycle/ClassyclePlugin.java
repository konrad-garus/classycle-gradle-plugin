package pl.squirrel.classycle;

import java.io.File;
import java.util.SortedMap;

import org.apache.tools.ant.types.FileSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.reporting.ReportingExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.logging.ConsoleRenderer;

import classycle.ant.DependencyCheckingTask;

public class ClassyclePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        Logger log = project.getLogger();
        JavaPluginConvention javaPlugin = project.getConvention().getPlugin(JavaPluginConvention.class);
        ReportingExtension reporting = project.getExtensions().getByType(ReportingExtension.class);
        SortedMap<String, SourceSet> sourceSets = javaPlugin.getSourceSets().getAsMap();

        Task classycleTask = project.task("classycle");
        Task checkTask = project.getTasks().getByName("check");
        sourceSets.forEach((name, sourceSet) -> {
            File definitionFile = project.file("src/test/resources/classycle-" + name + ".txt");
            if (!definitionFile.exists()) {
                log.debug("Classycle definition file not found: " + definitionFile + ", skipping source set "
                        + name);
            }
            File classDir = sourceSet.getOutput().getClassesDir();
            String taskName = sourceSet.getTaskName("classycle", null);
            File reportFile = reporting.file("classycle/" + name + ".txt");
            Task task = project.task(taskName);
            task.getInputs().files(classDir, definitionFile);
            task.getOutputs().file(reportFile);
            log.debug("Created classycle task: " + taskName + ", report file: " + reportFile);
            task.doLast(na -> {
                if (!classDir.isDirectory()) {
                    log.debug("Class directory doesn't exist, skipping: " + classDir);
                    return;
                }
                reportFile.getParentFile().mkdirs();
                try {
                    log.debug("Running classycle analysis on: " + classDir);
                    DependencyCheckingTask classycle = new DependencyCheckingTask();
                    classycle.setReportFile(reportFile);
                    classycle.setFailOnUnwantedDependencies(true);
                    classycle.setMergeInnerClasses(true);
                    classycle.setDefinitionFile(definitionFile);
                    classycle.setProject(project.getAnt().getAntProject());
                    FileSet fileSet = new FileSet();
                    fileSet.setDir(classDir);
                    fileSet.setProject(classycle.getProject());
                    classycle.add(fileSet);
                    classycle.execute();
                } catch (Exception e) {
                    throw new RuntimeException("Classycle check failed: " + e.getMessage()
                            + ". See report at " + new ConsoleRenderer().asClickableFileUrl(reportFile), e);
                }
            });
            classycleTask.dependsOn(task);
            checkTask.dependsOn(classycleTask);
        });
    }
}
