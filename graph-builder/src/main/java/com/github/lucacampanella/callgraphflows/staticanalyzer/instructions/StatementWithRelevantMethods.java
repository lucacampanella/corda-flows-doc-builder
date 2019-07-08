package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzerUtils;
import spoon.reflect.code.CtStatement;

public class StatementWithRelevantMethods extends InstructionStatement {

    protected StatementWithRelevantMethods(CtStatement statement) {
        super(statement);
    }

    public static StatementWithRelevantMethods fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        StatementWithRelevantMethods statementWithRelevantMethods = new StatementWithRelevantMethods(statement);
        statementWithRelevantMethods.internalMethodInvocations.add(
                StaticAnalyzerUtils.getAllRelevantMethodInvocations(statement, analyzer));

        return statementWithRelevantMethods;
    }

    @Override
    public boolean isRelevantForAnalysis() {
        return internalMethodInvocations.isRelevant();
    }
}
