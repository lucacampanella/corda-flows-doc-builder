package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzerUtils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import spoon.reflect.code.*;

import java.util.Optional;

public class Receive extends InstructionStatement implements StatementWithCompanionInterface {

    private static final String SYMBOL = "";//"<==";

    String receivedType;

    public Receive(CtStatement statement) {
        super(statement);
    }

    public Receive() {
        super();
    }

    public static Receive fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        Receive receive = new Receive();
        receive.line = statement.getPosition().getLine();
        receive.internalMethodInvocations.add(StaticAnalyzerUtils.getAllRelevantMethodInvocations(statement, analyzer));

        CtInvocation invocation = (CtInvocation) MatcherHelper.getFirstMatchedExpression(statement,
                "receiveMatcher");
        if(invocation == null) {
            invocation = (CtInvocation) MatcherHelper.getFirstMatchedExpression(statement,
                    "receiveWithBoolMatcher");
        }

        Object firstArgument = invocation.getArguments().get(0);

        //maybe there is a more rubust way to do this, for example with a while
        if(firstArgument instanceof CtFieldRead) {
            CtTypeAccess fieldRead = (CtTypeAccess) ((CtFieldRead) (firstArgument)).getTarget();
            receive.receivedType = analyzer.getCurrClassCallStackHolder().resolveEventualGenerics(
                    fieldRead.getAccessedType())
                    .box().toString();
        }
        else if(firstArgument instanceof CtLambda) {
            invocation = (CtInvocation) invocation.getTarget();
            //receivedType = invocation.getArguments().get(0).getTarget().getAccessedType()
            receive.receivedType = analyzer.getCurrClassCallStackHolder().resolveEventualGenerics(
                    ((CtTypeAccess) ((CtFieldRead) (invocation.getArguments().get(0))).getTarget()).getAccessedType())
                    .box().toString();
        }
        else if(firstArgument instanceof CtAbstractInvocation) {
            receive.receivedType = analyzer.getCurrClassCallStackHolder().resolveEventualGenerics(
                    ((CtAbstractInvocation) firstArgument).getExecutable().getType())
                    .box().toString();

            //todo: if the method returns a Class object with a generic attached, here is not recognized
            //be careful when comparing flows, also because here we call .toString and before getTarget

            //todo: probably is enough to do
            // ((CtAbstractInvocation) firstArgument)/*.getExecutable()*/.getType().box().toString();
        }

        receive.targetSessionName = Optional.ofNullable(invocation.getTarget().toString());

        receive.buildGraphElem();

        return receive;
    }

    @Override
    public boolean acceptCompanion(StatementWithCompanionInterface companion) {
        boolean accepted = false;
        accepted = isAccepted(companion, accepted, receivedType);

        return accepted;
    }

    static boolean isAccepted(StatementWithCompanionInterface companion, boolean accepted, String receivedType) {
        if(companion instanceof Send) {
            //check they send and receive exactly the same type
            accepted = receivedType.equals(((Send) companion).getSentType());
        }
        else if(companion instanceof SendAndReceive) {
            accepted = receivedType.equals(((SendAndReceive) companion).getSentType());
            accepted = accepted && !((SendAndReceive) companion).isSentConsumed();
            //it must be equivalent to a Send statement
            ((SendAndReceive) companion).setSentConsumed(true); //we consumed the send state of SendAndReceive
        }
        return accepted;
    }

    @Override
    public void createGraphLink(StatementWithCompanionInterface companion) {
        companion.getGraphElem().addBrother(this.getGraphElem());
        if(companion instanceof SendAndReceive) {
            ((SendAndReceive) companion).setSentConsumed(true); //we consumed the send state of SendAndReceive
        }
    }

    public String getReceivedType() {
        return receivedType;
    }

    @Override
    public boolean isSendOrReceive() {
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
        sb.append("receive(");
        sb.append(Utils.removePackageDescriptionIfWanted(receivedType));
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
