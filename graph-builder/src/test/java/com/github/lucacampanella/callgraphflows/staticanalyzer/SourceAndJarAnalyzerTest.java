package com.github.lucacampanella.callgraphflows.staticanalyzer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class SourceAndJarAnalyzerTest extends AnalyzerWithModelTest {

    private static Logger LOGGER = LoggerFactory.getLogger(SourceAndJarAnalyzerTest.class);
    private static String TEST_JAR_NAME = "JarAnalyzerTestJar.jar";
    private static String folderPath;

    @BeforeAll
    static void setUp() throws IOException {
        final URL jarURL = SourceAndJarAnalyzerTest.class.getClassLoader().getResource(TEST_JAR_NAME);
        LOGGER.trace("{}", jarURL);
        folderPath = Paths.get(Paths.get(jarURL.getPath()).getParent().toString(), "subclassestests").toString();
        LOGGER.trace("{}", folderPath);
        analyzerWithModel = new SourceAndJarAnalyzer(Arrays.asList(folderPath),
                Arrays.asList(jarURL.getPath()),
                DecompilerEnum.CFR, false);
    }

    @Test
    void getAnalysisName() {
        assertThat(analyzerWithModel.getAnalysisName()).isEqualTo("subclassestests");
    }

    @Test
    void findQualifiedName() throws IOException {
        final String qualifiedName = SourceAndJarAnalyzer.findQualifiedName(
                Paths.get(folderPath, "InitiatorBaseFlow.java").toFile());
        assertThat(qualifiedName).isEqualTo(
                "com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.InitiatorBaseFlow");
    }
}