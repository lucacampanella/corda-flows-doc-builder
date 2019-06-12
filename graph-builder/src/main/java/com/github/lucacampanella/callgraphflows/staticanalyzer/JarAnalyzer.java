package com.github.lucacampanella.callgraphflows.staticanalyzer;

import net.corda.core.flows.InitiatedBy;
import net.corda.core.flows.InitiatingFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

public class JarAnalyzer extends AnalyzerWithModel { //TODO: not all the flows are really recognized, debug
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JarAnalyzer.class);

    public JarAnalyzer(String pathToJar, String... additionalJars) {

        analysisName = pathToJar.substring(pathToJar.lastIndexOf(System.getProperty("file.separator"))+1);

        //printClasspath();

        List<String> jarsList =
                new LinkedList<String>();
        //jarsList.add("./flow-callgraph-drawer/lib/corda-flow-base-0.2.1903271115.jar");

        jarsList.add(pathToJar);
        if(additionalJars.length > 0) {
            jarsList.addAll(Arrays.asList(additionalJars));
        }

        CustomJarLauncher jr = new CustomJarLauncher(jarsList);

//        final InputStream dependenciesTxt = JarAnalyzer.class.getClassLoader().getResourceAsStream("DependenciesToBeAdded.txt");
//
//        final String[] paths = new BufferedReader(new InputStreamReader(dependenciesTxt)).lines()
//                .filter(str -> str.contains(".jar"))
//                .toArray(String[]::new);
//
//        for(String path : paths) {
//           LOGGER.trace("|" + path + "|");
//        }
//
//        jr.getEnvironment().setSourceClasspath(paths);


        //LOGGER.trace("CLASSPATH = " + jr.getEnvironment().getSourceClasspath());

        //jr.getEnvironment().setNoClasspath(false);

        jr.buildModel();

        model = jr.getModel();


    }

    private static void printClasspath() {
        ClassLoader cl = JarAnalyzer.class.getClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();

        for(URL url: urls){
            LOGGER.trace(url.getFile());
        }
    }
}
