package com.github.lucacampanella.plugin;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.jvm.tasks.Jar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class FlowsDocBuilderPlugin implements Plugin<Project> {

    private static Map<LogLevel, String> gradleLogLevelToSLF4JLevel = new HashMap<>(6);
    static {
        gradleLogLevelToSLF4JLevel.put(LogLevel.ERROR, "error");
        gradleLogLevelToSLF4JLevel.put(LogLevel.QUIET, "error");
        gradleLogLevelToSLF4JLevel.put(LogLevel.WARN, "warn");
        gradleLogLevelToSLF4JLevel.put(LogLevel.LIFECYCLE, "info");
        gradleLogLevelToSLF4JLevel.put(LogLevel.INFO, "debug");
        gradleLogLevelToSLF4JLevel.put(LogLevel.DEBUG, "trace");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowsDocBuilderPlugin.class);

    @Override
    public void apply(Project project) {

        LOGGER.info("Corda flows doc builder plugin: ");
        LOGGER.error(System.getProperty("user.dir"));

        String pathToExecJar = findPathToExecJar(project);
        LOGGER.trace("Found plugin file in: " + pathToExecJar);

        final FlowsDocBuilderPluginExtention extension =
                project.getExtensions().create("flowsdocbuilder", FlowsDocBuilderPluginExtention.class);

        final String outPath = extension.getOutPath();

        final LogLevel gradleLogLevel = project.getLogging().getLevel();

        final TaskCollection<Jar> jarTasks = project.getTasks().withType(Jar.class);
        List<Jar> jarTasksList = new ArrayList<>(jarTasks);

        for(Jar task : jarTasksList) {

            final String path = task.getArchivePath().getAbsolutePath(); //if modified to the non deprecated call it doesn't work this doesn't work for cardossier-cordapp
            final String taskName = task.getName() + "AnalyzerTask";
            final JavaExec javaExecTask = project.getTasks().create(taskName, JavaExec.class);

            LOGGER.info("Run task " + taskName + " to generate graph documents for file " + task.getArchiveName()); //idem

            javaExecTask.setMain("-jar");
            javaExecTask.args(pathToExecJar, path, "-o " + outPath);
            javaExecTask.dependsOn(task);
        }
    }

    private static String findPathToExecJar(Project project) {
        final Configuration config = project.getConfigurations().create("analyzerExecutable")
                .setVisible(false)
                .setDescription("The jar file needed to run the corda flows doc builder plugin");

        project.getRepositories().maven(
                mavenArtifactRepository ->
                        mavenArtifactRepository.setUrl("https://dl.bintray.com/lucacampanella/mvn-release"));

        config.setTransitive(false);

        final String dependency = "com.github.lucacampanella:graph-builder:+:all";
        config.defaultDependencies(dependencies ->
                dependencies.add(project.getDependencies().create(dependency)));

        final Set<File> configFiles = config.getFiles();

        return configFiles.stream().map(file -> file.getPath()).filter(
                path -> path.contains("graph-builder")).findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find executable jar"));
    }

}