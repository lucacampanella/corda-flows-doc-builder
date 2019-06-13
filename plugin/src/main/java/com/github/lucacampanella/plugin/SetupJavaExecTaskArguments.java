package com.github.lucacampanella.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;


public class SetupJavaExecTaskArguments extends DefaultTask {

    String pathToJar = null;
    String defaultPathToExecJar = null;
    JavaExec javaExecTask = null;

    private static Map<LogLevel, String> gradleLogLevelToSLF4JLevel = new EnumMap<>(LogLevel.class);
    static {
        gradleLogLevelToSLF4JLevel.put(LogLevel.ERROR, "error");
        gradleLogLevelToSLF4JLevel.put(LogLevel.QUIET, "error");
        gradleLogLevelToSLF4JLevel.put(LogLevel.WARN, "error");
        gradleLogLevelToSLF4JLevel.put(LogLevel.LIFECYCLE, "error");
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

        final LogLevel gradleLogLevel = getCurrentLogLevel();
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

    private LogLevel getCurrentLogLevel() {
        for(LogLevel logLevel : LogLevel.values()) {
            if(this.getLogger().isEnabled(logLevel)) {
                return logLevel;
            }
        }

        return LogLevel.ERROR;
    }
}
