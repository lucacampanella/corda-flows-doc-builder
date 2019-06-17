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
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.github.lucacampanella.TestUtils.fromClassSrcToPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StaticAnalyzerUtilsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticAnalyzerUtilsTest.class);

    @BeforeAll
    static void setUp() {
        Paths.get(System.getProperty("user.dir"), DrawerUtil.DEFAULT_OUT_DIR).toFile().mkdirs();
    }

        @Test
    public void findCallMethod() throws FileNotFoundException {
        final CtClass ctClass = TestUtils.fromClassToCtClass(ExtendingSuperclassTestFlow.class);
        assertTrue(StaticAnalyzerUtils.findCallMethod(ctClass) != null);
    }

    @Test
    void findTargetSessionName() throws FileNotFoundException {
        final CtClass ctClass = TestUtils.fromClassToCtClass(InitiatorBaseFlow.class);
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
        final CtClass ctClass = TestUtils.fromClassToCtClass(InitiatorBaseFlow.class);
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