package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components.GSubFlow;
import com.github.lucacampanella.callgraphflows.staticanalyzer.Branch;


public class InlinableSubFlow extends SubFlowBaseWithAnalysis {

    GSubFlow graphElem = new GSubFlow();

    protected InlinableSubFlow() {

    }

    @Override
    public boolean isInlinableSubFlow() {
        return true;
    }

    @Override
    public GSubFlow getGraphElem() {
        return toBePainted() ? graphElem : null;
    }

    @Override
    protected void buildGraphElem() {
        graphElem = getMainSubFlowElement();
    }

    public Branch getInstructionsForCombinations() {
        //if the flow doesn't initiate anything than we just inline it, for analysis is the same
        //we keep the subFlow call because we need the map of sessions passed
        Branch res = new Branch(this);
        res.add(resultOfClassAnalysis.getStatements());
        return res;
    }
}





