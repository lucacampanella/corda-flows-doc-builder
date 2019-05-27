package com.github.lucacampanella.callgraphflows;

import com.github.lucacampanella.callgraphflows.staticanalyzer.JarAnalyzer;
import net.corda.core.flows.StartableByRPC;
import spoon.reflect.declaration.CtClass;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String []args) {

//        System.out.println(System.getProperty("java.io.tmpdir"));

        JarAnalyzer analyzer;

        if(args.length > 0) {
            analyzer = new JarAnalyzer(args[0]);
        }
        else {

            analyzer = new JarAnalyzer(
                    "./workflows-java/build/libs/cordapp-example-workflows.jar");
        }

//        final Map<CtClass, CtClass> initiatedClassToInitiatingMap = analyzer.getInitiatedClassToInitiatingMap();
//
//        System.out.println("***** found these classes referring to each other in the jar: ");
//        initiatedClassToInitiatingMap.forEach((initiatedClass, initiatingClass) -> System.out.println(
//                initiatingClass.getQualifiedName() + " -> " + initiatedClass.getQualifiedName()));
//
//        for (Map.Entry<CtClass, CtClass> entry : initiatedClassToInitiatingMap.entrySet()) {
//            CtClass initiatedClass = entry.getKey();
//            CtClass initiatingClass = entry.getValue();
//
//            boolean result = StaticAnalyzer.checkTwoClassesAndBuildGraphs(
//                    initiatingClass, initiatedClass, "example/test/");
//
//            System.out.println("**** between class " + initiatingClass.getSimpleName() +
//                    " and " + initiatedClass.getSimpleName() + " result: " + result);
//        }

        final List<CtClass> startableByRPCClasses = analyzer.getClassesByAnnotation(StartableByRPC.class);
        System.out.println("Found these classes annotated with @StartableByRPC: ");
        startableByRPCClasses.forEach(klass -> {
            System.out.println("**** Analyzing class " + klass.getQualifiedName());
            try {
                analyzer.drawFromClass(klass, "example/testStartable/" + klass.getQualifiedName() + ".svg");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
