package com.github.lucacampanella.callgraphflows.staticanalyzer;

import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.net.URL;

class JarAnalyzerTest extends AnalyzerWithModelTest {

    @BeforeAll
    static void setUp() {
        final URL testURL = JarAnalyzerTest.class.getClassLoader().getResource("test.txt");
        System.out.println(testURL);
        System.out.println(new File(testURL.getFile()).toString() + ": " + new File(testURL.getFile()).exists());
        final URL jarURL = JarAnalyzerTest.class.getClassLoader().getResource("JarAnalyzerTestJar.jar");
        System.out.println(jarURL);
        analyzerWithModel = new JarAnalyzer(jarURL.getPath());
    }
}