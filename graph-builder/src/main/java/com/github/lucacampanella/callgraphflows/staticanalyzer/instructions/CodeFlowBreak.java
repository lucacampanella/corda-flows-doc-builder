package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzerUtils;
import spoon.reflect.code.CtStatement;

import java.awt.*;

/**
 * Class that maps CtCFlowBreak, so CtBreak, CtContinue, CtLabelledFlowBreak, CtReturn, CtThrow
 */
public class CodeFlowBreak extends InstructionStatement {
    protected CodeFlowBreak(CtStatement statement) {
        super(statement);
    }

    public static CodeFlowBreak fromStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        CodeFlowBreak flowBreak = new CodeFlowBreak(statement);
        flowBreak.internalMethodInvocations.add(
                StaticAnalyzerUtils.getAllRelevantMethodInvocations(statement, analyzer));
        return flowBreak;
    }

    @Override
    protected Color getTextColor() { return Color.GRAY; }

    @Override
    public boolean isRelevantForLoop() {
        return true;
    }

    @Override
    public boolean isRelevantForAnalysis() {
        return internalMethodInvocations.isRelevant();
    }
}
