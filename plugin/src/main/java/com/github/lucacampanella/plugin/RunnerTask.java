package com.github.lucacampanella.plugin;

//import com.github.lucacampanella.callgraphflows.Drawer;
//import com.github.lucacampanella.callgraphflows.staticanalyzer.JarAnalyzer;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class RunnerTask extends DefaultTask {
//    String pathToJar = null;
//
//    @TaskAction
//    public void analyzeJar() {
//        System.out.println("Runner task");
//
//        System.out.println(getProject().getConfigurations().getByName("runtime").getFiles().size());
//        getProject().getConfigurations().getByName("runtime").getFiles().forEach(System.out::println);
//
//        System.out.println("Before task running");
//
//        JarAnalyzer analyzer = new JarAnalyzer(pathToJar);
//        try {
//            Drawer.drawAllStartableClasses(analyzer, "./build/graphs");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void setPathToJar(String pathToJar) {
//        this.pathToJar = pathToJar;
//    }
}
