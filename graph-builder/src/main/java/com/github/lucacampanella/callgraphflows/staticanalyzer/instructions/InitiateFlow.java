package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzerUtils;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;

import java.util.Optional;

public class InitiateFlow extends InstructionStatement {

    public InitiateFlow(CtStatement statement) {
        super(statement);
    }
    public InitiateFlow() {
        super();
    }

    public static InitiateFlow fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        InitiateFlow initiateFlow = new InitiateFlow();
        initiateFlow.line = statement.getPosition().getLine();
        initiateFlow.internalMethodInvocations.add(StaticAnalyzerUtils.getAllRelevantMethodInvocations(statement, analyzer));

        if(statement instanceof CtLocalVariable) {
            initiateFlow.targetSessionName = Optional.ofNullable(((CtLocalVariable) statement).getReference().getSimpleName());
        }
        else if(statement instanceof CtAssignment) {
            initiateFlow.targetSessionName = Optional.ofNullable(((CtAssignment) statement).getAssigned().toString());
        }

        initiateFlow.buildGraphElem();
        return initiateFlow;
    }

    @Override
    public boolean modifiesSession() {
        return true;
    }

    /**
     * @return true, being itself an InitiateFlow call
     */
    @Override
    public Optional<InitiateFlow> getInitiateFlowStatementAtThisLevel() {
        return Optional.of(this);
    }

    @Override
    protected void buildGraphElem() {
        super.buildGraphElem();
        graphElem.setDrawBox(true);
    }

    @Override
    public String getStringDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("initiateFlow(");
        if(targetSessionName.isPresent()) {
            sb.append(Utils.removePackageDescriptionIfWanted(targetSessionName.get()));
        }
        else {
            sb.append("session");
        }
        sb.append(")");

        return sb.toString();
    }
}
