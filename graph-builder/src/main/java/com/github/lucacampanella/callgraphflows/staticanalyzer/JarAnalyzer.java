package com.github.lucacampanella.callgraphflows.staticanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class JarAnalyzer extends AnalyzerWithModel { //TODO: not all the flows are really recognized, debug
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JarAnalyzer.class);

    public JarAnalyzer(String pathToJar, String... additionalJars) {

        analysisName = pathToJar.substring(pathToJar.lastIndexOf(System.getProperty("file.separator"))+1);

        //printClasspath();

        List<String> jarsList = new LinkedList<>();

        jarsList.add(pathToJar);
        if(additionalJars.length > 0) {
            jarsList.addAll(Arrays.asList(additionalJars));
        }

        CustomJarLauncher jr = new CustomJarLauncher(jarsList);

//        final InputStream dependenciesTxt = JarAnalyzer.class.getClassLoader().getResourceAsStream("DependenciesToBeAdded.txt");//
//        final String[] paths = new BufferedReader(new InputStreamReader(dependenciesTxt)).lines()
//                .filter(str -> str.contains(".jar"))
//                .toArray(String[]::new);//
//        for(String path : paths) {
//           LOGGER.trace("|" + path + "|");
//        }//
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
