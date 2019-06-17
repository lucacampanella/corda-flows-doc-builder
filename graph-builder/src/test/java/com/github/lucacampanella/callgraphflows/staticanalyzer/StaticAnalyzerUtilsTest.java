package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.TestUtils;
import com.github.lucacampanella.callgraphflows.DrawerUtil;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.*;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.DoubleExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.ExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.InitiatorBaseFlow;
import net.corda.core.flows.StartableByRPC;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.lucacampanella.TestUtils.fromClassSrcToPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StaticAnalyzerUtilsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticAnalyzerUtilsTest.class);

    @BeforeAll
    static void setUp() {
        final boolean mkdirs = Paths.get(System.getProperty("user.dir"), DrawerUtil.DEFAULT_OUT_DIR).toFile().mkdirs();
    }

    @Test
    public void findCallMethod() throws FileNotFoundException {
        final CtClass ctClass = fromClassToCtClass(ExtendingSuperclassTestFlow.class);
        assertTrue(StaticAnalyzerUtils.findCallMethod(ctClass) != null);
    }

//    @Test
//    public void testWhileForEach() throws FileNotFoundException {
//        testClass(WhileForEachTest.class);
//    }
//
//    @Test
//    public void testDoWhile() throws FileNotFoundException {
//        testClass(DoWhileTest.class);
//    }
//
//    @Test
//    public void testNestedIfs() throws FileNotFoundException {
//        testClass(NestedIfsTest.class);
//    }
//
//    @Test
//    public void testFor() throws FileNotFoundException {
//        testClass(ForTest.class);
//    }
//
//    @Test
//    public void testExtendingSuperclass() throws FileNotFoundException {
//        testClass(ExtendingSuperclassTest.class); //todo: we need to get the methods and analyze their body too
//    }
//
//
//    @Test
//    public void subFlowAnalysisTest() throws FileNotFoundException {
//        testClass(SubFlowAnalysisTest.class);
//    }
//
//    @Test
//    public void cordaSubflowsNotMatchingTest() throws FileNotFoundException {
//        testClass(CordaSubflowsNotMatchingTest.class, false);
//    }
//
//    @Test
//    public void simpleFlowTest() throws FileNotFoundException {
//        testClass(SimpleFlowTest.class);
//    }
//
//    @Test
//    public void methodInvocationTest() throws FileNotFoundException {
//        testClass(MethodInvocationTest.class);
//    }
//
//    @Test
//    public void ifFailingTest() throws FileNotFoundException {
//        testClass(IfFailingTest.class, false);
//    }

    @Test
    public void testWhileForEachStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(WhileForEachTestFlow.class);
    }

    @Test
    public void testDoWhileStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(DoWhileTestFlow.class);
    }

    @Test
    public void testNestedIfsStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(NestedIfsTestFlow.class);
    }

    @Test
    public void testForStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(ForTestFlow.class);
    }

    @Test
    public void testExtendingSuperclassStartable() throws IOException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(fromClassSrcToPath(InitiatorBaseFlow.class),
                fromClassSrcToPath(ExtendingSuperclassTestFlow.class),
                fromClassSrcToPath(DoubleExtendingSuperclassTestFlow.class));

        testStartableRPCFromAnalyzer(analyzer); //todo: thin about how to get the methods of statically dispatche methods
    }


    @Test
    public void subFlowAnalysisTestStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(SubFlowAnalysisTestFlow.class);
    }

    @Test
    public void graphForDocsFlowStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(GraphForDocsFlow.class);
    }

    @Test
    public void simpleFlowTestStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(SimpleFlowTestFlow.class);
    }

    @Test
    public void methodInvocationTestStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(MethodInvocationTestFlow.class);
    }

    @Test
    public void nestedMethodInvocationTestStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(NestedMethodInvocationsTestFlow.class);
    }

    @Test
    public void SubFlowInitializationTestStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(SubFlowInitializationTestFlow.class);
    }

    private void testAnalyzeStartableByRPCWithClass(Class toBeAnalyzed) throws IOException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(TestUtils.fromClassSrcToPath(toBeAnalyzed));

        testStartableRPCFromAnalyzer(analyzer);
    }

    private void testStartableRPCFromAnalyzer(AnalyzerWithModel analyzer) throws IOException {
        final List<CtClass> startableClasses = analyzer.getClassesByAnnotation(StartableByRPC.class);
        for (CtClass clazz : startableClasses) {
            LOGGER.info("{}{}.svg", DrawerUtil.DEFAULT_OUT_DIR, clazz.getSimpleName());
            final File file = new File(DrawerUtil.DEFAULT_OUT_DIR + clazz.getSimpleName() + ".svg");
            LOGGER.info("{}", file.getAbsolutePath());
            LOGGER.info("{}", file.exists());
            DrawerUtil.drawFromClass(analyzer, clazz, DrawerUtil.DEFAULT_OUT_DIR);
        }
    }

