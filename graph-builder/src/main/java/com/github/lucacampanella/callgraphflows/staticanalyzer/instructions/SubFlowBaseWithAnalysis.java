package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;

public abstract class SubFlowBaseWithAnalysis extends SubFlowBase {
    AnalysisResult resultOfClassAnalysis = new AnalysisResult();

    public AnalysisResult getResultOfClassAnalysis() {
        return resultOfClassAnalysis;
    }
}
