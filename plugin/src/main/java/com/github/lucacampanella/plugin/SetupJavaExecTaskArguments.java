package com.github.lucacampanella.plugin;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.impldep.com.google.common.collect.Lists;
import org.gradle.process.JavaExecSpec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class SetupJavaExecTaskArguments extends DefaultTask {
    String pathToJar = null;
    String defaultPathToExecJar = null;
    JavaExec javaExecTask = null;

    private static Map<LogLevel, String> gradleLogLevelToSLF4JLevel = new HashMap<>(6);
    static {
        gradleLogLevelToSLF4JLevel.put(LogLevel.ERROR, "error");
        gradleLogLevelToSLF4JLevel.put(LogLevel.QUIET, "error");
        gradleLogLevelToSLF4JLevel.put(LogLevel.WARN, "warn");
        gradleLogLevelToSLF4JLevel.put(LogLevel.LIFECYCLE, "info");
        gradleLogLevelToSLF4JLevel.put(LogLevel.INFO, "debug");
        gradleLogLevelToSLF4JLevel.put(LogLevel.DEBUG, "trace");
    }


    @TaskAction
    public void readFromProjectProperties() {
        final Project project = getProject();

        final FlowsDocBuilderPluginExtention extension =
                project.getExtensions().getByType(FlowsDocBuilderPluginExtention.class);

        final String outPath = extension.getOutPath();
        final String pathToExecJar = extension.getPathToExecJar();

        javaExecTask.args(pathToExecJar, pathToJar, "-o", outPath);

        final LogLevel gradleLogLevel = project.getLogging().getLevel();
        javaExecTask.setJvmArgs(Collections.singletonList("-Dorg.slf4j.simpleLogger.defaultLogLevel=" +
                gradleLogLevelToSLF4JLevel.get(gradleLogLevel)));
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
