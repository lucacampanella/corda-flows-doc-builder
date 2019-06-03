package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.DoubleExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.ExtendingSuperclassTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.subclassestests.InitiatorBaseFlow;
import org.junit.jupiter.api.BeforeAll;

import static com.github.lucacampanella.callgraphflows.testUtils.TestUtils.fromClassSrcToPath;

class SourceClassAnalyzerTest extends AnalyzerWithModelTest {

    @BeforeAll
    static void setUp() {
        analyzerWithModel = new SourceClassAnalyzer(fromClassSrcToPath(InitiatorBaseFlow.class),
                fromClassSrcToPath(ExtendingSuperclassTestFlow.class),
                fromClassSrcToPath(DoubleExtendingSuperclassTestFlow.class));
    }

}