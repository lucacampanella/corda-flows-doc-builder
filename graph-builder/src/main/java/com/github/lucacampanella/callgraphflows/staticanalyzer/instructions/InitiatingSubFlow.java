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

    /**
     * This method is called when checking the protocol makes
     * sense. Not to be confused with {@link StatementWithCompanionInterface#acceptCompanion(StatementWithCompanionInterface)}
     * which is called in a later stage.
     * @return true if the protocol makes sense, false otherwise
     */
    @Override
    public boolean checkIfContainsValidProtocolAndDraw() {
        return resultOfClassAnalysis.checkIfContainsValidProtocolAndSetupLinks();
    }
}





