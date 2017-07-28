package pl.squirrel.classycle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.reporting.ReportingExtension;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.util.SortedMap;

public class ClassyclePlugin implements Plugin<Project> {

    public static final String TASK_GROUP_NAME = "Verification";

    private ClassyclePluginConfiguration configuration;

    @Override
    public void apply(final Project project) {
        loadConfiguration(project);

        final JavaPluginConvention javaPlugin = project.getConvention().getPlugin(JavaPluginConvention.class);
        final ReportingExtension reporting = project.getExtensions().getByType(ReportingExtension.class);
        final SortedMap<String, SourceSet> sourceSets = javaPlugin.getSourceSets().getAsMap();

        File projectDir = project.getProjectDir();
        org.apache.tools.ant.Project antProject = project.getAnt().getAntProject();

        final Task classycleTask = project.task("classycle");
        classycleTask.setGroup(TASK_GROUP_NAME);
        classycleTask.setDescription("Run all classycle tasks");
        final Task checkTask = project.getTasks().getByName("check");
        for (String name : sourceSets.keySet()) {
            final SourceSet sourceSet = sourceSets.get(name);
            File classDir = sourceSet.getOutput().getClassesDir();
            final String taskName = sourceSet.getTaskName("classycle", null);
            final Task task = project.task(taskName);
            task.setGroup(TASK_GROUP_NAME);
            task.setDescription("Classycle check task for " + name + " source set.");

            ClassycleCheckAction action = new ClassycleCheckAction();
            action.setConfiguration(configuration);
            action.setClassDir(classDir);
            action.setReportFile(generateReportFileName(reporting, name));
            action.setAntProject(antProject);
            action.setProjectDir(projectDir);
            task.doLast(action);
            classycleTask.dependsOn(task);
        }
        checkTask.dependsOn(classycleTask);
    }

    private File generateReportFileName(ReportingExtension reporting, String name) {
        return reporting.file("classycle/" + name + ".txt");
    }

    private void loadConfiguration(Project project) {
        configuration = project.getRootProject().getExtensions().create("classycle", ClassyclePluginConfiguration.class);
        // set defaults
        configuration.setFailOnUnwantedDependencies(true);
        configuration.setMergeInnerClasses(true);
    }

}
