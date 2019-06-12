package com.github.lucacampanella.plugin;

import com.github.lucacampanella.TestUtils;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlowsDocBuilderPluginTest {

    private static Logger LOGGER = LoggerFactory.getLogger(FlowsDocBuilderPluginTest.class);

    private static final Path upperDir = Paths.get(System.getProperty("user.dir")).getParent();

    private static final File sampleProjectDirectory = Paths.get(upperDir.toString(), "simple-flow-project").toFile();
    private static final File outputDir = Paths.get(sampleProjectDirectory.toString(),
            "build", "reports", "flowsdocbuilder").toFile();

    @BeforeEach
    void setUp() throws IOException {
//        tmpDir = Files.createTempDirectory("testTempDir").toFile();
//        LOGGER.info(tmpDir);
//
//        FileUtils.copyDirectory(sampleProjectDirectory, tmpDir);

        FileUtils.deleteDirectory(outputDir);

        final BuildResult buildResult = GradleRunner.create().withProjectDir(sampleProjectDirectory)
                .withPluginClasspath().withArguments("JarAnalyzerTask").build();
        //todo: copy only the important files, not caches and so on, this may also allow to fire up different gradle
        //versions

        LOGGER.info("{}", buildResult.getOutput());
        LOGGER.info("{}", buildResult);
    }

    @Test
    void hasOutput() {
        final File[] outputFiles = outputDir.listFiles();
        assertThat(outputFiles).isNotEmpty();
    }

    @Test
    void outputSVGIsCorrect() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {

        //we check directly the XML file
        final List<String> nodeContents = TestUtils.parseXMLFile(outputDir.toString()
                + "/com.github.lucacampanella.testclasses.SimpleFlowTest$Initiator.svg");

        assertThat(nodeContents).hasSize(4);
        assertThat(nodeContents).contains("[52] initiateFlow(session)",
                "[53] ==><== sendAndReceive(<== <<String>>, ==> <<Boolean>>)==><==",
                "[32] <== receive(<<Boolean>>) <==", "[33] ==> send(<<String>>) ==>");

    }

    @Test
    void outputAsciiDocIsCorrect() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {

        //we check directly the XML file
        final File outFile = new File(outputDir.toString()
                + "/com.github.lucacampanella.testclasses.SimpleFlowTest$Initiator.adoc");

        assertThat(outFile).exists();
    }
}