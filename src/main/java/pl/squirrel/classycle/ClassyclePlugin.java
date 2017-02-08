package pl.squirrel.classycle;

import classycle.ant.DependencyCheckingTask;
import org.apache.tools.ant.types.FileSet;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.reporting.ReportingExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.internal.UncheckedException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.SortedMap;

public class ClassyclePlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        final Logger log = project.getLogger();
        final JavaPluginConvention javaPlugin = project.getConvention().getPlugin(JavaPluginConvention.class);
        final ReportingExtension reporting = project.getExtensions().getByType(ReportingExtension.class);
        final SortedMap<String, SourceSet> sourceSets = javaPlugin.getSourceSets().getAsMap();

        final Task classycleTask = project.task("classycle");
        final Task checkTask = project.getTasks().getByName("check");
        for (String name : sourceSets.keySet()) {
            final SourceSet sourceSet = sourceSets.get(name);
            final String taskName = sourceSet.getTaskName("classycle", null);
            final SourceSetClassycleTask task = project.getTasks().create(taskName, SourceSetClassycleTask.class);

            final File definitionFile;
            if (task.getDefinitionFilePath() != null) {
                definitionFile = project.file(task.getDefinitionFilePath());
                if (!definitionFile.exists()) {
                    throw new RuntimeException("Definition file not found: " + definitionFile);
                }
            } else {
                definitionFile = project.file("src/test/resources/classycle-" + name + ".txt");
                if (!definitionFile.exists()) {
                    log.debug("Default classycle definition file not found: " + definitionFile + ", skipping source set "
                                  + name);
                    continue;
                }
            }

            final File classDir = sourceSet.getOutput().getClassesDir();
            final File reportFile = reporting.file("classycle/" + name + ".txt");
            task.getInputs().files(classDir, definitionFile);
            task.getOutputs().file(reportFile);
            log.debug("Created classycle task: " + taskName + ", report file: " + reportFile);
            task.doLast(new Action<Task>() {
                @Override
                public void execute(Task task) {
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
                        fileSet.setIncludes("**/*.class");
                        fileSet.setDir(classDir);
                        fileSet.setProject(classycle.getProject());
                        classycle.add(fileSet);
                        classycle.execute();
                    } catch (Exception e) {
                        throw new RuntimeException(
                            "Classycle check failed: " + e.getMessage() + ". See report at "
                                + clickableFileUrl(reportFile), e);
                    }
                }
            });
            classycleTask.dependsOn(task);
            checkTask.dependsOn(classycleTask);
        }
    }

    private String clickableFileUrl(File path) {
        try {
            return (new URI("file", "", path.toURI().getPath(), (String) null, (String) null)).toString();
        } catch (URISyntaxException var3) {
            throw UncheckedException.throwAsUncheckedException(var3);
        }
    }
}
