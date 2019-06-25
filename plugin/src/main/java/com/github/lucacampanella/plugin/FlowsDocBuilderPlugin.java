package com.github.lucacampanella.plugin;


import org.gradle.api.*;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.jvm.tasks.Jar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class FlowsDocBuilderPlugin implements Plugin<Project> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowsDocBuilderPlugin.class);

    private static final String BUILD_VERSION = JarExecPathFinderUtils.getBuildVersion();

    @Override
    public void apply(Project project) {

        project.getLogger().info("Corda flows doc builder plugin: ");
        project.getLogger().info("Version: {}", BUILD_VERSION);
        project.getLogger().trace(System.getProperty("user.dir"));

        final TaskCollection<Jar> jarTasks = project.getTasks().withType(Jar.class);
        List<Jar> jarTasksList = new ArrayList<>(jarTasks);

        project.getLogger().info("Creating listFlowAnalysisTasks task");
        final DefaultTask listFlowAnalysisTask = project.getTasks()
                .create("listFlowAnalysisTasks", DefaultTask.class);

        listFlowAnalysisTask.doLast(task -> {
            project.getLogger().info("Available tasks to create corda flow docs: ");
            for(Jar jarTask : jarTasksList) {
                project.getLogger().info("{}AnalyzerTask for file {}", jarTask.getName(), jarTask.getArchiveName());
            }
        });
        project.getLogger().info("Configured");

        for(Jar task : jarTasksList) {
            final String path = task.getArchivePath().getAbsolutePath(); //if modified to the non deprecated call it doesn't work this doesn't work for cardossier-cordapp
            final String taskName = task.getName() + "AnalyzerTask";
            final JarAnalyzerJavaExec javaExecTask = project.getTasks().create(taskName, JarAnalyzerJavaExec.class);
            javaExecTask.setMain("-jar");
            javaExecTask.dependsOn(task);

            javaExecTask.setPathToJar(path);

            project.getLogger().info("Run task {} to generate graph documents for file {}", taskName, task.getArchiveName()); //idem

        }
    }
}