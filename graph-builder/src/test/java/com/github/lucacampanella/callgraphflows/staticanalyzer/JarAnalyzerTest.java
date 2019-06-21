package com.github.lucacampanella.callgraphflows.staticanalyzer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

class JarAnalyzerTest extends AnalyzerWithModelTest {

    private static Logger LOGGER = LoggerFactory.getLogger(JarAnalyzerTest.class);
    private static String TEST_JAR_NAME = "JarAnalyzerTestJar.jar";

    @BeforeAll
    static void setUp() {
        final URL jarURL = JarAnalyzerTest.class.getClassLoader().getResource(TEST_JAR_NAME);
        LOGGER.trace("{}", jarURL);
        analyzerWithModel = new JarAnalyzer(jarURL.getPath());
    }

    @Test
    void getAnalysisName() {
        assertThat(analyzerWithModel.getAnalysisName()).isEqualTo(TEST_JAR_NAME);
    }
}