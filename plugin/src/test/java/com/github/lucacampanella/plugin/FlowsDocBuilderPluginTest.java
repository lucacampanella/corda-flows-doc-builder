package com.github.lucacampanella.plugin;

import com.github.lucacampanella.TestUtils;
import org.apache.commons.io.FileUtils;
import org.gradle.internal.impldep.org.testng.annotations.Test;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

@Test
class FlowsDocBuilderPluginTest {

    private static Logger LOGGER = LoggerFactory.getLogger(FlowsDocBuilderPluginTest.class);

    private static final Path upperDir = Paths.get(System.getProperty("user.dir")).getParent();

    private static final File sampleProjectDirectory = Paths.get(upperDir.toString(), "simple-flow-project").toFile();

    private File tmpOutputdir = null;
    private File tmpDir = null;

    private static final String[] filesToCopy = {"settings.gradle", "gradlew.bat", "gradlew", "build.gradle"};
    private static final String[] dirsToCopy = {"gradle", "src"};

    @BeforeEach
    void setUp() throws IOException {
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



        //todo: copy only the important files, not caches and so on, this may also allow to fire up different gradle
        //versions
    }

    void hasOutput() {
        final File[] outputFiles = tmpOutputdir.listFiles();
        assertThat(outputFiles).isNotEmpty();
    }

    void outputSVGIsCorrect() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {

        //we check directly the XML file
        final List<String> nodeContents = TestUtils.parseXMLFile(Paths.get(tmpOutputdir.getPath(), "images",
                "com.github.lucacampanella.testclasses.SimpleFlowTest$Initiator.svg").toString());

        assertThat(nodeContents).hasSize(9);
        assertThat(nodeContents).contains(
                "SimpleFlowTest$Initiator",
                "@StartableByRPC",
                "@InitiatingFlow",
                "session = initiateFlow(otherParty)",
                "sendAndReceive(String, Boolean)",
                "SimpleFlowTest$Acceptor",
                "@InitiatedBy(SimpleFlowTest$Initiator)",
                "receive(Boolean)",
                "send(String)");

    }

    void outputAsciiDocIsCorrect() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {

        //we check directly the XML file
        final File outFile = new File(tmpOutputdir.toString()
                + "/com.github.lucacampanella.testclasses.SimpleFlowTest$Initiator.adoc");

        assertThat(outFile).exists();
    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.deleteDirectory(tmpDir);
    }


    @ParameterizedTest
    @ValueSource(strings = {"4.10", "4.10.1", "5.1", "5.4.1"})
    void gradlePassingVersionsTest(String version) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        GradleRunner.create().withProjectDir(tmpDir)
                .withPluginClasspath().withArguments("JarAnalyzerTask").withGradleVersion(version).build();
        hasOutput();
        outputSVGIsCorrect();
        outputSVGIsCorrect();
    }

    @ParameterizedTest
    @ValueSource(strings = {"4.8", "4.4.1"})
    void gradleFailingVersionsTest(String version) {
        final BuildResult buildResult = GradleRunner.create().withProjectDir(tmpDir)
                .withPluginClasspath().withArguments("JarAnalyzerTask").withGradleVersion(version).buildAndFail();
        assertThat(buildResult.getOutput()).contains("Flows doc builder plugin doesn't support version");
    }
}