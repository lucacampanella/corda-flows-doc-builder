package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.DrawerUtil;
import com.github.lucacampanella.callgraphflows.graphics.components2.GBaseComponent;
import com.github.lucacampanella.callgraphflows.graphics.components2.GBaseText;
import com.github.lucacampanella.callgraphflows.graphics.components2.GConditionalBranchIndented;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.CombinationsHolder;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzerUtils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import com.github.lucacampanella.callgraphflows.utils.Utils;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.*;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MethodInvocation extends InstructionStatement {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodInvocation.class);

    Branch body = new Branch();
    Map<String, String> callerSessionNameToCalleeSessionName = new HashMap<>();
    Map<String, String> callerFlowNameToCalleeFlowName = new HashMap<>();
    GConditionalBranchIndented indentedComponent = new GConditionalBranchIndented();
    String returnType = null;

    protected MethodInvocation(CtStatement statement) {
        super(statement.getPosition() instanceof  NoSourcePosition ? 0 : statement.getPosition().getLine(),
                Utils.fromStatementToString(statement));
    }


    public static MethodInvocation fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {

        MethodInvocation methodInvocation = new MethodInvocation(statement);

        if(statement instanceof CtAbstractInvocation) {
            CtAbstractInvocation inv = (CtAbstractInvocation) statement;
            CtExecutable dynamicallyDispatchedExecutable;
            CtExecutable declaration = null;

            if (MatcherHelper.isCordaMethod(inv)) {
                LOGGER.trace("A method invocation is created using a corda method," +
                        "this happens in case of receive().unwrap(), the receive is already analyzed and will" +
                        "be skipped");
                return null;
            }

            try {
                declaration = inv.getExecutable().getDeclaration();
            } catch (NullPointerException e) {
                LOGGER.warn("Couldn't retrieve the declaration of method {} adding an empty one", inv);
            }

            if(statement instanceof CtInvocation) {
                CtInvocation methodInv = (CtInvocation) statement;

                if (methodInv.getTarget() instanceof CtInvocation) { //the target is the "inner" invocation
                    MethodInvocation targetInv = MethodInvocation.fromCtStatement((CtInvocation) methodInv.getTarget(), analyzer);
                    methodInvocation.internalMethodInvocations.addIfRelevantForLoopFlowBreakAnalysis(targetInv);
                }
            }

            //this and the preceding if should mean the same, but they don't in case of a call to "this(...)"
            //which is still represented as a CtInvocation even if it's a constructor call
            if(declaration instanceof CtMethod) {
                dynamicallyDispatchedExecutable =
                        analyzer.getCurrClassCallStackHolder().
                                fromCtAbstractInvocationToDynamicExecutableRef((CtInvocation) statement);
            }
            else { //a constructor call
                //could use getExecutableDeclaration instaead of getDeclaration, but this would also mean
                //analyzing the body of corda methods, for now this is ok
                dynamicallyDispatchedExecutable = declaration;
            }

            try {
                methodInvocation.returnType = StaticAnalyzerUtils
                        .nullifyIfVoidTypeAndGetString(
                                analyzer.getCurrClassCallStackHolder().resolveEventualGenerics(
                                        inv.getExecutable().getDeclaration().getType()));
            } catch(NullPointerException e) {
                LOGGER.warn("Couldn't figure out the return type of method {}, continuing without", inv);
            }

            final List<CtExpression> arguments = inv.getArguments();
            for(int i = 0; i < arguments.size(); ++i) {
                CtExpression expr = arguments.get(i);
                if(expr instanceof CtVariableRead && expr.getType() != null) {
                    CtVariableRead varRead = (CtVariableRead) expr;
                    if(analyzer.getCurrClassCallStackHolder().resolveEventualGenerics(varRead.getType())
                            .isSubtypeOf(MatcherHelper.getTypeReference(FlowSession.class))) {
                        addToMapIfNoException(methodInvocation.callerSessionNameToCalleeSessionName, inv, i);
                    }
                    else if(analyzer.getCurrClassCallStackHolder().resolveEventualGenerics(varRead.getType())
                            .isSubtypeOf(MatcherHelper.getTypeReference(FlowLogic.class))) {
                        addToMapIfNoException(methodInvocation.callerFlowNameToCalleeFlowName, inv, i);
                    }
                }
                else {
                    LOGGER.trace("expr: {} of type {} {}", expr, expr.getType(), expr.getShortRepresentation());
                    if(expr.getType() != null) { //todo: I think this just means void, but test
                        if (analyzer.getCurrClassCallStackHolder().resolveEventualGenerics(expr.getType())
                                .isSubtypeOf(MatcherHelper.getTypeReference(FlowSession.class))) {
                            addToMapIfNoException(methodInvocation.callerSessionNameToCalleeSessionName, inv, i);
                            //todo: how to handle methods here?
                        } else if (analyzer.getCurrClassCallStackHolder().resolveEventualGenerics(expr.getType())
                                .isSubtypeOf(MatcherHelper.getTypeReference(FlowLogic.class))) {
                            addToMapIfNoException(methodInvocation.callerFlowNameToCalleeFlowName, inv, i);
                            //todo: how to handle methods here?
                        }
                    }
                    final Branch allRelevantMethodInvocations = StaticAnalyzerUtils.getAllRelevantMethodInvocations(expr,
                            analyzer);
                    methodInvocation.internalMethodInvocations.addIfRelevantForLoopFlowBreakAnalysis(allRelevantMethodInvocations);
                }
            }
            try {
                final Branch bodyStatements = MatcherHelper.fromCtStatementsToStatements(
                        dynamicallyDispatchedExecutable.getBody().getStatements(), analyzer);
                methodInvocation.body.addIfRelevantForLoopFlowBreakAnalysis(bodyStatements);
            } catch (NullPointerException e) {
                LOGGER.warn("Couldn't retrieve the body of method {} adding an empty one", inv);
            }
            methodInvocation.buildGraphElem();
        }

        return methodInvocation;
    }

    private static void addToMapIfNoException(Map<String, String> map, CtAbstractInvocation inv, int i) {
        try {
            map.put(inv.getArguments().get(i).toString(),
                    ((CtParameter) inv.getExecutable().getDeclaration().getParameters().get(i))
                            .getSimpleName());
        }catch (NullPointerException e) {
            LOGGER.warn("Error while retrieving parameter name for method: {}", inv);
        }
    }

    @Override
    public GBaseComponent getGraphElem() {
        if (toBePainted()) {
            return body.isEmpty() ? super.getGraphElem() : indentedComponent;
        }
        else {
            return null;
        }
    }

    @Override
    protected void buildGraphElem() {
        indentedComponent.setEnteringArrowText((GBaseText) super.getGraphElem());
        body.forEach(stmt -> indentedComponent.addComponent(stmt.getGraphElem()));
        if(returnType != null) {
            indentedComponent.setExitingArrowText(Utils.removePackageDescriptionIfWanted(returnType));
        }
    }

    @Override
    public boolean isRelevantForLoopFlowBreakAnalysis() {
        return isRelevantForProtocolAnalysis();
    }

    @Override
    public boolean isRelevantForMethodFlowBreakAnalysis() {
        return isRelevantForProtocolAnalysis();
    }

    @Override
    public boolean isRelevantForProtocolAnalysis() {
        return body.isRelevantForProtocolAnalysis();
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

    @Override
    public CombinationsHolder getResultingCombinations() {
        final CombinationsHolder res = CombinationsHolder.fromBranch(body);
        res.removeAllLocks();
        return res;
    }

    @Override
    public boolean checkIfContainsValidProtocolAndSetupLinks() {
        return super.checkIfContainsValidProtocolAndSetupLinks()
                && getBody().allInitiatingFlowsHaveValidProtocolAndSetupLinks();
    }
}
