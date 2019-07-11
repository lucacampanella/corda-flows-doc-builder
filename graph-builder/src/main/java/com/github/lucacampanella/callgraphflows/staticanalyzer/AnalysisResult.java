package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.graphics.components2.GTwoSidedContainer;

public class AnalysisResult {

    ClassDescriptionContainer classDescription;
    Branch statements = new Branch();
    AnalysisResult counterpartyClassResult = null;
    Boolean containsValidProtocolAndDrawn = null;
    private GTwoSidedContainer graphicRepresentation = null;

    public AnalysisResult(ClassDescriptionContainer classDescription) {
        this.classDescription = classDescription;
    }

    public AnalysisResult() {
        this.classDescription = ClassDescriptionContainer.getEmpty();
    }

    public Branch getStatements() {
        return statements;
    }

    public AnalysisResult getCounterpartyClassResult() {
        return counterpartyClassResult;
    }

    public boolean hasCounterpartyResult() {
        return  counterpartyClassResult != null;
    }

    public void setCounterpartyClassResult(AnalysisResult counterpartyClassResult) {
        this.counterpartyClassResult = counterpartyClassResult;
    }

    public void setStatements(Branch statements) {
        this.statements = statements;
    }

    public boolean checkIfContainsValidProtocolAndSetupLinks() {
        if(containsValidProtocolAndDrawn == null) {
            containsValidProtocolAndDrawn = checkIfContainsValidProtocolAndDrawNotLazy();
        }
        return containsValidProtocolAndDrawn;
    }

    private boolean checkIfContainsValidProtocolAndDrawNotLazy() {
        //first of all we check that all initiating subflows have a valid protocol
        final boolean validProtocol = getStatements().allInitiatingFlowsHaveValidProtocolAndSetupLinks();
        if(!validProtocol) {
            return false;
        }
        if(hasCounterpartyResult()) {
            final boolean validProtocolCounterparty = counterpartyClassResult.getStatements()
                    .allInitiatingFlowsHaveValidProtocolAndSetupLinks();
            if (!validProtocolCounterparty) {
                return false;
            }
        }

        if (hasCounterpartyResult()) {
            CombinationsHolder allCombinations = CombinationsHolder.fromBranch(statements);
            //then if it's an initiatingFlow we check that all possible send and receive combinations match
            CombinationsHolder counterpartyAllCombinations =
                    CombinationsHolder.fromBranch(counterpartyClassResult.getStatements());

            return allCombinations.checkIfMatchesAndDraw(counterpartyAllCombinations);
        } else {
            //then if it's NOT an initiatingFlow we check that it doesn't call any send or receive
            //(remember that we inline the inlinable non initiating flow, so they won't be analyzed here
            //this is only for "container flows" tagged ad @StartableByRPC"
            return !statements.hasSendOrReceiveAtThisLevel();
        }
    }

    public ClassDescriptionContainer getClassDescription() {
        return classDescription;
    }

    public GTwoSidedContainer getGraphicRepresentationNoTitles() {
        if(graphicRepresentation == null) {
            graphicRepresentation = GTwoSidedContainer.fromAnalysisResult(this);
        }
        return graphicRepresentation;
    }
}
