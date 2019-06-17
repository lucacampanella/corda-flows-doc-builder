package com.github.lucacampanella.callgraphflows.staticanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class JarAnalyzer extends AnalyzerWithModel {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JarAnalyzer.class);

    public JarAnalyzer(String pathToJar, String... additionalJars) {

        analysisName = pathToJar.substring(pathToJar.lastIndexOf(System.getProperty("file.separator"))+1);

        List<String> jarsList = new LinkedList<>();
        jarsList.add(pathToJar);
        if(additionalJars.length > 0) {
            jarsList.addAll(Arrays.asList(additionalJars));
        }

        CustomJarLauncher jr = new CustomJarLauncher(jarsList);
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
