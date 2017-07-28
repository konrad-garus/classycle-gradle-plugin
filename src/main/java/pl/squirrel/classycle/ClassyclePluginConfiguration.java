package pl.squirrel.classycle;

/**
 * Possible configuration based on: http://classycle.sourceforge.net/apidoc/classycle/ant/DependencyCheckingTask.html
 * A property reportFile is not supported, because Gradle tasks outputs needs to be set in configuration phase and not
 * in execution phase.
 */
public class ClassyclePluginConfiguration {

    private String definitionFile;
    private String excludingClasses;
    private Boolean failOnUnwantedDependencies;
    private String includingClasses;
    private Boolean mergeInnerClasses;
    private String reflectionPattern;
    private String resultRenderer;

    public ClassyclePluginConfiguration() {
    }

    public String getDefinitionFile() {
        return definitionFile;
    }

    public ClassyclePluginConfiguration setDefinitionFile(String definitionFile) {
        this.definitionFile = definitionFile;
        return this;
    }

    public String getExcludingClasses() {
        return excludingClasses;
    }

    public ClassyclePluginConfiguration setExcludingClasses(String excludingClasses) {
        this.excludingClasses = excludingClasses;
        return this;
    }

    public Boolean getFailOnUnwantedDependencies() {
        return failOnUnwantedDependencies;
    }

    public ClassyclePluginConfiguration setFailOnUnwantedDependencies(Boolean failOnUnwantedDependencies) {
        this.failOnUnwantedDependencies = failOnUnwantedDependencies;
        return this;
    }

    public String getIncludingClasses() {
        return includingClasses;
    }

    public ClassyclePluginConfiguration setIncludingClasses(String includingClasses) {
        this.includingClasses = includingClasses;
        return this;
    }

    public Boolean getMergeInnerClasses() {
        return mergeInnerClasses;
    }

    public ClassyclePluginConfiguration setMergeInnerClasses(Boolean mergeInnerClasses) {
        this.mergeInnerClasses = mergeInnerClasses;
        return this;
    }

    public String getReflectionPattern() {
        return reflectionPattern;
    }

    public ClassyclePluginConfiguration setReflectionPattern(String reflectionPattern) {
        this.reflectionPattern = reflectionPattern;
        return this;
    }

    public String getResultRenderer() {
        return resultRenderer;
    }

    public ClassyclePluginConfiguration setResultRenderer(String resultRenderer) {
        this.resultRenderer = resultRenderer;
        return this;
    }
}
