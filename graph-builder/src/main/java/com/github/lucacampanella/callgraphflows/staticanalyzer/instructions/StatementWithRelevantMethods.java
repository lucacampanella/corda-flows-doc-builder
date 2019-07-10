package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzerUtils;
import spoon.reflect.code.CtStatement;

public class StatementWithRelevantMethods extends InstructionStatement {

    private static boolean toBePainted = false;

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
    public boolean isRelevantForLoopFlowBreakAnalysis() {
        return internalMethodInvocations.isRelevantForLoopFlowBreakAnalysis();
    }

    @Override
    public boolean toBePainted() {
        return toBePainted && internalMethodInvocations.toBePainted();
    }

    public static void setToBePainted(boolean toBePainted) {
        StatementWithRelevantMethods.toBePainted = toBePainted;
    }
}
