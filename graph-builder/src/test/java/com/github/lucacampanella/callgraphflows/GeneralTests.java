package com.github.lucacampanella.callgraphflows;

import com.github.lucacampanella.TestUtils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.*;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.IfElse;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.MethodInvocation;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.StatementInterface;
import com.github.lucacampanella.callgraphflows.staticanalyzer.instructions.While;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.*;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.DoubleExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.ExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.InitiatorBaseFlow;
import net.corda.core.flows.InitiatingFlow;
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
    public void testSimplifiedNestedIfs() throws IOException {
        testAnalyzeByRPCWithClass(SimplifiedNestedIfsTestFlow.class);
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
    public void simpleFlowTest() throws AnalysisErrorException, IOException {
        final SourceClassAnalyzer analyzer = getSourceClassAnalyzerFromClasses(SimpleTestFlow.class);
        final AnalysisResult analysisResult = analyzer.analyzeFlowLogicClass(SimpleTestFlow.Initiator.class);
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
        assertThat(analysisResult.checkIfContainsValidProtocolAndSetupLinks()).isEqualTo(true);
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
    public void ContinueBreakTest() throws AnalysisErrorException, IOException {
        final AnalysisResult analysisResult = getSourceClassAnalyzerFromClasses(ContinueBreakTestFlow.class)
                .analyzeFlowLogicClass(ContinueBreakTestFlow.Initiator.class);
        assertThat(analysisResult.getStatements()).hasSize(5);
        While whileStatement = (While) analysisResult.getStatements().getStatements().get(1);
        assertThat(whileStatement.getBody().getStatements()).hasSize(3);
        IfElse ifElseInWhile = (IfElse)  whileStatement.getBody().getStatements().get(0);
        assertThat(ifElseInWhile.getBranchTrue()).hasSize(2);
        assertThat(analysisResult.getCounterpartyClassResult().getStatements()).hasSize(5);
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
    }

    @Test
    public void ContainerFlowTest() throws IOException, AnalysisErrorException {
        final AnalysisResult analysisResult = getSourceClassAnalyzerFromClasses(ContainerFlow.class,
                DoWhileTestFlow.class).analyzeFlowLogicClass(ContainerFlow.class);
        LOGGER.trace("{}", analysisResult.getStatements());
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
    }

    @Test
    public void InitiatingFlowInIfTestFlowTest() throws IOException, AnalysisErrorException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(fromClassSrcToPath(InitiatingFlowInIfTestFlow.class));
        final AnalysisResult analysisResult =
                analyzer.analyzeFlowLogicClass(analyzer.getClass(InitiatingFlowInIfTestFlow.Initiator.class));
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
        LOGGER.trace("{}", analysisResult.getStatements());
    }

    @Test
    public void InlinableFlowInIfTestFlowTest() throws IOException, AnalysisErrorException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(fromClassSrcToPath(InlinableFlowInIfTestFlow.class));
        final AnalysisResult analysisResult =
                analyzer.analyzeFlowLogicClass(analyzer.getClass(InlinableFlowInIfTestFlow.Initiator.class));
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
        LOGGER.trace("{}", analysisResult.getStatements());
    }

    @Test
    public void SendMultipleTransactionsFlowTest() throws IOException, AnalysisErrorException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(fromClassSrcToPath(GraphForDocsFlow.class));
        final AnalysisResult analysisResult =
                analyzer.analyzeFlowLogicClass(analyzer.getClass(GraphForDocsFlow.SendMultipleTransactionsFlow.class));
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
        LOGGER.trace("{}", analysisResult.getStatements());
    }

    @Test
    public void ContinueTestFlowTest() throws IOException, AnalysisErrorException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(fromClassSrcToPath(ContinueTestFlow.class));
        final AnalysisResult analysisResult =
                analyzer.analyzeFlowLogicClass(analyzer.getClass(ContinueTestFlow.Initiator.class));
        assertThat(analysisResult.checkIfContainsValidProtocolAndSetupLinks()).isEqualTo(true);
        assertThat(CombinationsHolder.fromBranch(analysisResult.getStatements()).getAllCombinations()).hasSize(7);
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
        LOGGER.trace("{}", analysisResult.getStatements());
    }

    @Test
    public void BreakTestFlowTest() throws IOException, AnalysisErrorException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(fromClassSrcToPath(BreakTestFlow.class));
        final AnalysisResult analysisResult =
                analyzer.analyzeFlowLogicClass(analyzer.getClass(BreakTestFlow.Initiator.class));
        assertThat(analysisResult.checkIfContainsValidProtocolAndSetupLinks()).isEqualTo(true);
        assertThat(CombinationsHolder.fromBranch(analysisResult.getStatements()).getAllCombinations()).hasSize(5);
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
        LOGGER.trace("{}", analysisResult.getStatements());
    }

    @Test
    public void ThrowTestFlowTest() throws IOException, AnalysisErrorException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(fromClassSrcToPath(ThrowTestFlow.class));
        final AnalysisResult analysisResult =
                analyzer.analyzeFlowLogicClass(analyzer.getClass(ThrowTestFlow.Initiator.class));
        assertThat(analysisResult.checkIfContainsValidProtocolAndSetupLinks()).isEqualTo(true);
        assertThat(CombinationsHolder.fromBranch(analysisResult.getStatements()).getAllCombinations()).hasSize(2);
        assertThat(analysisResult.getStatements()).hasSize(7);
        assertThat(analysisResult.getStatements().getStatements().stream().map(StatementInterface::toString)
        .filter(str -> str.contains("methodWithAThrowAndNORelevantStuff")).findAny()).isEmpty();
        assertThat(analysisResult.getStatements().getStatements().stream().map(StatementInterface::toString)
                .filter(str -> str.contains("methodWithAThrowAndRelevantStuff")).findAny()).isPresent();
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
        LOGGER.trace("{}", analysisResult.getStatements());
    }

    @Test
    public void ReturnTestFlowTest() throws IOException, AnalysisErrorException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(fromClassSrcToPath(ReturnTestFlow.class));
        final AnalysisResult analysisResult =
                analyzer.analyzeFlowLogicClass(analyzer.getClass(ReturnTestFlow.Initiator.class));
        assertThat(analysisResult.checkIfContainsValidProtocolAndSetupLinks()).isEqualTo(true);
        assertThat(CombinationsHolder.fromBranch(analysisResult.getStatements()).getAllCombinations()).hasSize(2);
        assertThat(analysisResult.getStatements()).hasSize(7);
        assertThat(analysisResult.getStatements().getStatements().stream().map(StatementInterface::toString)
                .filter(str -> str.contains("methodWithAReturnAndNORelevantStuff")).findAny()).isEmpty();
        assertThat(analysisResult.getStatements().getStatements().stream().map(StatementInterface::toString)
                .filter(str -> str.contains("methodWithAReturnAndRelevantStuff")).findAny()).isPresent();
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
        LOGGER.trace("{}", analysisResult.getStatements());
    }

    @Test
    public void ReturnThrowBreakContinueTestFlowTest() throws IOException, AnalysisErrorException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(fromClassSrcToPath(ReturnThrowBreakContinueTestFlow.class));
        final AnalysisResult analysisResult =
                analyzer.analyzeFlowLogicClass(analyzer.getClass(ReturnThrowBreakContinueTestFlow.Initiator.class));
        assertThat(analysisResult.checkIfContainsValidProtocolAndSetupLinks()).isEqualTo(true);
        assertThat(CombinationsHolder.fromBranch(analysisResult.getStatements()).getAllCombinations()).hasSize(114);
        assertThat(analysisResult.getStatements()).hasSize(4);
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
        LOGGER.trace("{}", analysisResult.getStatements());
    }

    @Test
    public void InitiatedByInnerClassAcceptorTest() throws IOException, AnalysisErrorException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(fromClassSrcToPath(InitiatorBaseFlow.class),
                fromClassSrcToPath(ExtendingSuperclassTestFlow.class),
                fromClassSrcToPath(DoubleExtendingSuperclassTestFlow.class));
        final AnalysisResult analysisResult =
                analyzer.analyzeFlowLogicClass(analyzer.getClass(DoubleExtendingSuperclassTestFlow.Initiator.class));
        StaticAnalyzerUtils.getAllWronglyDoubleAnnotatedClasses(analyzer.getClassesByAnnotation(InitiatingFlow.class));
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
        LOGGER.trace("{}", analysisResult.getStatements());
        assertThat(analysisResult.getStatements().getStatements().get(1)).isInstanceOf(MethodInvocation.class);
        assertThat(((MethodInvocation) analysisResult.getStatements().getStatements().get(1)).getBody()).hasSize(3);
    }

    @Test
    public void dynamicallyDispatchedMethod() throws AnalysisErrorException, IOException {
        final SourceClassAnalyzer sourceClassAnalyzerFromClasses = getSourceClassAnalyzerFromClasses(InitiatorBaseFlow.class, ExtendingSuperclassTestFlow.class,
                DoubleExtendingSuperclassTestFlow.class);
        final AnalysisResult analysisResult =
                sourceClassAnalyzerFromClasses.analyzeFlowLogicClass(DoubleExtendingSuperclassTestFlow.Initiator.class);
        LOGGER.info("{}", analysisResult.getStatements());
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
        assertThat(analysisResult.getGraphicRepresentationNoTitles().getMainSubFlow().toString())
                .contains("initiateFlow");
        assertThat(analysisResult.getGraphicRepresentationNoTitles().toString())
                .contains("initiateFlow");
   }

    @Test
    public void InitiateFlowInIfTestFlowTest() throws AnalysisErrorException, IOException {
        final AnalysisResult analysisResult =
                getSourceClassAnalyzerFromClasses(InitiateFlowInIfTestFlow.class)
                        .analyzeFlowLogicClass(InitiateFlowInIfTestFlow.Initiator.class);
        DrawerUtil.drawFromAnalysis(analysisResult, DrawerUtil.DEFAULT_OUT_DIR);
        assertThat(analysisResult.checkIfContainsValidProtocolAndSetupLinks()).isEqualTo(true);
    }

    private void testAnalyzeByRPCWithClass(Class toBeAnalyzed) throws IOException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(TestUtils.fromClassSrcToPath(toBeAnalyzed));

        testRPCFromAnalyzer(analyzer);
    }

    private void testRPCFromAnalyzer(AnalyzerWithModel analyzer) throws IOException {
        final List<CtClass> Classes = analyzer.getClassesByAnnotation(StartableByRPC.class);
        Paths.get(DrawerUtil.DEFAULT_OUT_DIR, "images").toFile().mkdirs();
        for (CtClass clazz : Classes) {
            DrawerUtil.drawFromClass(analyzer, clazz, DrawerUtil.DEFAULT_OUT_DIR);
        }
    }

    public static SourceClassAnalyzer getSourceClassAnalyzerFromClasses(Class... toBeAnalyzed) {
        return new SourceClassAnalyzer(Arrays.stream(toBeAnalyzed)
                .map(TestUtils::fromClassSrcToPath).collect(Collectors.toList()));
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
