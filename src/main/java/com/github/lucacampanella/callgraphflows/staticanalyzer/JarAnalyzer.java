package com.github.lucacampanella.callgraphflows.staticanalyzer;

import net.corda.core.flows.InitiatedBy;
import net.corda.core.flows.InitiatingFlow;
import spoon.JarLauncher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtPath;
import spoon.reflect.path.CtPathStringBuilder;
import spoon.reflect.visitor.filter.AnnotationFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtFieldReadImpl;

import java.util.*;

public class JarAnalyzer extends AnalyzerWithModel { //TODO: not all the flows are really recognized, debug

    public JarAnalyzer(String pathToJar) {

        List<String> jarsList =
                new LinkedList<String>();
        //jarsList.add("./flow-callgraph-drawer/lib/corda-flow-base-0.2.1903271115.jar");

        jarsList.add(pathToJar);

        CustomJarLauncher jr = new CustomJarLauncher(jarsList);

        jr.buildModel();

        model = jr.getModel();
    }
}
