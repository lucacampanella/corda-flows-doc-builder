package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.DoubleExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.ExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.InitiatorBaseFlow;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.lucacampanella.callgraphflows.testUtils.TestUtils.fromClassSrcToPath;
import static org.assertj.core.api.Assertions.assertThat;

class AnalyzerWithModelTest {

    static SourceClassAnalyzer sca;

    @BeforeAll
    static void setUp() {
         sca = new SourceClassAnalyzer(fromClassSrcToPath(InitiatorBaseFlow.class),
                fromClassSrcToPath(ExtendingSuperclassTestFlow.class),
                        fromClassSrcToPath(DoubleExtendingSuperclassTestFlow.class));
    }

    @Test
    void analyzeFlowLogicClass() {
        final List<CtClass> initiatingClasses = sca.getClassesByAnnotation(InitiatingFlow.class);
        assertThat(initiatingClasses).hasSize(1);
        final CtClass ctClass = initiatingClasses.get(0);
        final AnalysisResult analysisResult = sca.analyzeFlowLogicClass(ctClass);
        System.out.println(analysisResult);
        assertThat(analysisResult.getClassName()).isEqualTo("InitiatorBaseFlow");
        assertThat(analysisResult.getStatements()).hasSize(2);
        assertThat(analysisResult.getCounterpartyClassResult().getClassName()).isEqualTo("Acceptor");
        assertThat(analysisResult.getCounterpartyClassResult().getStatements()).hasSize(3);
    }

    @Test
    void getClassesByAnnotation() {
        final List<CtClass> initiatingClasses = sca.getClassesByAnnotation(InitiatingFlow.class);
        assertThat(initiatingClasses).hasSize(1);
        assertThat(initiatingClasses.get(0).getSimpleName()).isEqualTo("InitiatorBaseFlow");

        final List<CtClass> initiatedByClasses = sca.getClassesByAnnotation(InitiatedBy.class);
        assertThat(initiatedByClasses).hasSize(2);
        assertThat(initiatedByClasses.get(0).getSimpleName()).isEqualTo("Acceptor");

        final List<CtClass> startableClasses = sca.getClassesByAnnotation(StartableByRPC.class);
        assertThat(startableClasses).hasSize(2);
        final List<String> classesNames = startableClasses.stream().map(CtType::getSimpleName).collect(Collectors.toList());
        assertThat(classesNames).contains("InitiatorBaseFlow", "Initiator");
    }

    @Test
    void getDeeperClassInitiatedBy() {
        final List<CtClass> initiatingClasses = sca.getClassesByAnnotation(InitiatingFlow.class);
        assertThat(initiatingClasses).hasSize(1);
        final CtClass ctClass = initiatingClasses.get(0);
        final CtClass deeperClassInitiatedBy = sca.getDeeperClassInitiatedBy(ctClass);
        assertThat(deeperClassInitiatedBy.getQualifiedName())
                .isEqualTo("com.github.lucacampanella.callgraphflows.staticanalyzer." +
                        "testclasses.DoubleExtendingSuperclassTestFlow$Acceptor");
    }

    @Test
    void getFurthestAwaySubclass() {
        final List<CtClass> initiatingClasses = sca.getClassesByAnnotation(InitiatingFlow.class);
        assertThat(initiatingClasses).hasSize(1);
        final CtClass ctClass = initiatingClasses.get(0);
        final CtClass furthestAwaySubclass = sca.getFurthestAwaySubclass(ctClass);
        assertThat(furthestAwaySubclass.getQualifiedName())
                .isEqualTo("com.github.lucacampanella.callgraphflows.staticanalyzer." +
                        "testclasses.DoubleExtendingSuperclassTestFlow$Initiator");
    }

    @Test
    void getAllSubClasses() {
         final List<CtClass> initiatingClasses = sca.getClassesByAnnotation(InitiatingFlow.class);
         assertThat(initiatingClasses).hasSize(1);
         final CtClass ctClass = initiatingClasses.get(0);
        final List<CtClass> allSubClasses = sca.getAllSubClassesIncludingThis(ctClass);
        assertThat(allSubClasses).hasSize(3);
    }
}