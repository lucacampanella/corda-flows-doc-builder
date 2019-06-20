package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import spoon.reflect.code.CtStatement;

import java.awt.*;

/**
 * Class that maps CtCFlowBreak, so CtBreak, CtContinue, CtLabelledFlowBreak, CtReturn<R>, CtThrow
 */
public class FlowBreak extends InstructionStatement {
    protected FlowBreak(CtStatement statement) {
        super(statement);
    }

    public static FlowBreak fromStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        FlowBreak flowBreak = new FlowBreak(statement);
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
