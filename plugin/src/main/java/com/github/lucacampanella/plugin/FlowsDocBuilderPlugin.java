package com.github.lucacampanella.plugin;


import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.UnknownConfigurationException;
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

    private static final String buildVersion = JarExecPathFinder.getBuildVersion();

    @Override
    public void apply(Project project) {

        LOGGER.info("Corda flows doc builder plugin: ");
        LOGGER.info("Version: " + buildVersion);
        LOGGER.error(System.getProperty("user.dir"));

//        String pathToExecJar;
//        try {
//            pathToExecJar = findPathToExecJar(project);
//        } catch(RuntimeException e) {
//            LOGGER.warn("*** Path to exec jar not found, must be specified in flowsdocbuilder DSL block" +
//                    "for plugin to work");
//            pathToExecJar = null;
//        }
//        LOGGER.trace("Found plugin file in: {}", pathToExecJar);

        final TaskCollection<Jar> jarTasks = project.getTasks().withType(Jar.class);
        List<Jar> jarTasksList = new ArrayList<>(jarTasks);

        final DefaultTask listFlowAnalysisTask = project.getTasks()
                .create("listFlowAnalysisTasks", DefaultTask.class);

        listFlowAnalysisTask.doLast(task -> {
            System.out.println("Available tasks to create corda flow docs: ");
            for(Jar jarTask : jarTasksList) {
                System.out.println(jarTask.getName() + "AnalyzerTask" + " for file " + jarTask.getArchiveName());
            }
        });

        for(Jar task : jarTasksList) {

            final String path = task.getArchivePath().getAbsolutePath(); //if modified to the non deprecated call it doesn't work this doesn't work for cardossier-cordapp
            final String taskName = task.getName() + "AnalyzerTask";
            final JarAnalyzerJavaExec javaExecTask = project.getTasks().create(taskName, JarAnalyzerJavaExec.class);
            javaExecTask.setMain("-jar");
            javaExecTask.dependsOn(task);

            javaExecTask.setPathToJar(path);
//            javaExecTask.setPathToExecJar(pathToExecJar);

            LOGGER.info("Run task {} to generate graph documents for file {}", taskName, task.getArchiveName()); //idem

        }
    }
}