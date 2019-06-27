package com.github.lucacampanella.callgraphflows;

import com.github.lucacampanella.callgraphflows.staticanalyzer.DecompilerEnum;
import com.github.lucacampanella.callgraphflows.staticanalyzer.JarAnalyzer;
import com.github.lucacampanella.callgraphflows.staticanalyzer.SourceAndJarAnalyzer;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;


public class Main implements Callable<Integer> {

    @CommandLine.Parameters(arity = "1..*", index="0..*", paramLabel = "filesToAnalyze",
            description = "The paths to the files that need to be analyzed, they can be " +
                    ".java files, folders or .jar files")
    private String[] filesPaths;

    @CommandLine.Option(names = {"-o", "--output"}, defaultValue = "graphs", description = "Output folder path")
    private String outputPath;

    @CommandLine.Option(names = {"-d", "--decompiler"}, defaultValue = "CFR", description = "Decompiler, choose between CFR and Fernflower")
    private String decompilerName;

    @CommandLine.Option(names = {"-l", "--draw-line-numbers"}, description = "draw the line numbers")
    boolean drawLineNumbers = false;

    public static void main(String []args) throws IOException {

        final Main app = CommandLine.populateCommand(new Main(), args);
        final int exitCode = app.call();

        System.exit(exitCode);
    }

    @Override
    public Integer call() throws IOException {
        final String loggerLevel = System.getProperty("org.slf4j.simpleLogger.defaultLogLevel");
        if(loggerLevel == null) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
        }
        LoggerFactory.getLogger(Main.class).trace("Logger level = {}", loggerLevel);
        SourceAndJarAnalyzer analyzer = new SourceAndJarAnalyzer(filesPaths,
                DecompilerEnum.fromStringOrDefault(decompilerName));

        LoggerFactory.getLogger(Main.class).trace("drawLineNumbers = {}", drawLineNumbers);
        DrawerUtil.setDrawLineNumbers(drawLineNumbers);
        DrawerUtil.drawAllStartableClasses(analyzer, outputPath);
        return 0;
    }
}
