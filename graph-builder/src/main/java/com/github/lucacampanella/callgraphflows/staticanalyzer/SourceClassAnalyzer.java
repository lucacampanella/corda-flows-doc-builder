package com.github.lucacampanella.callgraphflows.staticanalyzer;

import net.corda.core.flows.InitiatedBy;
import spoon.Launcher;
import spoon.legacy.NameFilter;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.visitor.filter.AnnotationFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtFieldReadImpl;

import java.util.*;

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
