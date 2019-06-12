package com.github.lucacampanella.plugin;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.JavaExecSpec;


public class SetupJavaExecTaskArguments extends DefaultTask {
    String pathToJar = null;
    String defaultPathToExecJar = null;
    JavaExec javaExecTask = null;


    @TaskAction
    public void readFromProjectProperties() {
        final Project project = getProject();

        final FlowsDocBuilderPluginExtention extension =
                project.getExtensions().getByType(FlowsDocBuilderPluginExtention.class);

        final String outPath = extension.getOutPath();
        final String pathToExecJar = extension.getPathToExecJar();

        javaExecTask.args(pathToExecJar, pathToJar, "-o", outPath);
    }

    public void setPathToJar(String pathToJar) {
        this.pathToJar = pathToJar;
    }

    public void setJavaExecTask(JavaExec javaExecTask) {
        this.javaExecTask = javaExecTask;
    }

    public void setDefaultPathToExecJar(String defaultPathToExecJar) {
        this.defaultPathToExecJar = defaultPathToExecJar;
    }
}
