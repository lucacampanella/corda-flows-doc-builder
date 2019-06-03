package com.github.lucacampanella.callgraphflows.testUtils;

import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.DoubleExtendingSuperclassTestFlow;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtils {
    public static String fromClassSrcToPath(Class klass) {
        String path = klass.getCanonicalName();
        path = path.replace(".", System.getProperty("file.separator"));
        path = path + ".java";
        Path pathObj = Paths.get(System.getProperty("user.dir"), "src", "test", "java", path);
        return pathObj.toString();
    }

    public static void main(String []args) {
        System.out.println(fromClassSrcToPath(DoubleExtendingSuperclassTestFlow.class));
    }
}
