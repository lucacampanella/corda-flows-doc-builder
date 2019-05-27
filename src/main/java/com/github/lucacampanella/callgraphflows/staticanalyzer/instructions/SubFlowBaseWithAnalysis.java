package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components.GSubFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;

public abstract class SubFlowBaseWithAnalysis extends SubFlowBase {
    AnalysisResult resultOfClassAnalysis = new AnalysisResult();

    public AnalysisResult getResultOfClassAnalysis() {
        return resultOfClassAnalysis;
    }

    @Override
    protected GSubFlow getMainSubFlowElement() {
        final GSubFlow mainSubFlow = super.getMainSubFlowElement();
        resultOfClassAnalysis.getStatements().forEach(stmt -> mainSubFlow.addComponent(stmt.getGraphElem()));
        return mainSubFlow;
    }
}
