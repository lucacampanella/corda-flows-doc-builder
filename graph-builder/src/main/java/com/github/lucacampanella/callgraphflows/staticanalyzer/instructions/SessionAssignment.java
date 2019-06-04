package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzerUtils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import net.corda.core.flows.FlowSession;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtTypedElement;
import spoon.support.reflect.code.CtAssignmentImpl;

import java.awt.*;
import java.util.Optional;

public class SessionAssignment extends InstructionStatement {

    private String rhsName;

    protected SessionAssignment(CtStatement statement) {
        super(statement);
    }

    public static SessionAssignment fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        if(!((CtTypedElement) statement).getType().isSubtypeOf(MatcherHelper.getTypeReference(FlowSession.class))) {
            return null;
        }
        SessionAssignment sessionAssignment = new SessionAssignment(statement);
        System.out.println("invoked getAllRelevantMethodInvocations for "
                + statement + " class SessionAssignment");
        sessionAssignment.internalMethodInvocations.add(StaticAnalyzerUtils.
                getAllRelevantMethodInvocations(statement, analyzer));

        if(statement instanceof CtLocalVariable) {
            sessionAssignment.targetSessionName = Optional.ofNullable(((CtLocalVariable) statement).getSimpleName());
            final CtExpression defaultExpression = ((CtLocalVariable) statement).getDefaultExpression();
            //if it's just a variable declaration without rhs we assign null to it (also null is assigned if in the
            //declaration the rhs is null
            sessionAssignment.rhsName = defaultExpression == null ? "null" : defaultExpression.toString();
        }
        else if(statement instanceof CtAssignment) {
            sessionAssignment.targetSessionName = Optional.ofNullable(((CtAssignmentImpl) statement).getAssigned().toString());
            final CtExpression assignment = ((CtAssignmentImpl) statement).getAssignment();
            sessionAssignment.rhsName = assignment == null ? "null" : assignment.toString();
            //we keep "this." in front of field names
        }
        return sessionAssignment;
    }

    public String getLhsName() {
        return targetSessionName.orElse(null);
    }

    public String getRhsName() {
        return rhsName;
    }

    @Override
    public boolean modifiesSession() {
        return true;
    }

    @Override
    protected Color getTextColor() { return GBaseTextComponent.LESS_IMPORTANT_TEXT_COLOR; }

    @Override
    public boolean toBePainted() {
        return false;
    }
}
