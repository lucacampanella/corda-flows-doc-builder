package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.svg.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzerUtils;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.support.reflect.code.CtAssignmentImpl;

import java.awt.*;

/**
 * It represents any statements that returns a flow object, so it can be a method call or a call to the flow constructor
 */
public class FlowConstructor extends InstructionStatement {

    String lhsName;

    protected FlowConstructor(CtStatement statement) {
        super(statement);
    }

    public static FlowConstructor fromStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        FlowConstructor flowConstructor = new FlowConstructor(statement);
        flowConstructor.internalMethodInvocations.add(StaticAnalyzerUtils.getAllRelevantMethodInvocations(statement,
                analyzer));


        if(statement instanceof CtLocalVariable) {
            flowConstructor.lhsName = ((CtLocalVariable) statement).getSimpleName();
        }
        else if(statement instanceof CtAssignment) {
            flowConstructor.lhsName = ((CtAssignmentImpl) statement).getAssigned().toString();
        }

        flowConstructor.targetSessionName =  StaticAnalyzerUtils.findTargetSessionName(statement);

        return flowConstructor;
    }

    public String getLhsName() {
        return lhsName;
    }

    @Override
    public boolean modifiesFlow() {
        return true;
    }

    @Override
    protected Color getTextColor() { return GBaseTextComponent.LESS_IMPORTANT_TEXT_COLOR; }

    @Override
    public boolean toBePainted() {
        return false;
    }
}
