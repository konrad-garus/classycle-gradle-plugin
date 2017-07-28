package pl.squirrel.classycle;

import classycle.ant.DependencyCheckingTask;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.internal.UncheckedException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class ClassycleCheckAction implements Action<Task> {

    private static final Logger LOG = Logging.getLogger(ClassycleCheckAction.class);

    @InputFiles
    private File classDir;

    @Input
    private ClassyclePluginConfiguration configuration;

    @OutputFile
    private File reportFile;

    private Project antProject;
    private File projectDir;

    @Override
    public void execute(Task task) {
        if (!classDir.isDirectory()) {
            LOG.debug("Class directory doesn't exist, skipping: " + classDir);
            return;
        }
        reportFile.getParentFile().mkdirs();
        try {
            LOG.debug("Running classycle analysis on: " + classDir);
            DependencyCheckingTask classycle = new DependencyCheckingTask();
            classycle.setReportFile(reportFile);
            classycle.setFailOnUnwantedDependencies(configuration.getFailOnUnwantedDependencies());
            classycle.setMergeInnerClasses(true);
            classycle.setDefinitionFile(new File(projectDir, configuration.getDefinitionFile()));
            classycle.setProject(antProject);
            if(isNotBlank(configuration.getReflectionPattern())) {
                classycle.setReflectionPattern(configuration.getReflectionPattern());
            }
            if(isNotBlank(configuration.getResultRenderer())) {
                classycle.setResultRenderer(configuration.getResultRenderer());
            }
            if(isNotBlank(configuration.getIncludingClasses())) {
                classycle.setIncludingClasses(configuration.getIncludingClasses());
            }
            if(isNotBlank(configuration.getExcludingClasses())) {
                classycle.setExcludingClasses(configuration.getExcludingClasses());
            }
            FileSet fileSet = new FileSet();
            fileSet.setDir(classDir);
            fileSet.setProject(classycle.getProject());
            classycle.add(fileSet);
            classycle.execute();
        } catch (Exception e) {
            throw new GradleException(
                    "Classycle check failed: " + e.getMessage() + ". See report at "
                            + clickableFileUrl(reportFile), e);
        }
    }

    private boolean isNotBlank(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private String clickableFileUrl(File path) {
        try {
            return (new URI("file", "", path.toURI().getPath(), (String) null, (String) null)).toString();
        } catch (URISyntaxException var3) {
            throw UncheckedException.throwAsUncheckedException(var3);
        }
    }

    public File getClassDir() {
        return classDir;
    }

    public void setClassDir(File classDir) {
        this.classDir = classDir;
    }

    public ClassyclePluginConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ClassyclePluginConfiguration configuration) {
        this.configuration = configuration;
    }

    public File getReportFile() {
        return reportFile;
    }

    public void setReportFile(File reportFile) {
        this.reportFile = reportFile;
    }

    public void setAntProject(Project antProject) {
        this.antProject = antProject;
    }

    public Project getAntProject() {
        return antProject;
    }

    public void setProjectDir(File projectDir) {
        this.projectDir = projectDir;
    }

    public File getProjectDir() {
        return projectDir;
    }
}
