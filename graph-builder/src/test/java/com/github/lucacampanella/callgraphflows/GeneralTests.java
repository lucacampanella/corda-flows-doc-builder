package com.github.lucacampanella.callgraphflows;

import com.github.lucacampanella.TestUtils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.*;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.IfElse;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.While;
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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.lucacampanella.TestUtils.fromClassSrcToPath;
import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void ContinueBreakTest() throws IOException, AnalysisErrorException {
        final AnalysisResult analysisResult = drawAndReturnAnalysis(ContinueBreakTest.class);
        assertThat(analysisResult.getStatements()).hasSize(4);
        While whileStatement = (While) analysisResult.getStatements().getStatements().get(1);
        assertThat(whileStatement.getBranchTrue().getStatements()).hasSize(3);
        IfElse ifElseInWhile = (IfElse)  whileStatement.getBranchTrue().getStatements().get(0);
        assertThat(ifElseInWhile.getBranchTrue()).hasSize(2);
        assertThat(analysisResult.getCounterpartyClassResult().getStatements()).hasSize(4);
    }

    @Test
    public void ContainerFlowTest() throws IOException, AnalysisErrorException {
        final AnalysisResult analysisResult = drawAndReturnAnalysis(ContainerFlow.class,
                DoWhileTestFlow.class);
        LOGGER.trace("{}", analysisResult.getStatements());
    }

    @Test
    public void InitiatedByInnerClassAcceptorTest() throws IOException, AnalysisErrorException {
        final AnalysisResult analysisResult = drawAndReturnAnalysis(InitiatedByInnerClassAcceptorTestFlow.class,
                SimpleFlowTestFlow.class);
        LOGGER.trace("{}", analysisResult.getStatements());
    }

    public AnalysisResult drawAndReturnAnalysis(Class... toBeAnalyzed) throws AnalysisErrorException, IOException {
        final List<String> pathToClasses = Arrays.stream(toBeAnalyzed).map(TestUtils::fromClassSrcToPath).collect(Collectors.toList());
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(pathToClasses);
        final CtClass klass = analyzer.getClassesByAnnotation(StartableByRPC.class).stream().findFirst().get();
        final AnalysisResult analysisResult = analyzer.analyzeFlowLogicClass(klass);
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
        return analysisResult;
    }

    private void testAnalyzeByRPCWithClass(Class toBeAnalyzed) throws IOException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(TestUtils.fromClassSrcToPath(toBeAnalyzed));

        testRPCFromAnalyzer(analyzer);
    }

    private void testRPCFromAnalyzer(AnalyzerWithModel analyzer) throws IOException {
        final List<CtClass> Classes = analyzer.getClassesByAnnotation(StartableByRPC.class);
        for (CtClass clazz : Classes) {
            DrawerUtil.drawFromClass(analyzer, clazz, DrawerUtil.DEFAULT_OUT_DIR);
        }
    }

    @Test
    public void KotlinTestFlowDecompiledTest() throws IOException {
        testAnalyzeByRPCWithClass(KotlinTestFlowDecompiled.class);
    }

    @Test
    public void kotlinTest() throws IOException, AnalysisErrorException {
        String pathToKotlinJar = getClass().getClassLoader().getResource("KotlinTestJar.jar").getPath();

        final JarAnalyzer analyzer = new JarAnalyzer(pathToKotlinJar);
        final List<CtClass> Classes = analyzer.getClassesByAnnotation(StartableByRPC.class);
        for (CtClass clazz : Classes) {
            final AnalysisResult analysisResult = analyzer.analyzeFlowLogicClass(clazz);
            System.out.println(analysisResult.getStatements());
            assertThat(analysisResult.getStatements()).hasSize(4);
            assertThat(analysisResult.getCounterpartyClassResult().getStatements()).hasSize(3);
        }
        DrawerUtil.drawAllStartableClasses(analyzer);
    }
}
