package com.github.lucacampanella.plugin;

import com.github.lucacampanella.TestUtils;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlowsDocBuilderPluginTest {
    private static final File sampleProjectDirectory = new File("../../simple-flow-project");
    static final File outputDir = new File("../../simple-flow-project/graphs");

    @BeforeEach
    void setUp() throws IOException {
//        tmpDir = Files.createTempDirectory("testTempDir").toFile();
//        System.out.println(tmpDir);
//
//        FileUtils.copyDirectory(sampleProjectDirectory, tmpDir);

        //System.out.println(System.getProperty("user.dir"));
        FileUtils.deleteDirectory(outputDir);

        final BuildResult buildResult = GradleRunner.create().withProjectDir(sampleProjectDirectory)
                .withPluginClasspath().withArguments("JarAnalyzerTask").build();

        System.out.println(buildResult.getOutput());

        System.out.println(buildResult);
    }

    @Test
    void hasOutput() {
        final File[] outputFiles = outputDir.listFiles();
        assertThat(outputFiles).isNotEmpty();
    }

    @Test
    void outputIsCorrect() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        //we check directly the XML file
        final List<String> nodeContents = TestUtils.parseXMLFile(outputDir.toString()
                + "/com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.SimpleFlowTest$Initiator.svg");

        assertThat(nodeContents).hasSize(4);
        assertThat(nodeContents).contains("[52] initiateFlow(session)",
                "[53] ▶◀ sendAndReceive(◀ <<String>>, ▶ <<Boolean>>)▶◀",
                "[32] ◀ receive(<<Boolean>>) ◀", "[33] ▶ send(<<String>>) ▶");

    }
}