package com.github.lucacampanella.plugin;


import com.github.lucacampanella.callgraphflows.staticanalyzer.JarAnalyzer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class FlowsDocBuilderPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        System.out.println("Plugin was applied");
        project.getTasks().withType(Jar.class).all((task) -> {
            task.doLast((abc) ->
                    {
                        final String path = task.getArchivePath().getAbsolutePath();
                        System.out.println("PATH TO JAR:" + path);
                        System.out.println("Before");
                        System.out.flush();
//                        Optional<Dependency> cordaJar = project.getConfigurations().getByName("compile").getAllDependencies()
//                                .stream().filter((dep) ->
//                                        dep.getName().contains("corda-core")).findAny();


//                        Optional<File> cordaJar = project.getConfigurations().getByName("compile").getFiles()
//                                .stream().filter((file) ->
//                                        file.getName().contains("corda-core")).findAny();
//
//                        if(cordaJar.isPresent()){
//                            System.out.println(cordaJar.get().getAbsolutePath());
//                            try {
//                                Files.write(Paths.get("testfile12341234.txt"), cordaJar.get().getAbsolutePath().getBytes());
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        else {
//                            cordaJar = project.getConfigurations().getByName("compileClasspath").getFiles()
//                                    .stream().filter((file) ->
//                                            file.getName().contains("corda-core")).findAny();
//
//                            if(cordaJar.isPresent()){
//                                try {
//                                    Files.write(Paths.get("testfile12341234.txt"), cordaJar.get().getAbsolutePath().getBytes());
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                                System.out.println(cordaJar.get().getAbsolutePath());
//                            }
//                        }


                        //JarAnalyzer analyzer = new JarAnalyzer(path/*, cordaJar.get().getAbsolutePath()*/);
                        JarAnalyzer analyzer = new JarAnalyzer("/Users/camp/Desktop/cardossier-core/cardossier-core-flows/build/libs/cardossier-core-flows-FAT-SNAPSHOT.jar");
                        System.out.println("After");
                        analyzer.drawAllStartableClasses();
                    }
            );
        });

        //project.getTasks().create("myTask", MyTask.class);
    }

}