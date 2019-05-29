package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.utils.Utils;
import com.github.lucacampanella.callgraphflows.graphics.components.GBaseGraphicComponent;
import com.github.lucacampanella.callgraphflows.graphics.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.graphics.components.GSubFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzer;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import spoon.reflect.code.*;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtParameter;

import java.util.*;

public class MethodInvocation extends InstructionStatement {

    Branch body = new Branch();
    Map<String, String> callerSessionNameToCalleeSessionName = new HashMap<>();
    Map<String, String> callerFlowNameToCalleeFlowName = new HashMap<>();
    GSubFlow indentedComponent = new GSubFlow();

    protected MethodInvocation(CtStatement statement) {
        super(statement.getPosition() instanceof  NoSourcePosition ? 0 : statement.getPosition().getLine(),
                    Utils.fromStatementToString(statement));
    }


    public static MethodInvocation fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {

        MethodInvocation methodInvocation = new MethodInvocation(statement);

        if (StaticAnalyzer.isCordaMethod(statement)) { //this should never happen
            System.out.println("Error");
            return null;
        }

        if(statement instanceof CtInvocation) {
            CtInvocation inv = (CtInvocation) statement;

            if (inv.getTarget() instanceof CtInvocation) { //the target is the "inner" invocation
                MethodInvocation targetInv = MethodInvocation.fromCtStatement((CtInvocation) inv.getTarget(), analyzer);
                methodInvocation.internalMethodInvocations.addIfRelevant(targetInv);
            }
        }

        if(statement instanceof CtAbstractInvocation) {
            CtAbstractInvocation inv = (CtAbstractInvocation) statement;
            final List<CtExpression> arguments = inv.getArguments();
            System.out.println("all arguments: " + arguments);
            for(int i = 0; i < arguments.size(); ++i) {
                CtExpression expr = arguments.get(i);
                if(expr instanceof CtVariableRead && expr.getType() != null) {
                    CtVariableRead varRead = (CtVariableRead) expr;
                    if(varRead.getType().isSubtypeOf(MatcherHelper.getTypeReference(FlowSession.class))) {
                        addToMapIfNoException(methodInvocation.callerSessionNameToCalleeSessionName, inv, i);
                        System.out.println("found a flow session argument");
                    }
                    else if(varRead.getType().isSubtypeOf(MatcherHelper.getTypeReference(FlowLogic.class))) {
                        addToMapIfNoException(methodInvocation.callerFlowNameToCalleeFlowName, inv, i);
                        System.out.println("found a flow logic argument");
                    }
                }
                else {
                    System.out.println("expr: " + expr +" of type " + expr.getType() + " " + expr.getShortRepresentation());
                    if(expr.getType() != null) { //todo: I think this just means void, but test
                        if (expr.getType().isSubtypeOf(MatcherHelper.getTypeReference(FlowSession.class))) {
                            addToMapIfNoException(methodInvocation.callerSessionNameToCalleeSessionName, inv, i);
                            //todo: how to handle methods here?
                            System.out.println("found a flow session argument");
                        } else if (expr.getType().isSubtypeOf(MatcherHelper.getTypeReference(FlowLogic.class))) {
                            addToMapIfNoException(methodInvocation.callerFlowNameToCalleeFlowName, inv, i);
                            //todo: how to handle methods here?
                            System.out.println("found a flow logic argument");
                        }
                    }
                    System.out.println("invoked getAllRelevantMethodInvocations for expr " + expr);
                    final Branch allRelevantMethodInvocations = StaticAnalyzer.getAllRelevantMethodInvocations(expr,
                            analyzer);
                    methodInvocation.internalMethodInvocations.addIfRelevant(allRelevantMethodInvocations);
                }
            }
            try {
                final Branch bodyStatements = MatcherHelper.fromCtStatementsToStatements(
                        inv.getExecutable().getDeclaration().getBody().getStatements(), analyzer);
                methodInvocation.body.addIfRelevant(bodyStatements);
            } catch (NullPointerException e) {
                System.out.println("Couldn't retrieve the body of method" + inv.toString() + ", adding an empty one");
            }
        }


//        Object firstArgument = arguments.get(0);
//
//        //maybe there is a more rubust way to do this, for example with a while
//        if(firstArgument instanceof CtFieldRead) {
//            CtTypeAccess fieldRead = (CtTypeAccess) ((CtFieldRead) (firstArgument)).getTarget();
//            receive.receivedType = fieldRead.getAccessedType().box().getSimpleName();
//        }
//        else if(firstArgument instanceof CtLambda) {
//            invocation = (CtInvocation) invocation.getTarget();
//            //receivedType = invocation.getArguments().get(0).getTarget().getAccessedType()
//            receive.receivedType = ((CtTypeAccess) ((CtFieldRead) (arguments.get(0))).getTarget())
//                    .getAccessedType().box().getSimpleName();
//        }
//
//        receive.targetSessionName = Optional.ofNullable(invocation.getTarget().toString());

        methodInvocation.buildGraphElem();

        System.out.println("returning from method invocation of statement " + statement);
        return methodInvocation;
    }

    private static void addToMapIfNoException(Map<String, String> map, CtAbstractInvocation inv, int i) {
        try {
            map.put(inv.getArguments().get(i).toString(),
                    ((CtParameter) inv.getExecutable().getDeclaration().getParameters().get(i))
                            .getSimpleName());
        }catch (NullPointerException e) {
            System.out.println("Error while retrieving parameter name for method: " + inv);
        }
    }

    @Override
    public GBaseGraphicComponent getGraphElem() {
        if (toBePainted()) {
            return body.isEmpty() ? super.getGraphElem() : indentedComponent;
        }
        else {
            return null;
        }
    }

    @Override
    protected void buildGraphElem() {
        indentedComponent.setEnteringArrowText((GBaseTextComponent) super.getGraphElem());
        body.forEach(stmt -> indentedComponent.addComponent(stmt.getGraphElem()));
    }

    @Override
    public boolean isRelevantForAnalysis() {
        return internalMethodInvocations.isRelevant() ||
                body.getStatements().stream().anyMatch(stmt -> {
                    if(!stmt.isRelevantForAnalysis()) {
                        return false;
                    }
                    if(stmt instanceof FlowAssignment ||
                    stmt instanceof FlowConstructor ||
                    stmt instanceof SessionAssignment) {
                        return false;
                    }
                    return true;
                });
    }

    @Override
    public Optional<InitiateFlow> getInitiateFlowStatementAtThisLevel() {
        Optional<InitiateFlow> res = super.getInitiateFlowStatementAtThisLevel();
        if(res.isPresent()) {
            return res;
        }
        return body.getInitiateFlowStatementAtThisLevel();
    }

    public Branch getBody() {
        return body;
    }

    @Override
    public boolean toBePainted() {
        return body.toBePainted();
    }
}
