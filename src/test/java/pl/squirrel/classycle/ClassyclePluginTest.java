package pl.squirrel.classycle;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.AndroidSourceSet;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.TaskContainer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClassyclePluginTest {

    private Project project;

    private ClassyclePlugin plugin;

    private SourceSetContainer javaSourceSets;

    private NamedDomainObjectContainer<AndroidSourceSet> androidSourceSets;

    private TaskContainer tasks;

    private ExtensionContainer extensions;

    @Before
    public void setUp() throws Exception {
        project = mock(Project.class);

        when(project.getLogger()).thenReturn(mock(Logger.class));

        final Convention convention = mock(Convention.class);
        when(project.getConvention()).thenReturn(convention);

        final JavaPluginConvention java = mock(JavaPluginConvention.class);
        when(convention.getPlugin(JavaPluginConvention.class)).thenReturn(java);

        javaSourceSets = mock(SourceSetContainer.class);
        when(javaSourceSets.iterator()).thenReturn(Collections.<SourceSet>emptyList().iterator());
        when(java.getSourceSets()).thenReturn(javaSourceSets);

        androidSourceSets = mock(NamedDomainObjectContainer.class);

        extensions = mock(ExtensionContainer.class);
        when(project.getExtensions()).thenReturn(extensions);

        tasks = mock(TaskContainer.class);

        final Task checkTask = mock(Task.class);
        when(tasks.getByName("check")).thenReturn(checkTask);
        when(project.getTasks()).thenReturn(tasks);

        final Task classycleTask = mock(Task.class);
        when(classycleTask.getProject()).thenReturn(project);
        when(project.task("classycle")).thenReturn(classycleTask);

        plugin = new ClassyclePlugin();
    }

    @Test
    public void shouldCreateJavaSourceSetClassycleTasks() {
        final SourceSet sourceSet1 = mockJavaSourceSet("ss1", new File("classesDir1"), "classesTask1");
        final SourceSet sourceSet2 = mockJavaSourceSet("ss2", new File("classesDir2"), "classesTask2");
        when(javaSourceSets.iterator()).thenReturn(Arrays.asList(sourceSet1, sourceSet2).iterator());

        final SourceSetClassycleTask task1 = mock(SourceSetClassycleTask.class);
        when(tasks.create("classycleSs1", SourceSetClassycleTask.class)).thenReturn(task1);
        final Task classesTask1 = mock(Task.class);
        when(tasks.getByName(sourceSet1.getClassesTaskName())).thenReturn(classesTask1);

        final SourceSetClassycleTask task2 = mock(SourceSetClassycleTask.class);
        when(tasks.create("classycleSs2", SourceSetClassycleTask.class)).thenReturn(task2);
        final Task classesTask2 = mock(Task.class);
        when(tasks.getByName(sourceSet2.getClassesTaskName())).thenReturn(classesTask2);

        plugin.apply(project);

        verifySourceSetClassycleTask(task1,
                                     sourceSet1.getName(),
                                     sourceSet1.getOutput().getClassesDir().getAbsolutePath(),
                                     classesTask1);

        verifySourceSetClassycleTask(task2,
                                     sourceSet2.getName(),
                                     sourceSet2.getOutput().getClassesDir().getAbsolutePath(),
                                     classesTask2);
    }

    @Test
    public void shouldCreateAndroidSourceSetClassycleTasks() {
        final AppExtension androidExtension = mock(AppExtension.class);
        when(androidExtension.getSourceSets()).thenReturn(androidSourceSets);
        when(extensions.findByType(AppExtension.class)).thenReturn(androidExtension);

        final AndroidSourceSet sourceSet1 = mockAndroidSourceSet("androidSs1");
        final AndroidSourceSet sourceSet2 = mockAndroidSourceSet("androidSs2");
        when(this.androidSourceSets.iterator()).thenReturn(Arrays.asList(sourceSet1, sourceSet2).iterator());

        final SourceSetClassycleTask task1 = mock(SourceSetClassycleTask.class);
        when(tasks.create("classycleAndroidSs1", SourceSetClassycleTask.class)).thenReturn(task1);

        final SourceSetClassycleTask task2 = mock(SourceSetClassycleTask.class);
        when(tasks.create("classycleAndroidSs2", SourceSetClassycleTask.class)).thenReturn(task2);

        final Task assembleTask = mock(Task.class);
        when(tasks.getByName("assemble")).thenReturn(assembleTask);

        plugin.apply(project);

        final String expectedClassesDirPath1 = new File(project.getBuildDir(),
                                                        "intermediates/classes/" + sourceSet1.getName()).getAbsolutePath();
        verifySourceSetClassycleTask(task1, sourceSet1.getName(), expectedClassesDirPath1, assembleTask);

        final String expectedClassesDirPath2 = new File(project.getBuildDir(),
                                                        "intermediates/classes/" + sourceSet2.getName()).getAbsolutePath();
        verifySourceSetClassycleTask(task2, sourceSet2.getName(), expectedClassesDirPath2, assembleTask);
    }

    private static SourceSet mockJavaSourceSet(final String name,
                                               final File classesDir,
                                               final String classesTaskName) {
        final SourceSet sourceSet = mock(SourceSet.class);
        when(sourceSet.getName()).thenReturn(name);
        when(sourceSet.getClassesTaskName()).thenReturn(classesTaskName);
        final SourceSetOutput output = mock(SourceSetOutput.class);
        when(output.getClassesDir()).thenReturn(classesDir);
        when(sourceSet.getOutput()).thenReturn(output);
        return sourceSet;
    }

    private static AndroidSourceSet mockAndroidSourceSet(final String name) {
        final AndroidSourceSet sourceSet = mock(AndroidSourceSet.class);
        when(sourceSet.getName()).thenReturn(name);
        return sourceSet;
    }

    private void verifySourceSetClassycleTask(final SourceSetClassycleTask task,
                                              final String sourceSetName,
                                              final String classesDirPath,
                                              final Task classesTask) {
        verify(task).setSourceSetName(sourceSetName);

        final ArgumentCaptor<File> classesDir = ArgumentCaptor.forClass(File.class);
        verify(task).setClassesDir(classesDir.capture());
        assertEquals(classesDirPath, classesDir.getValue().getAbsolutePath());

        verify(task).dependsOn(classesTask);
    }
}
