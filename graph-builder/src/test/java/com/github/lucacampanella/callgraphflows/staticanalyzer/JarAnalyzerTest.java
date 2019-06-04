package com.github.lucacampanella.callgraphflows.staticanalyzer;

import org.junit.jupiter.api.BeforeAll;

import java.net.URL;

class JarAnalyzerTest extends AnalyzerWithModelTest {

    @BeforeAll
    static void setUp() {
        final URL jarURL = JarAnalyzerTest.class.getClassLoader().getResource("JarAnalyzerTestJar-1.0-SNAPSHOT.jar");
        analyzerWithModel = new JarAnalyzer(jarURL.getPath());
    }
}