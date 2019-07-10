package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.DoubleExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.ExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.InitiatorBaseFlow;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.lucacampanella.TestUtils.fromClassSrcToPath;
import static org.assertj.core.api.Assertions.assertThat;

class AnalyzerWithModelTest {

    static AnalyzerWithModel analyzerWithModel;

    @BeforeAll
    static void setUp() throws IOException {
         analyzerWithModel = new SourceClassAnalyzer(fromClassSrcToPath(InitiatorBaseFlow.class),
                fromClassSrcToPath(ExtendingSuperclassTestFlow.class),
                        fromClassSrcToPath(DoubleExtendingSuperclassTestFlow.class));
    }

    @Test
    void analyzeFlowLogicClass() throws AnalysisErrorException {
        final List<CtClass> initiatingClasses = analyzerWithModel.getClassesByAnnotation(InitiatingFlow.class);
        assertThat(initiatingClasses).hasSize(1);
        final CtClass ctClass = initiatingClasses.get(0);
        final AnalysisResult analysisResult = analyzerWithModel.analyzeFlowLogicClass(ctClass);
        assertThat(analysisResult.getClassDescription().getSimpleName()).isEqualTo("InitiatorBaseFlow");
        assertThat(analysisResult.getStatements()).hasSize(3);
        assertThat(analysisResult.getCounterpartyClassResult().getClassDescription().getSimpleName()).isEqualTo("Acceptor");
        assertThat(analysisResult.getCounterpartyClassResult().getStatements()).hasSize(4);
    }

    @Test
    void getClassesByAnnotation() {
        final List<CtClass> initiatingClasses = analyzerWithModel.getClassesByAnnotation(InitiatingFlow.class);
        assertThat(initiatingClasses).hasSize(1);
        assertThat(initiatingClasses.get(0).getSimpleName()).isEqualTo("InitiatorBaseFlow");

        final List<CtClass> initiatedByClasses = analyzerWithModel.getClassesByAnnotation(InitiatedBy.class);
        assertThat(initiatedByClasses).hasSize(2);
        assertThat(initiatedByClasses.get(0).getSimpleName()).isEqualTo("Acceptor");

        final List<CtClass> startableClasses = analyzerWithModel.getClassesByAnnotation(StartableByRPC.class);
        assertThat(startableClasses).hasSize(2);
        final List<String> classesNames = startableClasses.stream().map(CtType::getSimpleName).collect(Collectors.toList());
        assertThat(classesNames).contains("InitiatorBaseFlow", "Initiator");
    }

    @Test
    void getDeeperClassInitiatedBy() {
        final List<CtClass> initiatingClasses = analyzerWithModel.getClassesByAnnotation(InitiatingFlow.class);
        assertThat(initiatingClasses).hasSize(1);
        final CtClass ctClass = initiatingClasses.get(0);
        final CtClass deeperClassInitiatedBy = analyzerWithModel.getDeeperClassInitiatedBy(ctClass);
        assertThat(deeperClassInitiatedBy.getQualifiedName())
                .isEqualTo("com.github.lucacampanella.callgraphflows.staticanalyzer." +
                        "testclasses.subclassestests.DoubleExtendingSuperclassTestFlow$Acceptor");
    }

    @Test
    void getFurthestAwaySubclass() {
        final List<CtClass> initiatingClasses = analyzerWithModel.getClassesByAnnotation(InitiatingFlow.class);
        assertThat(initiatingClasses).hasSize(1);
        final CtClass ctClass = initiatingClasses.get(0);
        final CtClass furthestAwaySubclass = analyzerWithModel.getFurthestAwaySubclass(ctClass);
        assertThat(furthestAwaySubclass.getQualifiedName())
                .isEqualTo("com.github.lucacampanella.callgraphflows.staticanalyzer." +
                        "testclasses.subclassestests.DoubleExtendingSuperclassTestFlow$Initiator");
    }

    @Test
    void getAllSubClasses() {
         final List<CtClass> initiatingClasses = analyzerWithModel.getClassesByAnnotation(InitiatingFlow.class);
         assertThat(initiatingClasses).hasSize(1);
         final CtClass ctClass = initiatingClasses.get(0);
        final List<CtClass> allSubClasses = analyzerWithModel.getAllSubClassesIncludingThis(ctClass);
        assertThat(allSubClasses).hasSize(3);
    }
}