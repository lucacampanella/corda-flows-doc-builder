package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.graphics.components2.GBaseText;
import com.github.lucacampanella.callgraphflows.graphics.components2.GInstruction;
import com.github.lucacampanella.callgraphflows.graphics.components.GSubFlow;
import com.github.lucacampanella.callgraphflows.graphics.components.GSubFlowWithCounterparty;
import com.github.lucacampanella.callgraphflows.graphics.components2.GSubFlowIndented;
import com.github.lucacampanella.callgraphflows.graphics.components2.GTwoSidedContainerInitiatingFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;


public class InitiatingSubFlow extends SubFlowBaseWithAnalysis {

    GTwoSidedContainerInitiatingFlow graphElem = new GTwoSidedContainerInitiatingFlow();

    protected InitiatingSubFlow() {

    }

    @Override
    public boolean isInitiatingSubFlow() {
        return true;
    }

    @Override
    public GTwoSidedContainerInitiatingFlow getGraphElem() {
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

    public Branch getInstructionsForCombinations() {
        //we return just the flow if is an initiating flow
        //this will result on acceptCompanion being called on this instance
        //for corda flows we'll just check if the flow matchesAnyChildren
        //for initiating flow we'll get the other class and compare if they have at least one matching combination
        return new Branch(this);
    }

    public boolean checkIfContainsValidProtocolAndDraw() {
        return resultOfClassAnalysis.checkIfContainsValidProtocolAndDraw();
    }
}





