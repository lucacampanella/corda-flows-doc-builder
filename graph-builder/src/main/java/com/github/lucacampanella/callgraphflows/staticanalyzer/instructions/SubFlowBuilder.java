package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components.GInstruction;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzerUtils;
import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import net.corda.core.flows.*;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtAssignmentImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SubFlowBuilder {

    private SubFlowBuilder() {
        //private constructor to hide public one
    }

    public static class SubFlowInfo {
        CtTypeReference<? extends FlowLogic> subFlowType = null;
        Optional<String> assignedVariableName = Optional.empty(); //if the subflow returns an object and assigns it
        //to a variable this is present and contains the variable name

        int line = 0;

        Branch internalMethodInvocations = new Branch();

        Optional<String> returnType;

        Optional<String> subFlowVariableName = Optional.empty(); //if the subFlow is called with a variable as a
        //parameter and not with a constructor this is present

        protected Optional<String> targetSessionName = Optional.empty();

        Boolean isInitiatingFlow = null;

        GInstruction initiatingInstruction; //this is the call to subFlow

        public void initializeFlow(SubFlowBase flow) {
            flow.subFlowType = subFlowType;
            flow.assignedVariableName = assignedVariableName;
            flow.line = line;
            flow.internalMethodInvocations = internalMethodInvocations;
            flow.returnType = returnType;
            flow.subFlowVariableName = subFlowVariableName;
            flow.targetSessionName = targetSessionName;
            flow.isInitiatingFlow = isInitiatingFlow;
            flow.initiatingInstruction = initiatingInstruction;

            flow.buildGraphElem();
        }
    }

    private static HashMap<CtTypeReference, CtTypeReference> cordaSpecialFlows = new HashMap<>();
    /*
    Special Corda flows:
    - SendTransactionFlow and ReceiveTransactionFlow
    - SendStateAndRefFlow and ReceiveStateAndRefFlow
    - CollectSignaturesFlow and SignTransactionFlow
    - FinalityFlow and ReceiveFinalityFlow
     */
    static {
        cordaSpecialFlows.put(MatcherHelper.getTypeReference(SendTransactionFlow.class),
                MatcherHelper.getTypeReference(ReceiveTransactionFlow.class));

        cordaSpecialFlows.put(MatcherHelper.getTypeReference(SendStateAndRefFlow.class),
                MatcherHelper.getTypeReference(ReceiveStateAndRefFlow.class));

        cordaSpecialFlows.put(MatcherHelper.getTypeReference(CollectSignaturesFlow.class),
                MatcherHelper.getTypeReference(SignTransactionFlow.class));

        cordaSpecialFlows.put(MatcherHelper.getTypeReference(FinalityFlow.class),
                MatcherHelper.getTypeReference(ReceiveFinalityFlow.class));
    }

    public static SubFlowBase fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {

        SubFlowInfo subFlowInfo = new SubFlowInfo();

        subFlowInfo.line = statement.getPosition().getLine();

        subFlowInfo.internalMethodInvocations.add(StaticAnalyzerUtils.getAllRelevantMethodInvocations(statement, analyzer));

        if(statement instanceof CtLocalVariable) {
            subFlowInfo.assignedVariableName = Optional.ofNullable(((CtLocalVariable) statement).getSimpleName());
        }
        else if(statement instanceof CtAssignment) {
            subFlowInfo.assignedVariableName = Optional.ofNullable(((CtAssignmentImpl) statement).getAssigned().toString());
        }

        final CtExpression expression = MatcherHelper.getFirstMatchedExpression(statement, "subFlowMatcher");
        if(expression instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) expression;
            Object firstArgument = invocation.getArguments().get(0);
            if(firstArgument instanceof CtAbstractInvocation) {

                subFlowInfo.subFlowType = ((CtAbstractInvocation) firstArgument).getExecutable().getType();

                //this works if the subflow is created with a "new" in the subFlow invocation itself, otherwise it doesn't
                //if it doesn't a more profound search must be done, looking for which flow is passed and what
                //session this passed flow references
                subFlowInfo.targetSessionName =  StaticAnalyzerUtils.findTargetSessionName(statement);
            }
            else if(firstArgument instanceof CtVariableRead) {
                subFlowInfo.subFlowType = ((CtVariableRead) firstArgument).getVariable().getType();
                subFlowInfo.subFlowVariableName = Optional.ofNullable(((CtVariableRead) firstArgument).getVariable().getSimpleName());

            }

            final CtMethod callMethod = StaticAnalyzerUtils.findCallMethod(
                    (CtClass) subFlowInfo.subFlowType.getTypeDeclaration());

            if(callMethod != null) {
                subFlowInfo.returnType = Optional.ofNullable(callMethod.getType().toString());
            }

        }

        SubFlowBase result;
        if(isInitiatingSpecialCordaFlow(subFlowInfo.subFlowType)) {
            subFlowInfo.isInitiatingFlow = true;
            result = new CordaSubFlow();
        }
        else if(isInitiatedSpecialCordaFlow(subFlowInfo.subFlowType)) {
            subFlowInfo.isInitiatingFlow = false;
            result = new CordaSubFlow();
        }
        else { //is not a corda flow
            final AnalysisResult resultOfClassAnalysis = analyzer.analyzeFlowLogicClass(
                    (CtClass) subFlowInfo.subFlowType.getDeclaration());

            subFlowInfo.isInitiatingFlow = resultOfClassAnalysis.hasCounterpartyResult();
            if(subFlowInfo.isInitiatingFlow) {
                result = new InitiatingSubFlow();
            }
            else {
                result = new InlinableSubFlow();
            }
            ((SubFlowBaseWithAnalysis) result).resultOfClassAnalysis = resultOfClassAnalysis;
        }

        subFlowInfo.initializeFlow(result);
        return result;
    }

    private static boolean isInitiatingSpecialCordaFlow(CtTypeReference subFlowType) {
        for (Map.Entry<CtTypeReference, CtTypeReference> entry : cordaSpecialFlows.entrySet()) {
            CtTypeReference cordaInitiatingFlow = entry.getKey();
            if (subFlowType.isSubtypeOf(cordaInitiatingFlow)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInitiatedSpecialCordaFlow(CtTypeReference subFlowType) {
        for (Map.Entry<CtTypeReference, CtTypeReference> entry : cordaSpecialFlows.entrySet()) {
            CtTypeReference cordaInitiatedFlow = entry.getValue();
            if (subFlowType.isSubtypeOf(cordaInitiatedFlow)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean areMatchingSpecialCordaFlow(CtTypeReference initiatingFlow, CtTypeReference initiatedFlow) {
        for (Map.Entry<CtTypeReference, CtTypeReference> entry : cordaSpecialFlows.entrySet()) {
            CtTypeReference cordaInitiatingFlow = entry.getKey();
            CtTypeReference cordaInitiatedFlow = entry.getValue();
            if (initiatingFlow.isSubtypeOf(cordaInitiatingFlow) && initiatedFlow.isSubtypeOf(cordaInitiatedFlow)) {
                return true;
            }
        }
        return false;
    }
}
