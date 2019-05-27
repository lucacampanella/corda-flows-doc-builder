package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalyzerWithModel;
import com.github.lucacampanella.callgraphflows.staticanalyzer.StaticAnalyzer;
import net.corda.core.flows.FlowSession;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtTypedElement;
import spoon.support.reflect.code.CtAssignmentImpl;

public class StatementWithRelevantMethods extends InstructionStatement {

    protected StatementWithRelevantMethods(CtStatement statement) {
        super(statement);
    }

    public static StatementWithRelevantMethods fromCtStatement(CtStatement statement, AnalyzerWithModel analyzer) {
        StatementWithRelevantMethods statementWithRelevantMethods = new StatementWithRelevantMethods(statement);
        statementWithRelevantMethods.internalMethodInvocations.add(
                StaticAnalyzer.getAllRelevantMethodInvocations(statement, analyzer));

//        final List<CtInvocation> ctInvocations = exploreUntilInvocationOrNoChildren(statement);
//
//        (ctInvocations.stream().map(MethodInvocation::fromCtStatement).collect(Collectors.toList()))

        return statementWithRelevantMethods;
    }

    @Override
    public boolean isRelevantForAnalysis() {
        return internalMethodInvocations.isRelevant();
    }

//    private static List<CtInvocation> exploreUntilInvocationOrNoChildren(CtElement elem) {
//        final List<CtElement> directChildren = elem.getDirectChildren();
//        final List<CtInvocation> invocations = new LinkedList<>();
//
//        for(CtElement child : directChildren) {
//            if(child instanceof CtInvocation) {
//                invocations.add((CtInvocation) child);
//            }
//            else {
//                invocations.addAll(exploreUntilInvocationOrNoChildren(child));
//            }
//        }
//
//        return invocations;
//    }
}
