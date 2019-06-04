package com.github.lucacampanella.plugin;

import com.github.lucacampanella.callgraphflows.Drawer;
import com.github.lucacampanella.callgraphflows.staticanalyzer.JarAnalyzer;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

import java.util.Set;

public class MyTask extends DefaultTask {

    @TaskAction
    public void myTask() {
        System.out.println("MyTask executed");
        final Set<Task> jarTasks = getProject().getTasksByName("jar", false);
        System.out.println(jarTasks);
        jarTasks.forEach(uncastedTask -> {
            Jar task = (Jar) uncastedTask;
            final String path = task.getArchivePath().getAbsolutePath();
            System.out.println("PATH TO JAR:" + path);
            System.out.println("Before");
            System.out.flush();

            JarAnalyzer analyzer = new JarAnalyzer(path/*, cordaJar.get().getAbsolutePath()*/);
            //JarAnalyzer analyzer = new JarAnalyzer("/Users/camp/Desktop/cardossier-core/cardossier-core-flows/build/libs/cardossier-core-flows-FAT-SNAPSHOT.jar");
            System.out.println("After");
            Drawer.drawAllStartableClasses(analyzer);
        });
    }
}