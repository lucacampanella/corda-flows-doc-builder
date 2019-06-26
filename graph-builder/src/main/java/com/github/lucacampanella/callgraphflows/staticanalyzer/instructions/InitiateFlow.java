package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzerUtils;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.*;

import java.util.Optional;

public class InitiateFlow extends InstructionStatement {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitiateFlow.class);

    String partyArgument = null;

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

        CtAbstractInvocation inv = MatcherHelper.getFirstMatchedExpression(statement, "initiateFlowMatcher");
        try {
            initiateFlow.partyArgument = inv.getArguments().get(0).toString();
        } catch (NullPointerException e) {
            LOGGER.warn("Couldn't find out the party name involved in the initiateFlow call {}, " +
                    "continuing without this information, thy may affect analysis", statement);
            LOGGER.trace("Exception: ", e);
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
        if(targetSessionName.isPresent()) {
            sb.append(Utils.removePackageDescriptionIfWanted(targetSessionName.get()));
            sb.append(" = ");
        }
        sb.append("initiateFlow(");
        if(partyArgument != null) {
            sb.append(partyArgument);
        }
        else {
            sb.append("<<Party>>");
        }
        sb.append(")");

        return sb.toString();
    }
}
