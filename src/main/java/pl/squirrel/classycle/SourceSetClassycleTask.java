package pl.squirrel.classycle;

import org.apache.tools.ant.types.FileSet;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.reporting.ReportingExtension;

import java.io.File;

import classycle.ant.DependencyCheckingTask;

/**
 * Source set classycle check task.
 */
class SourceSetClassycleTask extends DefaultTask {
    /** Reporting extension. */
    private final ReportingExtension reporting;

    /** Definition file path. */
    private String definitionFilePath;

    /** Directory where class-files are stored. */
    private File classesDir;

    /** Report file. */
    private File reportFile;

    /** Dependency checking task factory. */
    private DependencyCheckingTaskFactory dependencyCheckingTaskFactory;

    /** Default definition file (required for backward compatibility). */
    private File defaultDefinitionFile;

    public SourceSetClassycleTask() {
        final Logger log = getProject().getLogger();

        reporting = getProject().getExtensions().getByType(ReportingExtension.class);

        doLast(task ->
               {
                   final File definitionFile;
                   if (getDefinitionFilePath() == null) {
                       if (!defaultDefinitionFile.exists()) {
                           log.info("Classycle definition file for task " + task.getName() + " is not specified");
                           return;
                       }
                       definitionFile = defaultDefinitionFile;
                   }
                   else {
                       definitionFile = getProject().file(getDefinitionFilePath());
                       if (!definitionFile.exists()) {
                           throw new RuntimeException("Classycle definition file "
                                                              + definitionFile.getAbsolutePath()
                                                              + " does not exist");
                       }
                   }

                   if (!classesDir.exists() || !classesDir.isDirectory()) {
                       throw new RuntimeException("Invalid classycle directory " + classesDir.getAbsolutePath());
                   }

                   final File parentFile = reportFile.getParentFile();
                   parentFile.mkdirs();
                   try {
                       log.debug("Running classycle analysis on: " + classesDir);

                       final DependencyCheckingTask depCheckTask = dependencyCheckingTaskFactory.createTask();
                       depCheckTask.setReportFile(reportFile);
                       depCheckTask.setFailOnUnwantedDependencies(true);
                       depCheckTask.setMergeInnerClasses(true);
                       depCheckTask.setDefinitionFile(definitionFile);
                       depCheckTask.setProject(getProject().getAnt().getAntProject());
                       final FileSet fileSet = new FileSet();
                       fileSet.setIncludes("**/*.class");
                       fileSet.setDir(classesDir);
                       fileSet.setProject(depCheckTask.getProject());
                       depCheckTask.add(fileSet);
                       depCheckTask.execute();
                   }
                   catch (Exception e) {
                       throw new RuntimeException("Classycle check failed: " + e.getMessage()
                                                          + ". See report at " + reportFile.getAbsolutePath());

                   }
               });
    }

    void setSourceSetName(final String sourceSetName) {
        reportFile = reporting.file("classycle/" + sourceSetName + ".txt");
        getOutputs().file(reportFile);
        defaultDefinitionFile = getProject().file("src/test/resources/classycle-" + sourceSetName + ".txt");
    }

    void setClassesDir(final File classesDir) {
        this.classesDir = classesDir;
        getInputs().files(classesDir);
    }

    private String getDefinitionFilePath() {
        return definitionFilePath;
    }

    public void setDefinitionFilePath(final String definitionFilePath) {
        this.definitionFilePath = definitionFilePath;
    }

    void setDependencyCheckingTaskFactory(final DependencyCheckingTaskFactory factory) {
        this.dependencyCheckingTaskFactory = factory;
    }
}
