package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
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
        return flowBreak;
    }

    protected Color getTextColor() { return Color.GRAY; }

    @Override
    public boolean isRelevantForLoop() {
        return true;
    }

    @Override
    public boolean isRelevantForAnalysis() {
        return false;
    }
}
