package com.github.lucacampanella.plugin;

import groovyjarjarpicocli.CommandLine;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.Optional;
import org.slf4j.event.Level;

import java.util.*;
import java.util.stream.Collectors;


public class JarAnalyzerJavaExec extends JavaExec {

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
    boolean drawLineNumbers = false;
    boolean analyzeOnlySourceFiles = false;
    boolean removeJavaAgents = true; //remove agents like quasar that might be pluggen in to any javaexec task by the quasar plugin
    String logLevel = null;
    List<String> sourceFilesPath = null;
    boolean drawArrows = true;
    boolean drawBoxes = true;
    boolean drawReturn = false;
    boolean drawThrow = true;
    boolean drawBreakContinue = true;
    boolean drawStatementsWithRelevantMethods = false;

    @TaskAction
    @Override
    public void exec() {

        if(pathToExecJar == null) { //not configured by DSL
            pathToExecJar = JarExecPathFinderUtils.getPathToExecJar(getProject());
        }

        List<String> args = new ArrayList<>(Arrays.asList(pathToExecJar, pathToJar));

        if(sourceFilesPath != null) {
            args.addAll(sourceFilesPath);
        }
        args.addAll(Arrays.asList("-o", outPath, "-d", decompilerName));
        if(drawLineNumbers) {
            getLogger().info("drawLineNumbers = true");
            args.add("-l");
        }
        if(analyzeOnlySourceFiles) {
            getLogger().info("analyzeOnlySourceFiles = true");
            args.add("-s");
        }
        if(!drawArrows) {
            getLogger().info("drawArrows = false");
            args.add("--no-arrows");
        }
        if(!drawBoxes) {
            getLogger().info("drawBoxes = false");
            args.add("--no-box-subflows");
        }
        if(drawReturn) {
            getLogger().info("drawReturn = true");
            args.add("--draw-return");
        }
        if(!drawThrow) {
            getLogger().info("drawThrow = false");
            args.add("--no-draw-throw");
        }
        if(!drawBreakContinue) {
            getLogger().info("drawBreakContinue = false");
            args.add("--no-draw-break-continue");
        }
        if(drawStatementsWithRelevantMethods) {
            getLogger().info("drawStatementsWithRelevantMethods = true");
            args.add("--draw-statements-with-relevant-methods");
        }
        getLogger().info("args = {}", args);

        this.setArgs(args);

        Level slf4jLogLevel = null;
        if(logLevel != null) {
            try {
                slf4jLogLevel = Level.valueOf(logLevel.toUpperCase());
            } catch (Exception e) {
                getLogger().error("Cannot convert logLevel string \"{}\" to " +
                        "any log value, defaulting to gradle log level", logLevel, e);
            }
        }
        if(logLevel == null) {
            final LogLevel gradleLogLevel = getCurrentLogLevel();
            //if not configured defaults to Gradle log level
            slf4jLogLevel = gradleLogLevelToSLF4JLevel.get(gradleLogLevel);
        }
        final List<String> jvmArgs = this.getJvmArgs();
        jvmArgs.add("-Dorg.slf4j.simpleLogger.defaultLogLevel=" + slf4jLogLevel);

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

    public void setOutPath(String outPath) {
        this.outPath = outPath;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogLevel() {
        return logLevel;
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
    public boolean isDrawLineNumbers() {
        return drawLineNumbers;
    }

    @Input
    public String getDecompilerName() {
        return decompilerName;
    }

    @OutputDirectory
    public String getOutPath() {
        return outPath;
    }

    @Input
    @Optional
    public List<String> getSourceFilesPath() {
        return sourceFilesPath;
    }

    @Input
    public boolean isAnalyzeOnlySourceFiles() {
        return analyzeOnlySourceFiles;
    }

    @Input
    public boolean isDrawArrows() {
        return drawArrows;
    }

    @Input
    public boolean isDrawBoxes() {
        return drawBoxes;
    }

    @Input
    public boolean isDrawReturn() {
        return drawReturn;
    }

    @Input
    public boolean isDrawThrow() {
        return drawThrow;
    }

    @Input
    public boolean isDrawBreakContinue() {
        return drawBreakContinue;
    }

    @Input
    public boolean isDrawStatementsWithRelevantMethods() {
        return drawStatementsWithRelevantMethods;
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
