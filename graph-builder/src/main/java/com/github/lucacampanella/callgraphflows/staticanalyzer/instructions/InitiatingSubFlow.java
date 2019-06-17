package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components.GBaseTextComponent;
import com.github.lucacampanella.callgraphflows.graphics.components.GInstruction;
import com.github.lucacampanella.callgraphflows.graphics.components.GSubFlow;
import com.github.lucacampanella.callgraphflows.graphics.components.GSubFlowWithCounterparty;
import com.github.lucacampanella.callgraphflows.staticanalyzer.AnalysisResult;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;



public class InitiatingSubFlow extends SubFlowBaseWithAnalysis {

    GSubFlowWithCounterparty graphElem = new GSubFlowWithCounterparty();

    protected InitiatingSubFlow() {

    }

    @Override
    public boolean isInitiatingSubFlow() {
        return true;
    }

    @Override
    public GSubFlowWithCounterparty getGraphElem() {
        return toBePainted() ? graphElem : null;
    }

    @Override
    protected void buildGraphElem() {
        graphElem.setMainSubFlow(getMainSubFlowElement());
        if(resultOfClassAnalysis.hasCounterpartyResult()) {
            final AnalysisResult counterpartyClassResult = resultOfClassAnalysis.getCounterpartyClassResult();
            GSubFlow counterpartyFlow = new GSubFlow();
            counterpartyFlow.setEnteringArrowText(
                    new GBaseTextComponent(counterpartyClassResult.getClassDescription().getNameWithParent()));
            counterpartyClassResult.getStatements().forEach(stmt -> counterpartyFlow.addComponent(stmt.getGraphElem()));
            graphElem.setCounterpartySubFlow(counterpartyFlow,
                    (GInstruction) resultOfClassAnalysis.getStatements().getInitiateFlowStatementAtThisLevel()
                            .orElseThrow(() -> new RuntimeException("Error buildinf the graph elem for " +
                                    this.toString() + " because couldn't find the corresponding itiate flow" +
                                    " call")).getGraphElem());
        }
    }

    public Branch getInstructionsForCombinations() {
        //we return just the flow if is an initiating flow
        //this will result on acceptCompanion being called on this instance
        //for carda flows we'll just check if the flow matchesAnyChildren
        //for initiating flow we'll get the other class and compare if they have at least one matching combination
        return new Branch(this);
    }
}





