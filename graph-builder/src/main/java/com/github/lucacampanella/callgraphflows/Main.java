package com.github.lucacampanella.callgraphflows;

import com.github.lucacampanella.callgraphflows.staticanalyzer.JarAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;


public class Main implements Callable<Integer> {

    private static Logger LOGGER;

    @CommandLine.Parameters(arity = "1", index="0", paramLabel = "JarFile", description = "Jar file to process.")
    private String inputJarPath;

    @CommandLine.Parameters(arity = "0..*", index="1..*", paramLabel = "AdditionalJarFiles", description = "Additional jars to be added to classpath")
    private String[] additionalJarsPath;

    @CommandLine.Option(names = {"-o", "--output"}, defaultValue = "graphs", description = "Output folder path")
    private String outputPath;

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
        LOGGER = LoggerFactory.getLogger(Main.class);
        LOGGER.debug("Logger level = {}", loggerLevel);
        JarAnalyzer analyzer;

        if(additionalJarsPath == null) {
            analyzer = new JarAnalyzer(inputJarPath);
        }
        else {
            analyzer = new JarAnalyzer(inputJarPath, additionalJarsPath);
        }

        Drawer.drawAllStartableClasses(analyzer, outputPath);
        return 0;
    }
}
