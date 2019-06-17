package com.github.lucacampanella.callgraphflows.staticanalyzer;

import spoon.Launcher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SourceClassAnalyzer extends AnalyzerWithModel {

    public SourceClassAnalyzer(String pathToClass) {
        init(new String[] {pathToClass});
    }

    public SourceClassAnalyzer(String... pathsToClass) {
        init(pathsToClass);
    }

    public SourceClassAnalyzer(List<String> pathsToClass) {
        init(pathsToClass.toArray(new String[pathsToClass.size()]));
    }

    private void init(String [] pathsToClasses) {

        analysisName = Arrays.stream(pathsToClasses).map(
                pathToJar -> pathToJar.substring(pathToJar.lastIndexOf(System.getProperty("file.separator"))+1)).
                collect(Collectors.joining(", "));

        Launcher spoon = new Launcher();
        for(String path : pathsToClasses) {
            spoon.addInputResource(path);
        }
        spoon.buildModel();
        model = spoon.getModel();
    }
}
