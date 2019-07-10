package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components2.GSubFlowIndented;
import com.github.lucacampanella.callgraphflows.staticanalyzer.CombinationsHolder;


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
        graphElem = resultOfClassAnalysis.getGraphicRepresentationNoTitles().getMainSubFlow();
    }

    @Override
    public CombinationsHolder getResultingCombinations() {
        final CombinationsHolder res = CombinationsHolder.fromBranch(resultOfClassAnalysis.getStatements());
        res.removeAllLocks(); //the method here is the call method of the flow
        return res;
    }

    @Override
    public String toString() {
        return "InlinableSubFlow<<" + resultOfClassAnalysis.getClassDescription().getNameWithParent() + ">> :" + graphElem.toString();
    }

    @Override
    public boolean hasSendOrReceiveAtThisLevel() {
        return super.hasSendOrReceiveAtThisLevel()
                || getResultOfClassAnalysis().getStatements().hasSendOrReceiveAtThisLevel();
    }
}





