package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzer;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import net.corda.core.flows.FlowLogic;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtTypedElement;
import spoon.support.reflect.code.CtAssignmentImpl;

import java.awt.*;

/**
 * It represents an assignment to a flow variable.
 * For example:
 * {@code FinalityFlow finalityFlow = otherFinalityFlow;}
 */
public class FlowAssignment extends InstructionStatement {

    private String lhsName;
    private String rhsName;

    protected FlowAssignment(CtStatement statement) {
        super(statement);
    }

    public static FlowAssignment fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        if(!((CtTypedElement) statement).getType().isSubtypeOf(MatcherHelper.getTypeReference(FlowLogic.class))) {
            return null;
        }

        FlowAssignment flowAssignment = new FlowAssignment(statement);
        flowAssignment.internalMethodInvocations.add(
                StaticAnalyzer.getAllRelevantMethodInvocations(statement, analyzer));


        if(statement instanceof CtLocalVariable) {
            flowAssignment.lhsName = ((CtLocalVariable) statement).getSimpleName();
            flowAssignment.rhsName = ((CtLocalVariable) statement).getDefaultExpression().toString();
        }
        else if(statement instanceof CtAssignment) {
            flowAssignment.lhsName = ((CtAssignmentImpl) statement).getAssigned().toString();
            flowAssignment.rhsName = ((CtAssignmentImpl) statement).getAssignment().toString();
            //we keep "this." in front of field names
        }

        return flowAssignment;
    }

    public String getLhsName() {
        return lhsName;
    }

    public String getRhsName() {
        return rhsName;
    }

    @Override
    public boolean modifiesFlow() {
        return true;
    }

    protected Color getTextColor() { return GBaseTextComponent.LESS_IMPORTANT_TEXT_COLOR; }

    @Override
    public boolean toBePainted() {
        return false;
    }
}
