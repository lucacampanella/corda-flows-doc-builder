package com.github.lucacampanella.callgraphflows;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void main() throws IOException {
        final String jarPath = getClass().getClassLoader().getResource("KotlinTestJar.jar").getPath();
        Main.main(new String[]{jarPath, "-o", "build/graphs", "-d", "fernflower", "-l", "--no-box-subflows",
        "--draw-return", "--draw-statements-with-relevant-methods"});
    }
}