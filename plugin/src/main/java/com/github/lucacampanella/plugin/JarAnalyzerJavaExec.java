package com.github.lucacampanella.plugin;

import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.*;
import org.slf4j.event.Level;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class JarAnalyzerJavaExec extends JavaExec {

//        ERROR(40, "ERROR"),
//        WARN(30, "WARN"),
//        INFO(20, "INFO"),
//        DEBUG(10, "DEBUG"),
//        TRACE(0, "TRACE");

    private static Map<LogLevel, Level> gradleLogLevelToSLF4JLevel = new EnumMap<>(LogLevel.class);
    static {
        gradleLogLevelToSLF4JLevel.put(LogLevel.ERROR, Level.ERROR);
        gradleLogLevelToSLF4JLevel.put(LogLevel.QUIET, Level.ERROR);
        gradleLogLevelToSLF4JLevel.put(LogLevel.WARN, Level.ERROR);
        gradleLogLevelToSLF4JLevel.put(LogLevel.LIFECYCLE, Level.ERROR);
        gradleLogLevelToSLF4JLevel.put(LogLevel.INFO, Level.DEBUG);
        gradleLogLevelToSLF4JLevel.put(LogLevel.DEBUG, Level.TRACE);
    }

    String pathToJar = null;
    String outPath = "build/reports/flowsdocbuilder";
    String decompilerName = "CFR";
    String pathToExecJar = null;
    boolean removeJavaAgents = true; //remove agents like quasar that might be pluggen in to any javaexec task by the quasar plugin
    String logLevel = null;

    @TaskAction
    @Override
    public void exec() {

        if(pathToExecJar == null) { //not configured by DSL
            pathToExecJar = JarExecPathFinderUtils.getPathToExecJar(getProject());
        }

        this.args(pathToExecJar, pathToJar, "-o", outPath, "-d", decompilerName);

        getLogger().info("logLevel = {}", logLevel);
        Level slf4jLogLevel = null;
        try {
            slf4jLogLevel = Level.valueOf(logLevel.toUpperCase());
        } catch(Exception e) {
            getLogger().error("Cannot convert logLevel string \"{}\" to " +
                    "any log value, defaulting to gradle log level", logLevel, e);
        }
        getLogger().info("slf4jLogLevel = {}", slf4jLogLevel);
        if(logLevel == null) {
            final LogLevel gradleLogLevel = getCurrentLogLevel();
            //if not configured defaults to Gradle log level
            slf4jLogLevel = gradleLogLevelToSLF4JLevel.get(gradleLogLevel);
        }
        getLogger().info("slf4jLogLevel = {}", slf4jLogLevel);
        final List<String> jvmArgs = this.getJvmArgs();
        jvmArgs.add("-Dorg.slf4j.simpleLogger.defaultLogLevel=" + logLevel);

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

    public void setDecompilerName(String decompilerName) {
        this.decompilerName = decompilerName;
    }

    @Input
    public String getPathToJar() {
        return pathToJar;
    }

    @Input
    @Optional
    public String getPathToExecJar() {
        return pathToExecJar;
    }

    @Input
    public boolean isRemoveJavaAgents() {
        return removeJavaAgents;
    }

    @Input
    public String getDecompilerName() {
        return decompilerName;
    }

    @OutputDirectory
    public String getOutPath() {
        return outPath;
    }

    private LogLevel getCurrentLogLevel() {
        for(LogLevel logLevelIt : LogLevel.values()) {
            if(this.getLogger().isEnabled(logLevelIt)) {
                return logLevelIt;
            }
        }

        return LogLevel.ERROR;
    }
}
