package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.DoubleExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.ExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.InitiatorBaseFlow;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.github.lucacampanella.TestUtils.fromClassSrcToPath;
import static org.assertj.core.api.Assertions.assertThat;


class SourceClassAnalyzerTest extends AnalyzerWithModelTest {

    @BeforeAll
    static void setUp() {
        analyzerWithModel = new SourceClassAnalyzer(fromClassSrcToPath(InitiatorBaseFlow.class),
                fromClassSrcToPath(ExtendingSuperclassTestFlow.class),
                fromClassSrcToPath(DoubleExtendingSuperclassTestFlow.class));
    }

    @Test
    void getAnalysisName() {
        assertThat(analyzerWithModel.getAnalysisName()).isEqualTo(
                "InitiatorBaseFlow.java, ExtendingSuperclassTestFlow.java, DoubleExtendingSuperclassTestFlow.java");
    }
}