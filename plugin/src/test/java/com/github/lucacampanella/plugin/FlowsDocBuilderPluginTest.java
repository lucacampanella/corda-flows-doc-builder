package com.github.lucacampanella.plugin;

import com.github.lucacampanella.TestUtils;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlowsDocBuilderPluginTest {

    private static Logger LOGGER = LoggerFactory.getLogger(FlowsDocBuilderPluginTest.class);

    private static final Path upperDir = Paths.get(System.getProperty("user.dir")).getParent();

    private static final File sampleProjectDirectory = Paths.get(upperDir.toString(), "simple-flow-project").toFile();
    private static final File outputDir = Paths.get(sampleProjectDirectory.toString(),
            "build", "reports", "differentdir", "flowsdocbuilder").toFile();
    private static File tmpOutputdir = null;
    private static File tmpDir = null;

    private static final String[] filesToCopy = {"settings.gradle", "gradlew.bat", "gradlew", "build.gradle"};
    private static final String[] dirsToCopy = {"gradle", "src"};

    @BeforeAll
    static void setUp() throws IOException {
        tmpDir = Files.createTempDirectory("testTempDir").toFile();

        tmpOutputdir = Paths.get(tmpDir.toString(),
                "build", "reports", "differentdir", "flowsdocbuilder").toFile();

        LOGGER.trace("{}", tmpDir);

        for(String dirName : dirsToCopy) {
            FileUtils.copyDirectory(Paths.get(sampleProjectDirectory.getPath(), dirName).toFile(),
                    Paths.get(tmpDir.getPath(), dirName).toFile());
        }

        for(String fileName : filesToCopy) {
            FileUtils.copyFile(Paths.get(sampleProjectDirectory.getPath(), fileName).toFile(),
                    Paths.get(tmpDir.getPath(), fileName).toFile());
        }

        //copy executable jar
        final File[] candidateJarFiles = Paths.get("../graph-builder/build/libs").toFile()
                .listFiles((dir, name) -> name.startsWith("graph-builder") && name.endsWith("-all.jar"));
        File jarExecFile = null;
        if(candidateJarFiles != null) {
            jarExecFile = Arrays.stream(candidateJarFiles)
                .findAny().orElseThrow(() -> new RuntimeException("Could not find local executable jar"));
        } else { throw new RuntimeException("Could not find local executable jar"); }

        FileUtils.copyFile(jarExecFile,
                Paths.get(tmpDir.getPath(), jarExecFile.getName()).toFile());

        //FileUtils.deleteDirectory(outputDir);

        final BuildResult buildResult = GradleRunner.create().withProjectDir(tmpDir)
                .withPluginClasspath().withArguments("JarAnalyzerTask")/*.withGradleVersion("4.10.1")*/.build();

        //todo: copy only the important files, not caches and so on, this may also allow to fire up different gradle
        //versions

        LOGGER.trace("{}", buildResult.getOutput());
        LOGGER.trace("{}", buildResult);
    }

    @Test
    void hasOutput() {
        final File[] outputFiles = tmpOutputdir.listFiles();
        assertThat(outputFiles).isNotEmpty();
    }

    @Test
    void outputSVGIsCorrect() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {

        //we check directly the XML file
        final List<String> nodeContents = TestUtils.parseXMLFile(tmpOutputdir.toString()
                + "/com.github.lucacampanella.testclasses.SimpleFlowTest$Initiator.svg");

        assertThat(nodeContents).hasSize(4);
        assertThat(nodeContents).contains("[49] initiateFlow(session)",
                "[50] ==><== sendAndReceive(<== <<String>>, ==> <<Boolean>>)==><==",
                "[30] <== receive(<<Boolean>>) <==",
                "[31] ==> send(<<String>>) ==>");

    }

    @Test
    void outputAsciiDocIsCorrect() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {

        //we check directly the XML file
        final File outFile = new File(tmpOutputdir.toString()
                + "/com.github.lucacampanella.testclasses.SimpleFlowTest$Initiator.adoc");

        assertThat(outFile).exists();
    }

    @AfterAll
    static void tearDown() throws IOException {
        FileUtils.deleteDirectory(tmpDir);

    }
}