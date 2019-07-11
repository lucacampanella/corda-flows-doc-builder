package com.github.lucacampanella.callgraphflows.staticanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class JarAnalyzer extends AnalyzerWithModel {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JarAnalyzer.class);

    public JarAnalyzer(String pathToJar) {
        this(null, pathToJar, null);
    }

    public JarAnalyzer(String decompilerName, String pathToJar, String[] additionalJars) {

        analysisName = pathToJar.substring(pathToJar.lastIndexOf(System.getProperty("file.separator"))+1);

        List<String> jarsList = new LinkedList<>();
        jarsList.add(pathToJar);
        if(additionalJars != null && additionalJars.length > 0) {
            jarsList.addAll(Arrays.asList(additionalJars));
        }
        CustomJarLauncher jr;

        if(decompilerName != null && decompilerName.equalsIgnoreCase("CFR")) {
            LOGGER.trace("Using CFR (default) decompiler");
            jr = new CustomJarLauncher.Builder(jarsList)
                    .withDecompilerEnum(DecompilerEnum.CFR).build();
        }
        else if(decompilerName != null && decompilerName.equalsIgnoreCase("Fernflower")) {
            LOGGER.trace("Using Fernflower decompiler");
            jr = new CustomJarLauncher.Builder(jarsList)
                    .withDecompilerEnum(DecompilerEnum.FERNFLOWER).build();
        }
        else {
            LOGGER.error("Decompiler name {} not recognised, using default decompiler", decompilerName);
            jr = new CustomJarLauncher.Builder(jarsList).build();
        }

        jr.buildModel();
        model = jr.getModel();
    }
}
