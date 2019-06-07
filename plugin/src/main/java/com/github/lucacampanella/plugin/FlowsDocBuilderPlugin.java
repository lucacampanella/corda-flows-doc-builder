package com.github.lucacampanella.plugin;


import com.github.lucacampanella.callgraphflows.staticanalyzer.JarAnalyzer;
import groovy.lang.Closure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.query.ArtifactResolutionQuery;
import org.gradle.api.artifacts.result.ArtifactResolutionResult;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.base.artifact.SourcesArtifact;
import org.gradle.platform.base.Application;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlowsDocBuilderPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        System.out.println("Plugin was applied");
//        project.getTasks().withType(Jar.class).all((task) -> {
//            task.doLast((abc) ->
//                    {
//                        final String path = task.getArchivePath().getAbsolutePath();
//                        System.out.println("PATH TO JAR:" + path);
//                        Optional<Dependency> cordaJar = project.getConfigurations().getByName("compile").getAllDependencies()
//                                .stream().filter((dep) ->
//                                        dep.getName().contains("corda-core")).findAny();


//                        Optional<File> cordaJar = project.getConfigurations().getByName("compile").getFiles()
//                                .stream().filter((file) ->
//                                        file.getName().contains("corda-core")).findAny();
//
//                        if(cordaJar.isPresent()){
//                            System.out.println(cordaJar.get().getAbsolutePath());
//                        }
//                        else {
//                            cordaJar = project.getConfigurations().getByName("compileClasspath").getFiles()
//                                    .stream().filter((file) ->
//                                            file.getName().contains("corda-core")).findAny();
//
//                            if(cordaJar.isPresent()){
//                                System.out.println(cordaJar.get().getAbsolutePath());
//                            }
//                        }
//
//                        //JarAnalyzer analyzer = new JarAnalyzer(path/*, cordaJar.get().getAbsolutePath()*/);
//                        JarAnalyzer analyzer = new JarAnalyzer("/Users/camp/Desktop/cardossier-core/cardossier-core-flows/build/libs/cardossier-core-flows-FAT-SNAPSHOT.jar");
//                        System.out.println("After");
//                        analyzer.drawAllStartableClasses();
//                    }
//            );
//        });

        final File jarExecutable = project.getBuildscript().getConfigurations().getByName("classpath").getFiles()
                .stream().filter((file) -> file.getName().startsWith("graph-builder")).findFirst().orElseThrow(() -> new RuntimeException());

        System.out.println("Found plugin file in: " + jarExecutable.getPath());

        final TaskCollection<Jar> jarTasks = project.getTasks().withType(Jar.class);
        String pathToExecJar = jarExecutable.getPath();

        System.out.println("Found " + jarTasks.size() + " jar tasks");

        for(Jar task : jarTasks) {
                final String path = task.getArchivePath().getAbsolutePath();

            final String taskName = task.getName() + "AnalyzerTask";

            final JavaExec javaExecTask = project.getTasks().create(taskName, JavaExec.class);

                System.out.println("Run task " + taskName + " to generate graph documents for file " + task.getArchiveName());

                javaExecTask.setMain("-jar");
                System.out.println("after set jar");
                javaExecTask.args(pathToExecJar, path, "./graphs");
                System.out.println("after set path");
                javaExecTask.dependsOn(task);
                System.out.println("after set dependOn");
        }
    }

}