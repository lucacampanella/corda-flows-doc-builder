package com.github.lucacampanella.callgraphflows.staticanalyzer;

import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

class JarAnalyzerTest extends AnalyzerWithModelTest {

    private static Logger LOGGER = LoggerFactory.getLogger(JarAnalyzerTest.class);

    @BeforeAll
    static void setUp() {
        final URL testURL = JarAnalyzerTest.class.getClassLoader().getResource("test.txt");
        LOGGER.info("{}", testURL);
        LOGGER.info(new File(testURL.getFile()).toString() + ": " + new File(testURL.getFile()).exists());
        final URL jarURL = JarAnalyzerTest.class.getClassLoader().getResource("JarAnalyzerTestJar.jar");
        LOGGER.info("{}", jarURL);
        analyzerWithModel = new JarAnalyzer(jarURL.getPath());
    }
}