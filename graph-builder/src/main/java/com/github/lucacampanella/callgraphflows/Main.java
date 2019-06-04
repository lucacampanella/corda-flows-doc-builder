package com.github.lucacampanella.callgraphflows;

import com.github.lucacampanella.callgraphflows.staticanalyzer.JarAnalyzer;

public class Main {

    public static void main(String []args) {

        JarAnalyzer analyzer;

        if(args.length > 0) {
            analyzer = new JarAnalyzer(args[0]);
        }
        else {
            System.out.println("Please provide " +
                    "jar parth as first argument and optionally out folder for graphs as second");
            return;
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

        if(args.length > 1) {
            Drawer.drawAllStartableClasses(analyzer, args[1]);
        }
        else {
            Drawer.drawAllStartableClasses(analyzer);
        }
    }
}
