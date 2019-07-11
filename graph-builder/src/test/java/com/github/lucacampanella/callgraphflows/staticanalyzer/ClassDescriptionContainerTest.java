package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.TestUtils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.ReturnTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.SimpleTestFlow;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ClassDescriptionContainerTest {

    @Test
    void classDescriptionTest() {
        AnalyzerWithModel analyzer = new SourceClassAnalyzer(TestUtils.fromClassSrcToPath(SimpleTestFlow.class),
                TestUtils.fromClassSrcToPath(ReturnTestFlow.class));

        final ClassDescriptionContainer simpleTestFlowInitiatorContainer =
                ClassDescriptionContainer.fromClass(analyzer.getClass(SimpleTestFlow.Initiator.class));

        final ClassDescriptionContainer simpleTestFlowInitiatorContainer2 =
                ClassDescriptionContainer.fromClass(analyzer.getClass(SimpleTestFlow.Initiator.class));

        final ClassDescriptionContainer returnTestFlowInitiatorContainer =
                ClassDescriptionContainer.fromClass(analyzer.getClass(ReturnTestFlow.Initiator.class));

        assertThat(simpleTestFlowInitiatorContainer.equals(returnTestFlowInitiatorContainer)).isEqualTo(false);
        assertThat(simpleTestFlowInitiatorContainer.equals(simpleTestFlowInitiatorContainer2)).isEqualTo(true);
        assertThat(simpleTestFlowInitiatorContainer.hashCode()).isEqualTo(simpleTestFlowInitiatorContainer2.hashCode());
        assertThat(simpleTestFlowInitiatorContainer.hashCode()).isNotEqualTo(returnTestFlowInitiatorContainer.hashCode());
        assertThat(simpleTestFlowInitiatorContainer.getSimpleName()).isEqualTo("Initiator");
        assertThat(simpleTestFlowInitiatorContainer.getContainingClassName()).isEqualTo("SimpleTestFlow");
        assertThat(simpleTestFlowInitiatorContainer.getContainingClassNameOrItself()).isEqualTo("SimpleTestFlow");
        assertThat(simpleTestFlowInitiatorContainer.getFullyQualifiedName()).isEqualTo("com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.SimpleTestFlow$Initiator");
        assertThat(simpleTestFlowInitiatorContainer.getNameWithParent()).isEqualTo("SimpleTestFlow$Initiator");
        assertThat(simpleTestFlowInitiatorContainer.getPackageName()).isEqualTo("com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses");
        assertThat(simpleTestFlowInitiatorContainer.getComments()).contains("javadoc comments that will appear in the asciidoc");
    }

}