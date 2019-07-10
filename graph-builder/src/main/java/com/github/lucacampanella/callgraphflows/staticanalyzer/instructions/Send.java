package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components2.GBaseSimpleComponent;
import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzerUtils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtTypedElement;

import java.util.Optional;

public class Send extends InstructionStatement implements StatementWithCompanionInterface {
    private static final String SYMBOL = "";//"==>";


    String sentType;

    protected Send(CtStatement statement) {
        super(statement);
    }

    protected Send() {
        super();
    }

    public static Send fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        Send send = new Send();
        send.line = statement.getPosition().getLine();
        send.internalMethodInvocations.add(StaticAnalyzerUtils.getAllRelevantMethodInvocations(statement, analyzer));

        CtInvocation invocation = (CtInvocation) MatcherHelper.getFirstMatchedExpression(statement,
                "sendMatcher");
        if(invocation == null) {
            invocation = (CtInvocation) MatcherHelper.getFirstMatchedExpression(statement,
                    "sendWithBoolMatcher");
        }
        send.sentType = analyzer.getCurrClassCallStackHolder().resolveEventualGenerics(
                ((CtTypedElement) invocation.getArguments().get(0)).getType())
                .box().toString();

        send.targetSessionName = Optional.ofNullable(invocation.getTarget().toString());

        send.buildGraphElem();
        return send;
    }

    @Override
    public boolean acceptCompanion(StatementWithCompanionInterface companion) {
        boolean accepted = false;
        accepted = isAccepted(companion, accepted, sentType);

        return accepted;
    }

    static boolean isAccepted(StatementWithCompanionInterface companion, boolean accepted, String sentType) {
        if(companion instanceof Receive) {
            //check they send and receive exactly the same type
            accepted = sentType.equals(((Receive) companion).getReceivedType());
        }
        else if(companion instanceof SendAndReceive) {
            accepted = sentType.equals(((SendAndReceive) companion).getReceivedType());
            accepted = accepted && ((SendAndReceive) companion).isSentConsumed();
            // it must be equal to a Receive statement
            ((SendAndReceive) companion).setSentConsumed(false); //reset counter
        }
        return accepted;
    }

    @Override
    public void createGraphLink(StatementWithCompanionInterface companion) {
        graphElem.setBrotherSafely((GBaseSimpleComponent) companion.getGraphElem());
        if(companion instanceof SendAndReceive) {
            // it must be equal to a Receive statement
            ((SendAndReceive) companion).setSentConsumed(false); //reset counter
        }
    }

    public String getSentType() {
        return sentType;
    }

    @Override
    public boolean hasSendOrReceiveAtThisLevel() {
        return true;
    }

    @Override
    public String addIconsToText(String displayText) {
        return SYMBOL + " " + displayText + " " + SYMBOL;
    }

    public static String getSYMBOL() {
        return SYMBOL;
    }

    @Override
    public String getStringDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSYMBOL());
        sb.append("send(");
        sb.append(Utils.removePackageDescriptionIfWanted(sentType));
        sb.append(")");
        sb.append(getSYMBOL());

        return sb.toString();
    }

    @Override
    protected void buildGraphElem() {
        super.buildGraphElem();
        graphElem.setDrawBox(true);
    }
}
