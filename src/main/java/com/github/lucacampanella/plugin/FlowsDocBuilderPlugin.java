package com.github.lucacampanella.plugin;


import com.github.lucacampanella.callgraphflows.staticanalyzer.JarAnalyzer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.jvm.tasks.Jar;

public class FlowsDocBuilderPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getTasks().withType(Jar.class).all((task) -> {
            task.doLast((abc) ->
                    {
                        final String path = task.getArchivePath().getAbsolutePath();
                        System.out.println(path);

                        JarAnalyzer analyzer = new JarAnalyzer(path);

                        analyzer.drawAllStartableClasses();
                    }
            );
        });

        //project.getTasks().create("myTask", MyTask.class);
    }

}