//    @Test
//    public void subFlowInitiationTest() throws FileNotFoundException {
//        CtClass<?> toBeTestedClass = getFactory(Arrays.asList(SubFlowInitializationTest.class)).Class()
//                .get(SubFlowInitializationTest.class);
//        final Map<CtClass, CtClass> initiatedClassToInitiatingMap =
//                SourceClassAnalyzer.getInitiatedClassToInitiatingMap(toBeTestedClass);
//        LOGGER.trace(initiatedClassToInitiatingMap);
//
//        initiatedClassToInitiatingMap.forEach((initiatedClass, initiatingClass) ->
//                StaticAnalyzer.checkTwoClassesAndBuildGraphs(
//                        initiatingClass, initiatedClass, "example/test/")
//        );
//    }
//
//    private void testClass(Class klass, boolean expectedRes) throws FileNotFoundException {
//        CtClass<?> toBeTestedClass = getFactory(Arrays.asList(klass)).Class().get(klass);
//        final Map<CtClass, CtClass> initiatedClassToInitiatingMap =
//                SourceClassAnalyzer.getInitiatedClassToInitiatingMap(toBeTestedClass);
//        LOGGER.trace(initiatedClassToInitiatingMap);
//
//        initiatedClassToInitiatingMap.forEach((initiatedClass, initiatingClass) ->
//                assertEquals(expectedRes, StaticAnalyzer.checkTwoClassesAndBuildGraphs(
//                        initiatingClass, initiatedClass, "example/test/"))
//        );
//    }
//
//
//    private void testClass(Class klass) throws FileNotFoundException {
//        testClass(klass, true);
//    }

    private Factory getFactory(List<Class> classes) throws FileNotFoundException {
        Launcher spoon = new Launcher();
        Factory factory = spoon.getFactory();
        final List<String> paths = classes.stream().map(TestUtils::fromClassSrcToPath)
                .collect(Collectors.toList());
        spoon.createCompiler(
                factory,
                SpoonResourceHelper.resources(paths.toArray(new String[paths.size()])))
                .build();

        return factory;
    }

    private CtClass fromClassToCtClass(Class klass) throws FileNotFoundException {
        return getFactory(Arrays.asList(klass)).Class().get(klass);
    }

    @Test
    void findTargetSessionName() throws FileNotFoundException {
        final CtClass ctClass = fromClassToCtClass(InitiatorBaseFlow.class);
        final CtMethod callMethod = StaticAnalyzerUtils.findCallMethod(ctClass);

        final CtStatement nonContainingStatement = callMethod.getBody().getStatements().get(0);//FlowSession session = initiateFlow(otherParty);
        final Optional<String> emptyTargetSessionName = StaticAnalyzerUtils.findTargetSessionName(nonContainingStatement);
        assertThat(emptyTargetSessionName).isEqualTo(Optional.empty());

        final CtStatement containingStatement = callMethod.getBody().getStatements().get(1);//realCallMethod(session);
        final Optional<String> targetSessionName = StaticAnalyzerUtils.findTargetSessionName(containingStatement);
        assertThat(targetSessionName.get()).isEqualTo("session");
    }


    @Test
    void isCordaMethod() throws FileNotFoundException {
        final CtClass ctClass = fromClassToCtClass(InitiatorBaseFlow.class);
        final CtMethod callMethod = StaticAnalyzerUtils.findCallMethod(ctClass);

        final CtInvocation initiateMethod = (CtInvocation) callMethod.getBody().getStatements().get(0).getDirectChildren().get(1);//initiateFlow(otherParty);
        assertThat(StaticAnalyzerUtils.isCordaMethod(initiateMethod)).isEqualTo(true);

        final CtInvocation nonCordaMethod = (CtInvocation) callMethod.getBody().getStatements().get(1);//realCallMethod(session);
        assertThat(StaticAnalyzerUtils.isCordaMethod(nonCordaMethod)).isEqualTo(false);
    }

    @Test
    void getAllRelevantMethodInvocations() throws FileNotFoundException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(
                TestUtils.fromClassSrcToPath(NestedMethodInvocationsTestFlow.class));
        final CtClass<NestedMethodInvocationsTestFlow> ctClass = analyzer.getClass(NestedMethodInvocationsTestFlow.class);
        //final CtClass ctClass = (NestedMethodInvocationsTestFlow.class);
        final CtMethod callMethod = StaticAnalyzerUtils.findCallMethod(ctClass);

        //List<SignedTransaction> list = new LinkedList<>();
        final CtStatement ctIrrelevantStatement = callMethod.getBody().getStatements().get(0);
        assertThat(StaticAnalyzerUtils.getAllRelevantMethodInvocations(ctIrrelevantStatement, analyzer)).hasSize(0);

        //ClassWithSendInConstructor classWithSendInConstructor =
        //                    new ClassWithSendInConstructor(methodWithASendReturningASession(session));
        final CtStatement ctStatement = callMethod.getBody().getStatements().get(2);
        assertThat(StaticAnalyzerUtils.getAllRelevantMethodInvocations(ctStatement, analyzer)).hasSize(1);
        assertThat(StaticAnalyzerUtils.getAllRelevantMethodInvocations(ctStatement, analyzer).getStatements().get(0)
        .getInternalMethodInvocations()).hasSize(1);
    }
}