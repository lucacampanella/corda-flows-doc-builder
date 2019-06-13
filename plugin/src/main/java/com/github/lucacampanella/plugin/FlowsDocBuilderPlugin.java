package com.github.lucacampanella.plugin;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.jvm.tasks.Jar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FlowsDocBuilderPlugin implements Plugin<Project> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowsDocBuilderPlugin.class);

    @Override
    public void apply(Project project) {

        LOGGER.info("Corda flows doc builder plugin: ");
        LOGGER.error(System.getProperty("user.dir"));

        String pathToExecJar;
        try {
            pathToExecJar = findPathToExecJar(project);
        } catch(RuntimeException e) {
            LOGGER.warn("*** Path to exec jar not found, must be specified in flowsdocbuilder DSL block" +
                    "for plugin to work");
            pathToExecJar = null;
        }
        LOGGER.trace("Found plugin file in: {}", pathToExecJar);

        final TaskCollection<Jar> jarTasks = project.getTasks().withType(Jar.class);
        List<Jar> jarTasksList = new ArrayList<>(jarTasks);

        for(Jar task : jarTasksList) {

            final String path = task.getArchivePath().getAbsolutePath(); //if modified to the non deprecated call it doesn't work this doesn't work for cardossier-cordapp
            final String taskName = task.getName() + "AnalyzerTask";
            final JarAnalyzerJavaExec javaExecTask = project.getTasks().create(taskName, JarAnalyzerJavaExec.class);
            javaExecTask.setMain("-jar");
            javaExecTask.dependsOn(task);

            javaExecTask.setPathToJar(path);
            javaExecTask.setPathToExecJar(pathToExecJar);

            LOGGER.info("Run task {} to generate graph documents for file {}", taskName, task.getArchiveName()); //idem

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