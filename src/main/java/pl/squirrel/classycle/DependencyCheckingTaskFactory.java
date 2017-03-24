package pl.squirrel.classycle;

import classycle.ant.DependencyCheckingTask;

/**
 * Factory that creates dependency checking tasks.
 *
 * Dependency checking tasks are Ant tasks provided by Classycle lib.
 *
 * This class is created mostly for testing since PowerMockito doesn't work properly
 * with SourceSetClassycleTask.
 */
interface DependencyCheckingTaskFactory {
    /**
     * Creates new dependency checking task.
     *
     * @return dependency checking task
     */
    DependencyCheckingTask createTask();
}
