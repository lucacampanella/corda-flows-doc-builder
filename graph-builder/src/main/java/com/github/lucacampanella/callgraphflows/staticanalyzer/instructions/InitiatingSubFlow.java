package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components2.*;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;


public class InitiatingSubFlow extends SubFlowBaseWithAnalysis {

    GTwoSidedContainer graphElem = new GTwoSidedContainer();

    protected InitiatingSubFlow() {

    }

    @Override
    public boolean isInitiatingSubFlow() {
        return true;
    }

    @Override
    public GTwoSidedContainer getGraphElem() {
        return toBePainted() ? graphElem : null;
    }

    @Override
    protected void buildGraphElem() {
        graphElem.setMainSubFlow(getMainSubFlowElement());
        if(resultOfClassAnalysis.hasCounterpartyResult()) {
            final AnalysisResult counterpartyClassResult = resultOfClassAnalysis.getCounterpartyClassResult();

            final InitiateFlow initiateFlow = resultOfClassAnalysis.getStatements().getInitiateFlowStatementAtThisLevel()
                    .orElseThrow(() -> new RuntimeException("Error building the graph elem for " +
                            this.toString() + " because couldn't find the corresponding initiateFlow" +
                            " call"));

            String enteringArrowText = counterpartyClassResult.getClassDescription().getNameWithParent() +
                    "\n" + "@InitiatedBy(" + initiateFlow.getInitiatingClassDescription().getNameWithParent() + ")";

            GSubFlowIndented counterpartyFlow = new GSubFlowIndented();
            counterpartyFlow.setEnteringArrowText(
                    new GBaseText(enteringArrowText));

            counterpartyClassResult.getStatements().forEach(stmt -> counterpartyFlow.addComponent(stmt.getGraphElem()));
            graphElem.setCounterpartySubFlow(counterpartyFlow,
                    (GInstruction) initiateFlow.getGraphElem());
        }
    }

    @Override
    public String toString() {
        return "InitiatingSubFlow<<" + resultOfClassAnalysis.getClassDescription().getNameWithParent() + ">> :" + graphElem.toString();
    }

    public boolean checkIfContainsValidProtocolAndDraw() {
        return resultOfClassAnalysis.checkIfContainsValidProtocolAndSetupLinks();
    }
}





