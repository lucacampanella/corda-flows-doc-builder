package com.github.lucacampanella.plugin;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FlowsDocBuilderPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        System.out.println("Corda flows doc builder plugin: ");

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

        final TaskCollection<Jar> jarTasks = project.getTasks().withType(Jar.class);
        String pathToExecJar = configFiles.stream().map(file -> file.getPath()).filter(
                path -> path.contains("graph-builder")).findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find executable jar"));

        System.out.println("Found plugin file in: " + pathToExecJar);


        List<Jar> jarTasksList = new ArrayList<>(jarTasks);

        for(Jar task : jarTasksList) {

            final String path = task.getArchivePath().getAbsolutePath(); //if modified to the non deprecated call it doesn't work this doesn't work for cardossier-cordapp
            final String taskName = task.getName() + "AnalyzerTask";
            final JavaExec javaExecTask = project.getTasks().create(taskName, JavaExec.class);

            System.out.println("Run task " + taskName + " to generate graph documents for file " + task.getArchiveName()); //idem

            javaExecTask.setMain("-jar");
            javaExecTask.args(pathToExecJar, path, "./build/generated/graphs");
            javaExecTask.dependsOn(task);
        }
    }

}