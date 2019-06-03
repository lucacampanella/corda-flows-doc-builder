package com.github.lucacampanella.plugin;


import com.github.lucacampanella.callgraphflows.staticanalyzer.JarAnalyzer;
import groovy.lang.Closure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
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
import java.util.Optional;

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

//        project.getTasks().create("myTask", MyTask.class);
//        //project.getTasksByName("myTask", false).forEach((task) -> project.;
//        project.getTasksByName("build", false).forEach( (task) -> {
//            task.finalizedBy("myTask");
//        });


//        System.out.println(project.getConfigurations());
//        for (Configuration conf : project.getConfigurations()) {
//            System.out.println(conf);
//        if(conf.toString().equals("configuration ':apiElements'") || conf.toString().equals("configuration ':implementation'"))
//            continue;
//
//            conf.forEach(System.out::println);
//            System.out.println(conf.getAllDependencies().size());
//            conf.getAllDependencies().forEach(System.out::println);
//        }
//
//        System.out.println(project.getRepositories().size());
//        project.getRepositories().forEach((rep) -> {
//            System.out.println(rep.getName());
//        });
//
//        final Configuration lol = project.getConfigurations().create("lol");
//
//        project.getDependencies().add("lol", "" +
//                "project(path: ':graph-builder', configuration: 'shadow')");
//
//        project.getConfigurations().getByName("lol").getFiles().forEach(System.out::println);

//        final ArtifactResolutionResult result = project.getDependencies().createArtifactResolutionQuery().
//                forModule("com.github.lucacampanella", "graph-builder", "1.0-SNAPSHOT-all")
//                .withArtifacts(Application.class, SourcesArtifact.class)
//                .execute();
//        System.out.println(result);
//        result.getComponents().forEach((comp) -> {
//            System.out.println(comp);
//            System.out.println(comp.getId());
//            final SoftwareComponent softComp = project.getComponents().getByName(comp.getId().getDisplayName());
//            System.out.println(softComp);
//        });

//        project.getConfigurations().forEach((conf) -> {
//            System.out.println(conf);
//            conf.getFiles().forEach(System.out::println);
//        });


        //project(path: ':graph-builder', configuration: 'shadow')

        final TaskCollection<Jar> jarTasks = project.getTasks().withType(Jar.class);
        String pathToExecJar =
                "/Users/camp/projects/corda-flows-doc-builder/graph-builder/build/libs/graph-builder-1.0-SNAPSHOT-all.jar";

        int i = 0;
        for(Jar task : jarTasks) {
//            try {
                final String path = task.getArchivePath().getAbsolutePath();

                final JavaExec javaExecTask = project.getTasks().create(task.getName()
                        + "AnalyzerTask", JavaExec.class);

                javaExecTask.setMain("-jar");
                javaExecTask.args(pathToExecJar, path, "./graphs");
                //javaExecTask.args(pathToExecJar, "/Users/camp/Desktop/cardossier-core/cardossier-core-flows/build/libs/cardossier-core-flows-FAT-SNAPSHOT.jar", "./graphs");
                javaExecTask.dependsOn(task);
                i++;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

}