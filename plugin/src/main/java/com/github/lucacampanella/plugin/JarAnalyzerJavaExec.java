package com.github.lucacampanella.plugin;

import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class JarAnalyzerJavaExec extends JavaExec {

    String pathToJar = null;
    String outPath = "build/reports/flowsdocbuilder";
    String pathToExecJar = null;
    boolean removeJavaAgents = true; //remove agents like quasar that might be pluggen in to any javaexec task by the quasar plugin

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
    @Override
    public void exec() {
        this.args(pathToExecJar, pathToJar, "-o", outPath);

        final LogLevel gradleLogLevel = getCurrentLogLevel();
        final List<String> jvmArgs = this.getJvmArgs();

        jvmArgs.add("-Dorg.slf4j.simpleLogger.defaultLogLevel=" +
                gradleLogLevelToSLF4JLevel.get(gradleLogLevel));

        if(removeJavaAgents) {
           getLogger().info("removeJavaAgents = true");
           getLogger().info("Unfiltered args {}", jvmArgs);
           final List<String> filteredArgs = jvmArgs.stream().filter(arg -> !(arg.startsWith("-javaagent")))
                   .collect(Collectors.toList());
           getLogger().info("{}", filteredArgs);
           if(!filteredArgs.isEmpty()) {
               this.setJvmArgs(filteredArgs);
           }
        }

        super.exec();
    }

    public void setPathToJar(String pathToJar) {
        this.pathToJar = pathToJar;
    }

    public void setPathToExecJar(String pathToExecJar) {
        this.pathToExecJar = pathToExecJar;
    }

    public void setRemoveJavaAgents(boolean removeJavaAgents) {
        this.removeJavaAgents = removeJavaAgents;
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
