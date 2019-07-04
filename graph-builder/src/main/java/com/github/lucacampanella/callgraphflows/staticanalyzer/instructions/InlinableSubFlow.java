package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components.GSubFlow;
import com.github.lucacampanella.callgraphflows.graphics.components2.GSubFlowIndented;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;


public class InlinableSubFlow extends SubFlowBaseWithAnalysis {

    GSubFlowIndented graphElem = new GSubFlowIndented();

    protected InlinableSubFlow() {

    }

    @Override
    public boolean isInlinableSubFlow() {
        return true;
    }

    @Override
    public GSubFlowIndented getGraphElem() {
        return toBePainted() ? graphElem : null;
    }

    @Override
    protected void buildGraphElem() {
        graphElem = getMainSubFlowElement();
    }

    //we don't want to analyze the call, but just the body of the subflow
    public Branch getBodyInstructionsForCombinations() {
        //if the flow doesn't initiate anything than we just inline it, for analysis is the same
        //we keep the subFlow call because we need the map of sessions passed
        Branch res = new Branch();
        res.add(resultOfClassAnalysis.getStatements());
        return res;
    }

    @Override
    public String toString() {
        return "InlinableSubFlow<<" + resultOfClassAnalysis.getClassDescription().getNameWithParent() + ">> :" + graphElem.toString();
    }
}





