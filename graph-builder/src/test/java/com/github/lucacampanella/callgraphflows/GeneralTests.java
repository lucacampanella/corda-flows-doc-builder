package com.github.lucacampanella.callgraphflows;

import com.github.lucacampanella.TestUtils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.SourceClassAnalyzer;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzerUtilsTest;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.*;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.DoubleExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.ExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.InitiatorBaseFlow;
import net.corda.core.flows.StartableByRPC;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static com.github.lucacampanella.TestUtils.fromClassSrcToPath;

public class GeneralTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticAnalyzerUtilsTest.class);

    @BeforeAll
    static void setUp() {
        Paths.get(System.getProperty("user.dir"), DrawerUtil.DEFAULT_OUT_DIR).toFile().mkdirs();
    }

    @Test
    public void testNoCallMethodClass() throws IOException {
        testAnalyzeByRPCWithClass(NoCallMethodClass.class);
    }

    @Test
    public void testWhileForEach() throws IOException {
        testAnalyzeByRPCWithClass(WhileForEachTestFlow.class);
    }

    @Test
    public void testDoWhile() throws IOException {
        testAnalyzeByRPCWithClass(DoWhileTestFlow.class);
    }

    @Test
    public void testNestedIfs() throws IOException {
        testAnalyzeByRPCWithClass(NestedIfsTestFlow.class);
    }

    @Test
    public void testFor() throws IOException {
        testAnalyzeByRPCWithClass(ForTestFlow.class);
    }

    @Test
    public void testExtendingSuperclass() throws IOException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(fromClassSrcToPath(InitiatorBaseFlow.class),
                fromClassSrcToPath(ExtendingSuperclassTestFlow.class),
                fromClassSrcToPath(DoubleExtendingSuperclassTestFlow.class));

        testRPCFromAnalyzer(analyzer); //todo: think about how to get the methods of statically dispatche methods
    }


    @Test
    public void subFlowAnalysisTest() throws IOException {
        testAnalyzeByRPCWithClass(SubFlowAnalysisTestFlow.class);
    }

    @Test
    public void graphForDocsFlow() throws IOException {
        testAnalyzeByRPCWithClass(GraphForDocsFlow.class);
    }

    @Test
    public void simpleFlowTest() throws IOException {
        testAnalyzeByRPCWithClass(SimpleFlowTestFlow.class);
    }

    @Test
    public void methodInvocationTest() throws IOException {
        testAnalyzeByRPCWithClass(MethodInvocationTestFlow.class);
    }

    @Test
    public void nestedMethodInvocationTest() throws IOException {
        testAnalyzeByRPCWithClass(NestedMethodInvocationsTestFlow.class);
    }

    @Test
    public void SubFlowInitializationTest() throws IOException {
        testAnalyzeByRPCWithClass(SubFlowInitializationTestFlow.class);
    }

    private void testAnalyzeByRPCWithClass(Class toBeAnalyzed) throws IOException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(TestUtils.fromClassSrcToPath(toBeAnalyzed));

        testRPCFromAnalyzer(analyzer);
    }

    private void testRPCFromAnalyzer(AnalyzerWithModel analyzer) throws IOException {
        final List<CtClass> Classes = analyzer.getClassesByAnnotation(StartableByRPC.class);
        for (CtClass clazz : Classes) {
            LOGGER.info("{}{}.svg", DrawerUtil.DEFAULT_OUT_DIR, clazz.getSimpleName());
            final File file = new File(DrawerUtil.DEFAULT_OUT_DIR + clazz.getSimpleName() + ".svg");
            LOGGER.info("{}", file.getAbsolutePath());
            LOGGER.info("{}", file.exists());
            DrawerUtil.drawFromClass(analyzer, clazz, DrawerUtil.DEFAULT_OUT_DIR);
        }
    }
}