package pl.squirrel.classycle;

import org.gradle.api.DefaultTask;

public class SourceSetClassycleTask extends DefaultTask {

    private String definitionFilePath;

    public String getDefinitionFilePath() {
        return definitionFilePath;
    }

    public void setDefinitionFilePath(final String definitionFilePath) {
        this.definitionFilePath = definitionFilePath;
    }
}
