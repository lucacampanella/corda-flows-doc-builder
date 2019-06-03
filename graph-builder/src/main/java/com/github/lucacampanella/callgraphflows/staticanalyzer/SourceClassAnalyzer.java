package com.github.lucacampanella.callgraphflows.staticanalyzer;

import spoon.Launcher;

import java.util.List;

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
        Launcher spoon = new Launcher();
        for(String path : pathsToClasses) {
            spoon.addInputResource(path);
        }
        spoon.buildModel();
        model = spoon.getModel();
    }
}
