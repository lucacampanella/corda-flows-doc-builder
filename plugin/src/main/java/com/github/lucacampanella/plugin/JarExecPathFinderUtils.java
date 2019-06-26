package com.github.lucacampanella.plugin;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.UnknownConfigurationException;

import java.io.File;
import java.util.Set;

public final class JarExecPathFinderUtils {

    private static final String CONFIGURATION_NAME = "analyzerExecutable";
    private static String pathToExecJar = null;
    private static String buildVersion = null;

    public static String getPathToExecJar(Project project) {
        if (pathToExecJar == null) {
            Configuration config;
            try {
                config = project.getConfigurations().getByName(CONFIGURATION_NAME);
            } catch (UnknownConfigurationException e) {
                config = project.getConfigurations().create(CONFIGURATION_NAME)
                        .setVisible(false)
                        .setDescription("The jar file needed to run the corda flows doc builder plugin");
                config.setTransitive(false);
            }
            initializeBuildVersion();

            final String dependency = "com.github.lucacampanella:graph-builder:" + buildVersion + ":all";
            config.defaultDependencies(dependencies ->
                    dependencies.add(project.getDependencies().create(dependency)));

            final Set<File> configFiles = config.getFiles();

            pathToExecJar = configFiles.stream().map(file -> file.getPath()).filter(
                    path -> path.contains("graph-builder")).findFirst()
                    .orElseThrow(() -> new RuntimeException("Could not find executable jar"));
        }

        return pathToExecJar;
    }

    private static void initializeBuildVersion() {
        if(buildVersion == null) {
            buildVersion = FlowsDocBuilderPlugin.class.getPackage().getImplementationVersion();
        }
    }

    public static String getBuildVersion(){
        initializeBuildVersion();
        return buildVersion;
    }
}
