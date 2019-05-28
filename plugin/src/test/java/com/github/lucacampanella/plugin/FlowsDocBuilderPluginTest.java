package com.github.lucacampanella.plugin;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class FlowsDocBuilderPluginTest {
    private static final File sampleProjectDirectory = new File("../../simple-flow-project");
    File tmpDir;

    @BeforeEach
    void setUp() throws IOException {
        tmpDir = Files.createTempDirectory("testTempDir").toFile();
        System.out.println(tmpDir);

        FileUtils.copyDirectory(sampleProjectDirectory, tmpDir);

        final BuildResult buildResult = GradleRunner.create().withProjectDir(tmpDir)
                .withPluginClasspath().withArguments("build").build();

        System.out.println(buildResult.getOutput());

        System.out.println(buildResult);
    }

    @Test
    void emptyTest() {
        System.out.println("This is an empty test");
    }

//    @AfterEach
//    void tearDown() throws IOException {
//        FileUtils.deleteDirectory(tmpDir);
//    }
}