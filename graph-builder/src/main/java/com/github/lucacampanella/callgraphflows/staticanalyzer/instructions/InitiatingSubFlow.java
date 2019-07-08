package com.github.lucacampanella.callgraphflows.staticanalyzer.instructions;

import com.github.lucacampanella.callgraphflows.graphics.components2.GTwoSidedContainer;


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
        graphElem = resultOfClassAnalysis.getGraphicRepresentationNoTitles();
    }

    @Override
    public String toString() {
        return "InitiatingSubFlow<<" + resultOfClassAnalysis.getClassDescription().getNameWithParent() + ">> :" + graphElem.toString();
    }

    public boolean checkIfContainsValidProtocolAndDraw() {
        return resultOfClassAnalysis.checkIfContainsValidProtocolAndSetupLinks();
    }
}





