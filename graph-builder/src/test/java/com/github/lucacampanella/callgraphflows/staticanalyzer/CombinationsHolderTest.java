package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.TestUtils;
import com.github.lucacampanella.callgraphflows.AnalysisErrorException;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.DoWhileTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.IfFailingTestFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.SimpleFlowTestFlow;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtClass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CombinationsHolderTest {

    @Test
    void matchesCombinationsTrue() throws AnalysisErrorException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(TestUtils.fromClassSrcToPath(DoWhileTestFlow.class));
        final CtClass startableClass = analyzer.getClassesToBeAnalyzed().get(0);
        final AnalysisResult analysisResult = analyzer.analyzeFlowLogicClass(startableClass);
        final boolean validProtocol = analysisResult.containsValidProtocol();
        assertThat(validProtocol).isEqualTo(true);
    }

    @Test
    void matchesCombinationsFalse() throws AnalysisErrorException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(TestUtils.fromClassSrcToPath(IfFailingTestFlow.class));
        final CtClass startableClass = analyzer.getClassesToBeAnalyzed().get(0);
        final AnalysisResult analysisResult = analyzer.analyzeFlowLogicClass(startableClass);
        final boolean validProtocol = analysisResult.containsValidProtocol();
        assertThat(validProtocol).isEqualTo(false);
    }

}