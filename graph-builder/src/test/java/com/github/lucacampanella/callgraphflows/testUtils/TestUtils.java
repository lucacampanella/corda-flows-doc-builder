package com.github.lucacampanella.callgraphflows.testUtils;

public class TestUtils {
    public static String fromClassSrcToPath(Class klass) {
        return "./src/test/java/com/github/lucacampanella/callgraphflows/staticanalyzer/testclasses/"
                + klass.getSimpleName() + ".java";
    }
}
