package pl.squirrel.classycle;

import org.apache.tools.ant.types.FileSet;
import org.gradle.api.Project;
import org.gradle.api.reporting.ReportingExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;

import classycle.ant.DependencyCheckingTask;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SourceSetClassycleTaskTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private SourceSetClassycleTask task;

    private Project project;

    private File reportFile;

    @Before
    public void setUp() throws Exception {
        temporaryFolder.create();

        project = new ProjectBuilder().build();
        final ReportingExtension reporting = mock(ReportingExtension.class);
        project.getExtensions().add("reporting", reporting);

        reportFile = temporaryFolder.newFile();
        when(reporting.file(anyString())).thenReturn(reportFile);

        task = project.getTasks().create("classycleSourceSet", SourceSetClassycleTask.class);
    }

    @Test
    public void shouldDoNothingIfDefinitionFileNotSpecified() {
        task.setSourceSetName("ss");
        task.getActions().get(0).execute(task);
    }

    @Test
    public void shouldFailIfDefinitionFileNotFound() {
        task.setSourceSetName("ss");
        task.setDefinitionFilePath("aaa");
        expectedException.expect(RuntimeException.class);
        task.getActions().get(0).execute(task);
    }

    @Test
    public void shouldFailIfClassesDirNotFound() throws IOException {
        task.setSourceSetName("ss");
        task.setDefinitionFilePath(temporaryFolder.newFile().getAbsolutePath());
        task.setClassesDir(new File("zzz"));
        expectedException.expect(RuntimeException.class);
        task.getActions().get(0).execute(task);
    }

    @Test
    public void shouldFailIfClassesDirInvalid() throws IOException {
        task.setSourceSetName("ss");
        task.setDefinitionFilePath(temporaryFolder.newFile().getAbsolutePath());
        task.setClassesDir(temporaryFolder.newFile());
        expectedException.expect(RuntimeException.class);
        task.getActions().get(0).execute(task);
    }

    @Test
    public void shouldExecuteDependencyCheckingTask() throws Exception {
        final File definitionFile = temporaryFolder.newFile();
        task.setDefinitionFilePath(definitionFile.getAbsolutePath());

        final File classesDir = temporaryFolder.newFolder();
        assertTrue(new File(classesDir, "class1.class").createNewFile());
        assertTrue(new File(classesDir, "class2.class").createNewFile());
        assertTrue(new File(classesDir, "xxx.yyy").createNewFile());
        task.setClassesDir(classesDir);

        task.setSourceSetName("ss");

        final org.apache.tools.ant.Project antProject = project.getAnt().getAntProject();

        final DependencyCheckingTask dependencyCheckingTask = mock(DependencyCheckingTask.class);
        when(dependencyCheckingTask.getProject()).thenReturn(antProject);
        task.setDependencyCheckingTaskFactory(() -> dependencyCheckingTask);

        task.getActions().get(0).execute(task);

        verify(dependencyCheckingTask).setReportFile(reportFile);
        verify(dependencyCheckingTask).setFailOnUnwantedDependencies(true);
        verify(dependencyCheckingTask).setMergeInnerClasses(true);
        verify(dependencyCheckingTask).setDefinitionFile(definitionFile);
        verify(dependencyCheckingTask).setProject(antProject);

        final ArgumentCaptor<FileSet> fileSet = ArgumentCaptor.forClass(FileSet.class);
        verify(dependencyCheckingTask).add(fileSet.capture());
        assertTrue(fileSet.getValue().getDir() == classesDir);
        assertTrue(fileSet.getValue().getProject() == antProject);
        assertEquals(2, fileSet.getValue().getDirectoryScanner().getIncludedFilesCount());

        verify(dependencyCheckingTask).execute();
    }

    @Test
    public void shouldSupportDefaultDefinitionFile() throws IOException {
        final String sourceSetName = "ss";
        task.setSourceSetName(sourceSetName);

        final String defaultDefinitionFilePath = String.format("src/test/resources/classycle-%s.txt", sourceSetName);
        final File defaultDefinitionFile = new File(project.getProjectDir(), defaultDefinitionFilePath);
        defaultDefinitionFile.getParentFile().mkdirs();
        assertTrue(defaultDefinitionFile.createNewFile());

        final File classesDir = temporaryFolder.newFolder();
        task.setClassesDir(classesDir);

        final DependencyCheckingTask dependencyCheckingTask = mock(DependencyCheckingTask.class);
        task.setDependencyCheckingTaskFactory(() -> dependencyCheckingTask);

        task.getActions().get(0).execute(task);

        verify(dependencyCheckingTask).execute();
    }
}
