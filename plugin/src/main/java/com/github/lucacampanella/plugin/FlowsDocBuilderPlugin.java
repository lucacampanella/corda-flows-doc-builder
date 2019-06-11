package com.github.lucacampanella.plugin;


import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.internal.impldep.org.junit.After;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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


        /*project.getDependencies().create("runtime \"com.github.lucacampanella:graph-builder:0.59.0:all\"");
        System .out.println(project.getConfigurations().getByName("runtime").getFiles().size());
        project.getConfigurations().getByName("runtime").getFiles().forEach(System.out::println);*/

//        final File jarExecutable = project.getBuildscript().getConfigurations().getByName("classpath").getFiles()
//                .stream().filter((file) -> file.getName().startsWith("graph-builder")).findFirst()
//                .orElseThrow(() -> new RuntimeException("Could not find executable jar"));

//        String version = "0.53.0";
//        String jarName = "graph-builder-" + version + "-all.jar";
//        String url = "https://dl.bintray.com/lucacampanella/mvn-release/com/github/lucacampanella/graph-builder/0.53.0/"
//                + jarName;
//
//        System.out.println(url);
//
//        File executableFolder = new File("tmp/");
//        executableFolder.mkdirs();
//        File dowloadedJarExecutable = Paths.get(executableFolder.getPath(), jarName).toFile();
//
//        System.out.println(executableFolder.getAbsolutePath());
//        System.out.println(dowloadedJarExecutable.getAbsolutePath());
//
//        try {
//            FileUtils.copyURLToFile(
//                    new URL(url),
//                    dowloadedJarExecutable,
//                    100000,
//                    100000);
//        } catch (IOException e) {
//            System.out.println("could not download file: \n");
//            e.printStackTrace();
//            new RuntimeException(e);
//        }


//        ReadableByteChannel readChannel = null;
//        try {
//            readChannel = Channels.newChannel(new URL(url).openStream());
//        } catch (IOException e) {
//            System.out.println("could not open channel");
//            new RuntimeException(e);
//        }
//
//        FileOutputStream fileOS = null;
//        try {
//            fileOS = new FileOutputStream(dowloadedJarExecutable.getAbsolutePath());
//            FileChannel writeChannel = fileOS.getChannel();
//            writeChannel.transferFrom(readChannel, 0, Long.MAX_VALUE);
//        } catch (IOException e) {
//            System.out.println("could not write on file");
//            new RuntimeException(e);
//        }
        System.out.println(System.getProperty("user.dir"));

        final Configuration config = project.getConfigurations().create("analyzerExecutable")
                .setVisible(false)
                .setDescription("The jar file needed to run the corda flows doc builder plugin");

        project.getRepositories().maven(
                mavenArtifactRepository ->
                        mavenArtifactRepository.setUrl("https://dl.bintray.com/lucacampanella/mvn-release"));

        final String dependency = "com.github.lucacampanella:graph-builder:0.231.0:all";
        System.out.println(dependency);
        config.defaultDependencies(dependencies ->
                dependencies.add(project.getDependencies().create(dependency)));

        final Set<File> configFiles = config.getFiles();
        System.out.println("configFiles = " + configFiles.size());
        configFiles.forEach(System.out::println);

//        System.out.println(project.getConfigurations().getByName("runtime").getFiles().size());
//        project.getConfigurations().getByName("runtime").getFiles().forEach(System.out::println);


        final TaskCollection<Jar> jarTasks = project.getTasks().withType(Jar.class);
        String pathToExecJar = configFiles.stream().map(file -> file.getPath()).filter(
                path -> path.contains("graph-builder")).findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find executable jar"));
        //dowloadedJarExecutable.getAbsolutePath(); ///jarExecutable.getPath();

        System.out.println("Found plugin file in: " + pathToExecJar);


        List<Jar> jarTasksList = new ArrayList<>(jarTasks);
        System.out.println("Found " + jarTasks.size() + " jar tasks");
        System.out.println("List created");

        for(Jar task : jarTasksList) {
            System.out.println("Task " + task.getName());

            final String path = task.getArchivePath().getAbsolutePath();

            final String taskName = task.getName() + "AnalyzerTask";

            final JavaExec javaExecTask = project.getTasks().create(taskName, JavaExec.class);

             System.out.println("Run task " + taskName + " to generate graph documents for file " + task.getArchiveName());

             javaExecTask.setMain("-jar");
             System.out.println("after set jar");
             //pathToExecJar = "/Users/camp/projects/corda-flows-doc-builder/graph-builder/build/libs/graph-builder-0.0.0-SNAPSHOT-all.jar"; //todo: remove
             javaExecTask.args(pathToExecJar, path, "./graphs");
             System.out.println("after set path");
             javaExecTask.dependsOn(task);
             System.out.println("after set dependOn");

//            final RunnerTask runnerTask = project.getTasks().create(taskName, RunnerTask.class);
//            runnerTask.setPathToJar(path);
        }
    }

}