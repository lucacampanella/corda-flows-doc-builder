package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzerUtils;
import spoon.reflect.code.*;

import java.awt.*;

/**
 * Class that maps CtCFlowBreak, so CtBreak, CtContinue, CtLabelledFlowBreak, CtReturn, CtThrow
 */
public class CodeFlowBreak extends InstructionStatement {

    private enum BreakType {
        BREAK, CONTINUE, THROW, RETURN;

        public static BreakType fromCtStatement(CtStatement statement) {
            if(statement instanceof CtBreak) {
                return BREAK;
            }
            if(statement instanceof CtContinue) {
                return CONTINUE;
            }
            if(statement instanceof CtReturn) {
                return RETURN;
            }
            if(statement instanceof CtThrow) {
                return THROW;
            }
            return null;
        }
    }

    private static boolean drawReturn = false;
    private static boolean drawThrow = true;
    private static boolean drawBreakContinue = true;

    private BreakType breakType = null;

    protected CodeFlowBreak(CtStatement statement) {
        super(statement);
    }

    public static CodeFlowBreak fromStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        CodeFlowBreak flowBreak = new CodeFlowBreak(statement);
        flowBreak.internalMethodInvocations.add(
                StaticAnalyzerUtils.getAllRelevantMethodInvocations(statement, analyzer));
        flowBreak.breakType = BreakType.fromCtStatement(statement);
        return flowBreak;
    }

    @Override
    protected Color getTextColor() { return Color.GRAY; }

    @Override
    public boolean isBreakLoopFlowBreak() { //break
        return breakType == BreakType.BREAK;
    }

    @Override
    public boolean isContinueLoopFlowBreak() { //continue
        return breakType == BreakType.CONTINUE;
    }

    @Override
    public boolean isMethodFlowBreak() { //throw, return
        return breakType == BreakType.THROW || breakType == BreakType.RETURN;
    }

    @Override
    public boolean isRelevantForProtocolAnalysis() {
        return false;
    }

    @Override
    public boolean isRelevantForMethodFlowBreakAnalysis() {
        return isMethodFlowBreak();
    }

    @Override
    public boolean toBePainted() {
        return ((breakType == BreakType.BREAK || breakType == BreakType.CONTINUE) && drawBreakContinue)
                || (breakType == BreakType.THROW && drawThrow)
                || (breakType == BreakType.RETURN && drawReturn);
    }

    public static void setDrawReturn(boolean drawReturn) {
        CodeFlowBreak.drawReturn = drawReturn;
    }

    public static void setDrawThrow(boolean drawThrow) {
        CodeFlowBreak.drawThrow = drawThrow;
    }

    public static void setDrawBreakContinue(boolean drawBreakContinue) {
        CodeFlowBreak.drawBreakContinue = drawBreakContinue;
    }
}
