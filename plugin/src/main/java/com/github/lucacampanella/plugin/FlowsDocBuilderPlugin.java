package com.github.lucacampanella.plugin;


import com.github.lucacampanella.callgraphflows.staticanalyzer.JarAnalyzer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.jvm.tasks.Jar;

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
//                        System.out.println("Before");
//                        System.out.flush();
////                        Optional<Dependency> cordaJar = project.getConfigurations().getByName("compile").getAllDependencies()
////                                .stream().filter((dep) ->
////                                        dep.getName().contains("corda-core")).findAny();
//
//
////                        Optional<File> cordaJar = project.getConfigurations().getByName("compile").getFiles()
////                                .stream().filter((file) ->
////                                        file.getName().contains("corda-core")).findAny();
////
////                        if(cordaJar.isPresent()){
////                            System.out.println(cordaJar.get().getAbsolutePath());
////                            try {
////                                Files.write(Paths.get("testfile12341234.txt"), cordaJar.get().getAbsolutePath().getBytes());
////                            } catch (IOException e) {
////                                e.printStackTrace();
////                            }
////                        }
////                        else {
////                            cordaJar = project.getConfigurations().getByName("compileClasspath").getFiles()
////                                    .stream().filter((file) ->
////                                            file.getName().contains("corda-core")).findAny();
////
////                            if(cordaJar.isPresent()){
////                                try {
////                                    Files.write(Paths.get("testfile12341234.txt"), cordaJar.get().getAbsolutePath().getBytes());
////                                } catch (IOException e) {
////                                    e.printStackTrace();
////                                }
////                                System.out.println(cordaJar.get().getAbsolutePath());
////                            }
////                        }
//
//
//                        JarAnalyzer analyzer = new JarAnalyzer(path/*, cordaJar.get().getAbsolutePath()*/);
//                        //JarAnalyzer analyzer = new JarAnalyzer("/Users/camp/Desktop/cardossier-core/cardossier-core-flows/build/libs/cardossier-core-flows-FAT-SNAPSHOT.jar");
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

        final TaskCollection<Jar> jarTasks = project.getTasks().withType(Jar.class);

        int i = 0;
        for(Jar task : jarTasks) {
            try {
                final String path = task.getArchivePath().getAbsolutePath();
                System.out.println(System.getProperty("user.dir"));
                final InputStream jarStream =
                        FlowsDocBuilderPlugin.class.getClassLoader()
                                .getResourceAsStream("graph-builder-1.0-SNAPSHOT-all.jar");

                System.out.println(FlowsDocBuilderPlugin.class.getClassLoader()
                        .getResource("graph-builder-1.0-SNAPSHOT-all.jar"));

                Path jarTmpDir = Files.createTempDirectory("jarTmpDir");

                byte[] buffer = new byte[jarStream.available()];
                jarStream.read(buffer);

                File targetFile = Paths.get(jarTmpDir.toString(),
                        "graph-builder-1.0-SNAPSHOT-all.jar").toFile();
                OutputStream outStream = new FileOutputStream(targetFile);
                outStream.write(buffer);

                final JavaExec javaExecTask = project.getTasks().create("jarAnalyzerTask" + i, JavaExec.class);

                javaExecTask.setMain("-jar");
                javaExecTask.args(targetFile.getPath(), path, "~/builtGraphs");
                task.finalizedBy(javaExecTask);
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}